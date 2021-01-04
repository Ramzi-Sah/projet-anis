<?php
    function passwordHash($password) {
        return hash('md5', hash('sha256', $password));
    }
?>
