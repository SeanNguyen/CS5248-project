<?php
error_reporting(E_ERROR);

include_once("utilities.php");
include_once("hlsGenerator.php");

set_time_limit(0);

$uploadPath = "uploads";

echo "START WATCHING\n";
while (1) {
	print(date('l jS \of F Y h:i:s A') . "\t watching... \n");
	$uploadedFiles = scandir($uploadPath);
	foreach($uploadedFiles as $newFile) {
		$ext = getFileExtension($newFile);
		if(strcmp($ext, "mp4") === 0) {
			makeHls($uploadPath . DIRECTORY_SEPARATOR . $newFile);
			print("Processed: " . $newFile . "\n");		
	    	unlink($uploadPath . DIRECTORY_SEPARATOR . $newFile); // delete file
		}
	}
	sleep(3); 
}

?>