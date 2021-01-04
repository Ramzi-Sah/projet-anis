<?php
    include("../config.php");
    include("../database/dbConnect.php");
    include("../common/Loging.php");

    $user_name_email = "null";
    $user_password = "null";

    //--------------------------------------------------------------------------
    // check user name or email
    $connectMethod = "none"; // "email" or name
    if (isset($_POST['user_name_email']) AND !empty($_POST['user_name_email'])) {
        if (filter_var($_POST['user_name_email'], FILTER_VALIDATE_EMAIL)) {
            $connectMethod = "email";
            $user_name_email = htmlspecialchars($_POST['user_name_email']);
        } else {
            $connectMethod = "name";

            if (preg_match("/^[a-zA-Z0-9 _]*$/", $_POST['user_name_email'])) {
                if (strlen($_POST['user_name_email']) >= 3 AND strlen($_POST['user_name_email']) <= 20) {
                    $user_name_email = htmlspecialchars($_POST['user_name_email']);
                } else {
                    $response = array("error"=>"USER_NAME_LENGTH_NOT_VALID");
                    die(json_encode($response));
                };
            } else {
                $response = array("error"=>"USER_NAME_NOT_VALID");
                die(json_encode($response));
            };
        };
    } else {
        $response = array("error"=>"USER_NAME_EMAIL_NOT_SET");
        die(json_encode($response));
    };

    // check if failing
    if ($connectMethod == "none") {
        $response = array("error"=>"CONNECTION_METHODE_ERROR");
        die(json_encode($response));
    };

    // check if client exists
    if ($connectMethod == "email") {
        $sql = "SELECT COUNT(*) FROM users WHERE user_email = '$user_name_email';";
    } else if ($connectMethod == "name") {
        $sql = "SELECT COUNT(*) FROM users WHERE user_name = '$user_name_email';";
    };

    $response = $db->prepare($sql);
    $response->execute();

    if ($response->fetch()[0] == 0) {
        if ($connectMethod == "email") {
            $response = array("error"=>"USER_EMAIL_DOES_NOT_EXIST");
        } else if ($connectMethod == "name") {
            $response = array("error"=>"USER_NAME_DOES_NOT_EXIST");
        };
        die(json_encode($response));
    };

    //--------------------------------------------------------------------------
    // check user password
    include("../common/passwordHash.php");
    if (isset($_POST['user_password']) AND !empty($_POST['user_password'])) {
        if (preg_match("/^[a-zA-Z0-9._!?]*$/", $_POST['user_password'])) {
            if (strlen($_POST['user_password']) > 5) {
                // user password should be ok
                $user_password = passwordHash(htmlspecialchars($_POST['user_password']), PASSWORD_DEFAULT);

                // check with remote password
                if ($connectMethod == "email") {
                    $sql = "SELECT user_password FROM users WHERE user_email = '$user_name_email';";
                } else if ($connectMethod == "name") {
                    $sql = "SELECT user_password FROM users WHERE user_name = '$user_name_email';";
                };

                $response = $db->prepare($sql);
                $response->execute();
                $response = $response->fetch();

                if ($response[0] != $user_password) {
                    $response = array("error"=>"USER_WRONG_PASSWORD");
                    die(json_encode($response));
                };
            } else {
                $response = array("error"=>"USER_PASSWORD_LENGTH_NOT_VALID");
                die(json_encode($response));
            };
        } else {
            $response = array("error"=>"USER_PASSWORD_NOT_VALID");
            die(json_encode($response));
        };
    } else {
        $response = array("error"=>"USER_PASSWORD_NOT_SET");
        die(json_encode($response));
    };

    //--------------------------------------------------------------------------
    if ($connectMethod == "email") {
        $sql = "SELECT uid FROM users WHERE user_email = '$user_name_email';";
    } else if ($connectMethod == "name") {
        $sql = "SELECT uid FROM users WHERE user_name = '$user_name_email';";
    };

    $response = $db->prepare($sql);
    $response->execute();
    $user_uid = $response->fetch()[0];

    //--------------------------------------------------------------------------
    // log action
    Loging::logToDataBase($user_uid, "asked for user UID.");

    //--------------------------------------------------------------------------
    // respond with uid
    $response = array("error"=>"SUCCESS", "UID"=>$user_uid);
    die(json_encode($response));
?>
