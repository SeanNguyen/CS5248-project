<?php

include_once("utilities.php");
include_once("getid3/getid3.php");

function makeHls($sourceVideoPath) {
	//some static constants
	$videoRepoPath = "video_repo";
	$videoExt = getFileExtension($sourceVideoPath);
	$videoName = basename($sourceVideoPath, "." . $videoExt);
	
	$baseVideoName = getVideoName($videoName);
	$segment = getSegmentNumber($videoName);
	$videoResultPath = $videoRepoPath . DIRECTORY_SEPARATOR . $baseVideoName;

	//check if source video exist
	if(!file_exists($sourceVideoPath)) {
		echo "Source file does not exist!";
		return;
	}

	//prepare an empty result folder
	if (!file_exists($videoResultPath)) {
	    mkdir($videoResultPath, 0777, true);
	}

	//convert videos
	prepareVideoSegment($sourceVideoPath, $videoResultPath.DIRECTORY_SEPARATOR.$videoName);
	prepareHls($videoResultPath, $videoName, $baseVideoName);
}

function prepareVideoSegment($sourceVideoPath, $videoResultPath) {
	//Analyze Original Video
	$getID3 = new getID3;
	$videoInfo = $getID3->analyze($sourceVideoPath);
	$width = $videoInfo['video']['resolution_x'];
	$height = $videoInfo['video']['resolution_y'];
	$fps = $videoInfo["video"]["frame_rate"];

	shell_exec("/usr/local/bin/convert.sh ".$sourceVideoPath." 3072 ".$fps." ".$width."x".$height." 44100 128 ".$videoResultPath."_original.ts");
	shell_exec("/usr/local/bin/convert.sh ".$sourceVideoPath." 768 ".$fps." ".($width / 2)."x".($height / 2)." 44100 128 ".$videoResultPath."_half.ts");
	shell_exec("/usr/local/bin/convert.sh ".$sourceVideoPath." 200 ".$fps." ".($width / 4)."x".($height / 4)." 44100 128 ".$videoResultPath."_quarter.ts");
}

function prepareHls($videoResultPath, $videoName, $baseVideoName) {
	$originalVideo = $videoResultPath . DIRECTORY_SEPARATOR . $videoName . "_original.ts";
	$halfVideo = $videoResultPath . DIRECTORY_SEPARATOR . $videoName . "_half.ts";
	$quarterVideo = $videoResultPath . DIRECTORY_SEPARATOR . $videoName . "_quarter.ts";

	$hlsOriginal = $videoResultPath . DIRECTORY_SEPARATOR . "original.m3u8";
	$hlsHalf = $videoResultPath . DIRECTORY_SEPARATOR . "half.m3u8";
	$hlsQuarter = $videoResultPath . DIRECTORY_SEPARATOR . "quarter.m3u8";

	makeSingleHls($hlsOriginal, $originalVideo);
	makeSingleHls($hlsHalf, $halfVideo);
	makeSingleHls($hlsQuarter, $quarterVideo);

	makeMultibandwidthHld($baseVideoName, $hlsOriginal, $hlsHalf, $hlsQuarter);
}

function makeSingleHls($hlsPath, $videoPath) {
	if (!file_exists($hlsPath)) {
		$content = "#EXTM3U\n"
					. "#EXT-X-TARGETDURATION:10\n"
					. "#EXT-X-MEDIA-SEQUENCE:0\n"
					. "#EXT-X-ENDLIST";
	} else {
		$content = file_get_contents($hlsPath);	
	}

	$contentToInsert = "#EXTINF:10,\n"
						. "http://pilatus.d1.comp.nus.edu.sg/~team10/" . $videoPath . "\n";
	$insertPos = strrpos($content, "#EXT-X-ENDLIST");
	$content = substr_replace($content, $contentToInsert, $insertPos, 0);

	$file = fopen($hlsPath,"w");
	fwrite($file, $content);
	fclose($file);
}

function makeMultibandwidthHld($baseVideoName, $hlsOriginal, $hlsHalf, $hlsQuarter) {
	$hlsPlaylistPath = "video_repo" . DIRECTORY_SEPARATOR . "playlists";
	if (!file_exists($hlsPlaylistPath)) {
	    mkdir($hlsPlaylistPath, 0777, true);
	}
	$content = 	"EXTM3U\n"
				. "#EXT-X-STREAM-INF:PROGRAM-ID=1, BANDWIDTH=3072000\n"
				. "http://pilatus.d1.comp.nus.edu.sg/~team10/" . $hlsOriginal . "\n"
				. "#EXT-X-STREAM-INF:PROGRAM-ID=1, BANDWIDTH=768000\n"
				. "http://pilatus.d1.comp.nus.edu.sg/~team10/" . $hlsHalf . "\n"
				. "#EXT-X-STREAM-INF:PROGRAM-ID=1, BANDWIDTH=200000\n"
				. "http://pilatus.d1.comp.nus.edu.sg/~team10/" . $hlsQuarter;
	$file = fopen($hlsPlaylistPath . DIRECTORY_SEPARATOR . $baseVideoName . ".m3u8","w");
	fwrite($file, $content);
	fclose($file);
}

?>