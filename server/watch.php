<?php

include 'dash.php';

set_time_limit(0);

$uploadPath = "uploads";
$fileList = array();

//add all file in upload folder
$it = new RecursiveIteratorIterator(new RecursiveDirectoryIterator($uploadPath));
foreach ($it as $file) {
	array_push($fileList, $file);
}

while (1) { 
	$it = new RecursiveIteratorIterator(new RecursiveDirectoryIterator($uploadPath));
	foreach ($it as $file) {
		for($i = 0; $i < count($fileList); ++$i) {
			//if the file is new, then add it to the list and process dash
			if(strcmp($file, $fileList[$i] !== 0) {
				makeMpd($file);
				array_push($fileList, $file);
				print("Processed: " . $file);
			}
		}
	}
	print('watching...');
	sleep(3000); 
}

?>