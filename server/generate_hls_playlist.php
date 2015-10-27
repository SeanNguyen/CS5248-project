 <?php

    $video_name =array("video_200k","video_768k","video_3072k");// for convenience
    $total_duration = 3;

    $i = 1;
    while($i <= 3){
		$dir = $argv[1]. "_output".$i."/";
		if(($dh = @opendir($dir)) !== false) {
			$output = "#EXTM3U\n";
			$output .= "#EXT-X-TARGETDURATION:4\n";// maximum length of all streamlets.segmentation gives a cileing value of 4s
			$output .= "#EXT-X-MEDIA-SEQUENCE:1\n";
			$files_in_dir = @scandir($dir);
			natsort($files_in_dir);
			while(list($key, $val) = each($files_in_dir)) {
				preg_match("/(.+?)(\.[^.]*$|$)/", $val, $matches);
				$file_ext = $matches[2];
				preg_match("/(.+?)(\_[^_]*$|$)/", $matches[1], $mat);
				$path_for_url = preg_split('\'public_html\'', $dir);	
				if($val != '.' && $val != '..' && $file_ext == ".ts") {
					// the below command uses ffprobe tofind the duration of the equivalent mp4 streamlet.
					$duration_line = shell_exec("/usr/local/bin/ffprobe ".$dir.$matches[1].".mp4 2>&1 | grep Duration");
					$duration_string = preg_split('\'\\,\\ start\'',$duration_line);
					preg_match('/^(?:[^:]*:)+([^:]*)$/', $duration_string[0], $dd); // $duration_string = "Duration 04:57:00"
					$as1=$dd[1]; //dd = {0 => "Duration 04:57:00", 1 => "04.57"}
					$as2=round($as1) ;
					$output .= "#EXTINF:".$as2.", no desc\n";
					$output .= "http://137.132.82.164/~a0118982".$path_for_url[1]."/".$val."\n"; // path_for_url = videos/<upladed folder name>/output_$i/                   
				}
			}
			$output .= "#EXT-X-ENDLIST";
			@closedir($dh);
			echo shell_exec("mkdir ".$argv[1]."playlists_".$i);
			$fp = fopen($argv[1]."playlists_".$i."/".$video_name[$i-1].".m3u8", "w");
			fwrite($fp, $output);
			fclose($fp);
		} else {
			die('unable to generate playlist');
		}
        $i += 1;
    }
	echo shell_exec("php generate_multi_bandwidth_hls_playlist.php ".$argv[1]);
?> 
