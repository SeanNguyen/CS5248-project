 <?php

    $video_name =array("video_200k","video_768k","video_3072k"); // as given in generate_hls_playlist.php

    $dir = $argv[1];
    $i = 1;
    $path = preg_split('\'public_html\'',$dir);
    echo $path[1];
    $bandwidth_array = array(200000,768000,3072000);
	$output = "#EXTM3U\n";
        if(($dh = @opendir($dir)) !== false)
        {
	    while($i <= 3){
		    $output .= "#EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=".$bandwidth_array[$i-1]."\n\n";
   	    	    $output .= "http://137.132.82.164/~a0118982".$path[1]."playlists_".$i."/".$video_name[$i-1].".m3u8\n\n"; //path[1] = vid_rep/<uploadedfoldername/>
		$i +=1;
	    }	   
 	    echo shell_exec("mkdir ".$argv[1]."main_playlist");
            $fp = fopen($argv[1]."main_playlist/test_video.m3u8","w");
	    $output .= "#EXT-X-ENDLIST\n\n";
	    @closedir($dh);
	    fwrite($fp, $output);
            fclose($fp);
            echo "\nPlaylist ".$i." created";
        }
        else
        {
            die('unable to generate playlist');
        }

?> 
