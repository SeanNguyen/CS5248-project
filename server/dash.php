<?php

include 'utilities.php';

function makeMpd($sourceVideoPath) {

	//some static constants
	$videoRepoPath = "video_repo";
	$videoExt = "." . getFileExtension($sourceVideoPath);
	$videoName = basename($sourceVideoPath, $videoExt);

	//check if source video exist
	if(!file_exists($sourceVideoPath)) {
		echo "Source file does not exist!";
		return;
	}
	
	//run MP4 tool to generate mpd file
	$command = "MP4Box -dash 10000 -frag 10000 -rap -out " 
			. $videoRepoPath . DIRECTORY_SEPARATOR . $videoName
			. "  -segment-name " . $videoName . "_ " 
			. $sourceVideoPath;
	echo exec($command);
}

?>