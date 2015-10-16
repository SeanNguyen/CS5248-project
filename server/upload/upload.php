<?php

ini_set('upload_max_filesize', '10M');
ini_set('post_max_size', '10M');
ini_set('max_input_time', 300);
ini_set('max_execution_time', 300);

$uploaddir = './video_repo/';
$uploadfile = $uploaddir . basename($_FILES['userfile']['name']);

if (move_uploaded_file($_FILES['userfile']['tmp_name'], $uploadfile)) {
    echo "File is valid, and was successfully uploaded.";
} else {
    echo "Fail to upload the file";
}

echo '<p>Here is some more debugging info:</p>';
print_r($_FILES);

?>