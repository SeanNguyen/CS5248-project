<?php

include 'utilities.php';

$videoRepoPath = "video_repo";
$mpdFileExt = "mpd";

$mpdList = array();

$it = new RecursiveIteratorIterator(new RecursiveDirectoryIterator($videoRepoPath));
foreach ($it as $file) {
	$fileExt = getFileExtension($file);
	if(strcmp($fileExt, $mpdFileExt) === 0) {
		$file = str_replace("\\","/",$file);
		$url = $file;
		array_push($mpdList, $url);
	}
}

$json = json_encode($mpdList);
die(str_replace("\/", "/", $json));

?>