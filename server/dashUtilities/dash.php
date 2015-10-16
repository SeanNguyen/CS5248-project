<?php

class dash {     
	$videoRepoUri = '../video_repo/';
	$videoExt = 'mp4';


	function prepareVideo($videoFileName) {
    	$videoFileUri = $videoRepoUri . $videoFileName;
		
		$command = 'x264 --output '
					. 'intermediate_2400k.264'
					. ' --fps 24'
					. ' --preset slow'
					. ' --bitrate 2400'
					. ' --vbv-maxrate 4800'
					. ' --vbv-bufsize 9600'
					. ' --min-keyint 48'
					. ' --keyint 48'
					. ' --scenecut 0'
					. ' --no-scenecut'
					. ' --pass 1'
					. ' --video-filter "resize:width=1280,height=720"'
					. $videoFileUri;
		echo exec($command);

		$command = 'MP4Box -add intermediate.264 -fps 24 ' . $videoFileUri . '.' . $videoExt;
		echo exec($command);
		return;
	}

    function makeDashSegmentAndMpd($videoFileName) {
    	$videoFileUri = $videoRepoUri . $videoFileName;
    	$command = 'MP4Box -dash 10000 -frag 10000 -rap -segment-name ' . $videoFileName . '_ ' . $videoFileUri . '.' . $videoExt;
    	echo exec($command);
    	return;
    } 
} 

?>