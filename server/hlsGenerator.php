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
	if (!file_exists($videoResultPath.DIRECTORY_SEPARATOR."original")) {
	    mkdir($videoResultPath.DIRECTORY_SEPARATOR."original", 0777, true);
	}
	if (!file_exists($videoResultPath.DIRECTORY_SEPARATOR."half")) {
	    mkdir($videoResultPath.DIRECTORY_SEPARATOR."half", 0777, true);
	}
	if (!file_exists($videoResultPath.DIRECTORY_SEPARATOR."quarter")) {
	    mkdir($videoResultPath.DIRECTORY_SEPARATOR."quater", 0777, true);
	}

	//Analyze Original Video
	$getID3 = new getID3;
	$videoInfo = $getID3->analyze($sourceVideoPath);
	$width = $videoInfo['video']['resolution_x'];
	$height = $videoInfo['video']['resolution_y'];
	$fps = $videoInfo["video"]["frame_rate"];
	$duration = $videoInfo["playtime_seconds"];

	//convert videos
	prepareVideoSegment($sourceVideoPath, $videoResultPath, $videoName, $fps, $width, $height);
	prepareHls($videoResultPath, $videoName, $baseVideoName, $duration);
}

function prepareVideoSegment($sourceVideoPath, $videoResultPath, $videoName, $fps, $width, $height) {
	shell_exec("/usr/local/bin/convert.sh ".$sourceVideoPath." 3072 ".$fps." ".$width."x".$height." 44100 128 "
					.$videoResultPath.DIRECTORY_SEPARATOR."original".DIRECTORY_SEPARATOR.$videoName.".ts");
	shell_exec("/usr/local/bin/convert.sh ".$sourceVideoPath." 768 ".$fps." ".($width / 2)."x".($height / 2)." 44100 128 "
					.$videoResultPath.DIRECTORY_SEPARATOR."half".DIRECTORY_SEPARATOR.$videoName.".ts");
	shell_exec("/usr/local/bin/convert.sh ".$sourceVideoPath." 200 ".$fps." ".($width / 4)."x".($height / 4)." 44100 128 "
					.$videoResultPath.DIRECTORY_SEPARATOR."quater".DIRECTORY_SEPARATOR.$videoName.".ts");
}

function prepareHls($videoResultPath, $videoName, $baseVideoName, $duration) {
	$hlsOriginal = $videoResultPath . DIRECTORY_SEPARATOR . "original" . DIRECTORY_SEPARATOR . "original.m3u8";
	$hlsHalf = $videoResultPath . DIRECTORY_SEPARATOR . "half" . DIRECTORY_SEPARATOR . "half.m3u8";
	$hlsQuarter = $videoResultPath . DIRECTORY_SEPARATOR . "quater" . DIRECTORY_SEPARATOR . "quarter.m3u8";

	makeSingleHls($hlsOriginal, $videoName . ".ts", $duration);
	makeSingleHls($hlsHalf, $videoName . ".ts", $duration);
	makeSingleHls($hlsQuarter, $videoName . ".ts", $duration);

	makeMultibandwidthHld($baseVideoName, $hlsOriginal, $hlsHalf, $hlsQuarter);
}

function makeSingleHls($hlsPath, $videoPath, $duration) {
	if (!file_exists($hlsPath)) {
		$content = "#EXTM3U\n"
					. "#EXT-X-TARGETDURATION:". $duration . "\n"
					. "#EXT-X-MEDIA-SEQUENCE:1\n"
					. "#EXT-X-ENDLIST";
	} else {
		$content = file_get_contents($hlsPath);	
	}

	$contentToInsert = "#EXTINF:" . $duration . ",\n"
						. $videoPath . "\n";
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
				. $hlsOriginal . "\n"
				. "#EXT-X-STREAM-INF:PROGRAM-ID=1, BANDWIDTH=768000\n"
				. $hlsHalf . "\n"
				. "#EXT-X-STREAM-INF:PROGRAM-ID=1, BANDWIDTH=200000\n"
				. $hlsQuarter;
	$file = fopen($hlsPlaylistPath . DIRECTORY_SEPARATOR . $baseVideoName . ".m3u8","w");
	fwrite($file, $content);
	fclose($file);
}

?>