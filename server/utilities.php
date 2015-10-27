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

?>