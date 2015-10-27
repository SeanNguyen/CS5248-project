<?php
	// code to transcode
	if ($handle = opendir($test_file_name)) {
		while (false !== ($entry = readdir($handle))) {
			if($entry !== "." && $entry !== "..") {
				transcodeFile($test_file_name."/".$entry, $test_file_name, $dir_path, $entry);
			}
		}
		closedir($handle);
	}
	// command to run generate_hls_playlist
	echo shell_exec("php generate_hls_playlist.php ".$test_file_name);

	function transcodeFile($video_file, $video_file_path, $dir_path, $entry){
		preg_match("/(.+?)(\.[^.]*$|$)/", $entry, $matches);
		$file_name = $matches[1];
		echo shell_exec("mkdir ".$video_file_path."_output1");
	    echo shell_exec("mkdir ".$video_file_path."_output2");
	    echo shell_exec("mkdir ".$video_file_path."_output3");
		echo shell_exec("/usr/local/bin/convert.sh ".$video_file." 3072 29.97 720x480 44100 128 ".$video_file_path."_output3/".$file_name.".ts");
		echo shell_exec("/usr/local/bin/convert.sh ".$video_file." 3072 29.97 720x480 44100 128 ".$video_file_path."_output3/".$file_name.".mp4");
		echo shell_exec("/usr/local/bin/convert.sh ".$video_file." 768 29.97 480x320 44100 128 ".$video_file_path."_output2/".$file_name.".ts");
		echo shell_exec("/usr/local/bin/convert.sh ".$video_file." 768 29.97 480x320 44100 128 ".$video_file_path."_output2/".$file_name.".mp4");
		echo shell_exec("/usr/local/bin/convert.sh ".$video_file." 200 29.97 240x160 44100 128 ".$video_file_path."_output1/".$file_name.".ts");
		echo shell_exec("/usr/local/bin/convert.sh ".$video_file." 200 29.97 240x160 44100 128 ".$video_file_path."_output1/".$file_name.".mp4");
	}
?>
