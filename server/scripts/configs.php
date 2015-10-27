<?php
$user_name = "test";
$password = "123456";
$database = "cs5248";
$server = "localhost";

$db_handle=mysql_connect($server, $user_name, $password);
$db_found = mysql_select_db($database,$db_handle);
if ($db_found) {
    $SQL="select * from cs5248";
    $result=mysql_query($SQL);
    while($db_field=mysql_fetch_assoc($result)){
	$patt=$db_field['path'];
	    $uploadfile = $patt; 
    }		
}
else {
	print "Database NOT Found";
}

$streamlets_uploaded = "";
if ($handle = opendir($uploadfile)) {
	
	$files_in_dir = @scandir($uploadfile);
        natsort($files_in_dir);
	$sub_dir_array = array("_output1","_output2","_output3","main_playlist","playlists_1","playlists_2","playlists_3");
        while(list($key, $val) = each($files_in_dir)){
        	if($val !== "." && $val !== ".." && !in_array($val,$sub_dir_array)) {
			$streamlets_uploaded .= $val."\n";
                }
        }
        closedir($handle);
	echo $streamlets_uploaded;
}

?>
