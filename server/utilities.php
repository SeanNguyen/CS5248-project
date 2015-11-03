<?php

function getFileExtension($filepath)
{
    preg_match('/[^?]*/', $filepath, $matches);
    $string = $matches[0];

    $pattern = preg_split('/\./', $string, -1, PREG_SPLIT_OFFSET_CAPTURE);

    if(count($pattern) > 1)
    {
        $filenamepart = $pattern[count($pattern)-1][0];
        preg_match('/[^?]*/', $filenamepart, $matches);
        return strtolower($matches[0]);
    }
}

//get name without segment number
function getVideoName($fullName) {
	$pos = strrpos($fullName, "_");
	return substr($fullName, 0, $pos); 
}

//get the segment number
function getSegmentNumber($fullName) {
	$pos = strrpos($fullName, "_");
	return substr($fullName, $pos + 1); 
}

?>