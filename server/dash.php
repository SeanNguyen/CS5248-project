<?php

function makeMpd($sourceVideoPath) {

	include_once("utilities.php");
	include_once("getid3/getid3.php");

	//some static constants
	$videoRepoPath = "video_repo";

	$videoExt = getFileExtension($sourceVideoPath);
	$videoName = basename($sourceVideoPath, "." . $videoExt);
	$videoResultPath = $videoRepoPath . DIRECTORY_SEPARATOR . $videoName;

	//check if source video exist
	if(!file_exists($sourceVideoPath)) {
		echo "Source file does not exist!";
		return;
	}

	//Analyze Original Video
	$getID3 = new getID3;
	$videoInfo = $getID3->analyze($sourceVideoPath);
	$width = $videoInfo['video']['resolution_x'];
	$height = $videoInfo['video']['resolution_y'];
	$fps = $videoInfo["video"]["frame_rate"];
	$tentativeSegmentDuration = 10; //in seconds

	//prepare an empty result folder
	if (!file_exists($videoResultPath)) {
	    mkdir($videoResultPath, 0777, true);
	} else {
		$files = glob($videoResultPath . DIRECTORY_SEPARATOR . '*'); // get all file names
		foreach($files as $file){
		  if(is_file($file))
		    unlink($file); // delete file
		}
	}
	//Convert original video
	//e.g: x264 --output intermediate_2400k.264 --fps 24 --preset slow --min-keyint 48 --keyint 48 --scenecut 0 --no-scenecut --pass 1 --video-filter "resize:width=1280,height=720" inputvideo.mkv
	//ORIGINAL QUALITY
	// $command = 'x264'
	// 			. ' --output ' . $videoResultPath . DIRECTORY_SEPARATOR .'intermediate.264 --preset slow'
	// 			. ' --min-keyint ' . $fps * $tentativeSegmentDuration 
	// 			. ' --keyint ' . $fps * $tentativeSegmentDuration 
	// 			. ' --scenecut 0 --no-scenecut --pass 1 ' 
	// 			. $sourceVideoPath;
	// shell_exec($command);

	// $command = "MP4Box -add " . $videoResultPath . DIRECTORY_SEPARATOR . "intermediate.264 "
	// 			. $videoResultPath . DIRECTORY_SEPARATOR . $videoName . "_original.mp4";
	// shell_exec($command);

	$command = "cp " . $sourceVideoPath . " " . $videoResultPath . DIRECTORY_SEPARATOR . $videoName . "_original.mp4 ";
	shell_exec($command);

	//HALF QUALITY
	//ffmpeg -i SampleVideo_1080x720_5mb.mp4 -vf scale=320:240 output.mp4
	$command = "ffmpeg -i " .  $videoResultPath . DIRECTORY_SEPARATOR . $videoName . "_original.mp4 "
				. "-vf scale=". $width / 2 . ":" . $height  / 2 . " "
				. $videoResultPath . DIRECTORY_SEPARATOR . $videoName . "_half.mp4";
	shell_exec($command);
	// echo $command;

	//QUARTER QUALITY
	$command = "ffmpeg -i " .  $videoResultPath . DIRECTORY_SEPARATOR . $videoName . "_original.mp4 "
				. "-vf scale=". $width / 4 . ":" . $height  / 4 . " "
				. $videoResultPath . DIRECTORY_SEPARATOR . $videoName . "_quarter.mp4";
	shell_exec($command);
	// echo $command;

	//make mpd file for videos
	//ORIGINAL
	$mpdPath = $videoResultPath . DIRECTORY_SEPARATOR . $videoName;
	$command = 'MP4Box'
				. ' -out ' . $mpdPath
				. ' -dash 10000 -frag 10000 -rap'
				. ' -segment-name ' . $videoName . '_original_ '
				. $videoResultPath . DIRECTORY_SEPARATOR . $videoName . "_original.mp4#video";
	shell_exec($command);

	//HALF
	$halfMpdPath = $videoResultPath . DIRECTORY_SEPARATOR . $videoName . "_half";
	$command = 'MP4Box'
				. ' -out ' . $halfMpdPath
				. ' -dash 10000 -frag 10000 -rap'
				. ' -segment-name ' . $videoName . '_half_ '
				. $videoResultPath . DIRECTORY_SEPARATOR . $videoName . "_half.mp4#video";
	shell_exec($command);

	//QUARTER
	$quarterMpdPath = $videoResultPath . DIRECTORY_SEPARATOR . $videoName . "_quater";
	$command = 'MP4Box'
				. ' -out ' . $quarterMpdPath
				. ' -dash 10000 -frag 10000 -rap'
				. ' -segment-name ' . $videoName . '_quarter_ '
				. $videoResultPath . DIRECTORY_SEPARATOR . $videoName . "_quarter.mp4#video";
	shell_exec($command);

	//make mpd file for audio
	$audioMpdPath = $videoResultPath . DIRECTORY_SEPARATOR . $videoName . "_audio";
	$command = 'MP4Box'
				. ' -out ' . $audioMpdPath
				. ' -dash 10000 -frag 10000 -rap'
				. ' -segment-name ' . $videoName . '_audio_ '
				. $sourceVideoPath . "#audio";
	shell_exec($command);

	// //merge mpd files
	$mpd = file_get_contents($mpdPath . ".mpd");
	$mpdHalf = file_get_contents($halfMpdPath . ".mpd");
	$mpdQuarter = file_get_contents($quarterMpdPath . ".mpd");
	$mpdAudio = file_get_contents($audioMpdPath . ".mpd");

	//video half
	$adaptationSetEndPos = strrpos($mpd, "</AdaptationSet>");
	if($adaptationSetEndPos !== false) {
		$startCut = strrpos($mpdHalf, "<Representation");
		$endCut = strrpos($mpdHalf, "</AdaptationSet>");
		$representationContent = substr($mpdHalf, $startCut, $endCut - $startCut);
		$mpd = substr_replace($mpd, $representationContent, $adaptationSetEndPos, 0);
	}
	//video quarter
	$adaptationSetEndPos = strrpos($mpd, "</AdaptationSet>");
	if($adaptationSetEndPos !== false) {
		$startCut = strrpos($mpdQuarter, "<Representation");
		$endCut = strrpos($mpdQuarter, "</AdaptationSet>");
		$representationContent = substr($mpdQuarter, $startCut, $endCut - $startCut);
		$mpd = substr_replace($mpd, $representationContent, $adaptationSetEndPos, 0);
	}

	//audio
	$periodEndPos = strrpos($mpd, "</Period>");
	if($periodEndPos !== false) {
		$startCut = strrpos($mpdAudio, "<AdaptationSet");
		$endCut = strrpos($mpdAudio, "</Period>");
		$representationContent = substr($mpdAudio, $startCut, $endCut - $startCut);
		$mpd = substr_replace($mpd, $representationContent, $periodEndPos, 0);
	}

	//fix bug of path not found
	$mpd = str_replace($videoRepoPath . "/" . $videoName . "/", "", $mpd);

	//remove other mpd file
	$files = glob($videoResultPath . DIRECTORY_SEPARATOR . '*.mpd'); // get all file names
	foreach($files as $file){
	  if(is_file($file))
	    unlink($file); // delete file
	}

	$mpdFile = fopen($mpdPath . ".mpd", "w");
	fwrite($mpdFile, $mpd);
	fclose($mpdFile);
}

?>