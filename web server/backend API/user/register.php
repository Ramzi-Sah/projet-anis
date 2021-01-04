<?php
    include("../config.php");
    include("../database/dbConnect.php");
    include("../common/Loging.php");

    $user_name = "null";
    $user_email = "null";
    $user_phone_number = "null";
    $user_password = "null";

    //--------------------------------------------------------------------------
    // check user name
    if (isset($_POST['user_name']) AND !empty($_POST['user_name'])) {
        if (preg_match("/^[a-zA-Z0-9 _]*$/", $_POST['user_name'])) {
            if (strlen($_POST['user_name']) >= 3 AND strlen($_POST['user_name']) <= 20) {
                // user name should be ok
                $user_name = htmlspecialchars($_POST['user_name']);

                // check if alredy exists
                $sql = "SELECT COUNT(*) FROM users WHERE user_name = '$user_name';";
                $response = $db->prepare($sql);
                $response->execute();
                $response = $response->fetch();

                if ($response[0] > 0) {
                    $response = array("error"=>"USER_NAME_ALREDY_EXISTS");
                    die(json_encode($response));
                };
            } else {
                $response = array("error"=>"USER_NAME_LENGTH_NOT_VALID");
                die(json_encode($response));
            };
        } else {
            $response = array("error"=>"USER_NAME_NOT_VALID");
            die(json_encode($response));
        };
    } else {
        $response = array("error"=>"USER_NAME_NOT_SET");
        die(json_encode($response));
    };

    // check user password
    include("../common/passwordHash.php");
    if (isset($_POST['user_password']) AND !empty($_POST['user_password'])) {
        if (preg_match("/^[a-zA-Z0-9._!?]*$/", $_POST['user_password'])) {
            if (strlen($_POST['user_password']) > 5) {
                // user password should be ok
                $user_password = passwordHash(htmlspecialchars($_POST['user_password']), PASSWORD_DEFAULT);

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

    // check user email
    if (isset($_POST['user_email']) AND !empty($_POST['user_email'])) {
        if (filter_var($_POST['user_email'], FILTER_VALIDATE_EMAIL)) {
            // user email should be ok
            $user_email = htmlspecialchars($_POST['user_email']);

            // check if alredy exists
            $sql = "SELECT COUNT(*) FROM users WHERE user_email = '$user_email';";
            $response = $db->prepare($sql);
            $response->execute();
            $response = $response->fetch();

            if ($response[0] > 0) {
                $response = array("error"=>"USER_EMAIL_ALREDY_EXISTS");
                die(json_encode($response));
            };
        } else {
            $response = array("error"=>"USER_EMAIL_NOT_VALID");
            die(json_encode($response));
        };
    } else {
        // $response = array("error"=>"USER_EMAIL_NOT_SET");
        // die(json_encode($response));

        $user_email = "NO_EMAIL";
    };

    // check user phone number
    if (isset($_POST['user_phone_number']) AND !empty($_POST['user_phone_number'])) {
        if (preg_match("/^[0-9]*$/", $_POST['user_phone_number'])) {
            if (strlen($_POST['user_phone_number']) == 10) {
                $phone_number_vendor = substr($_POST['user_data_input'], 0, 2);
                if ($phone_number_vendor == "05" OR $phone_number_vendor == "06" OR $phone_number_vendor == "07") {
                    // user phone number should be ok
                    $user_phone_number = htmlspecialchars($_POST['user_phone_number']);

                    // check if alredy exists
                    $sql = "SELECT COUNT(*) FROM users WHERE user_phone_number = '$user_phone_number';";
                    $response = $db->prepare($sql);
                    $response->execute();
                    $response = $response->fetch();

                    if ($response[0] > 0) {
                        $response = array("error"=>"USER_PHONE_ALREDY_EXISTS");
                        die(json_encode($response));
                    };
                } else {
                    $response = array("error"=>"USER_PHONE_NUMBER_VENDOR_NOT_VALID");
                    die(json_encode($response));
                }
            } else {
                $response = array("error"=>"USER_PHONE_NUMBER_LENGTH_NOT_VALID");
                die(json_encode($response));
            }
        } else {
            $response = array("error"=>"USER_PHONE_NUMBER_NOT_VALID");
            die(json_encode($response));
        };
    } else {
        // $response = array("error"=>"USER_PHONE_NUMBER_NOT_SET");
        // die(json_encode($response));

        $user_phone_number = "NO_PHONE_NUMBER";
    };

    //--------------------------------------------------------------------------
    // generate uid
    include("../common/UIDHandler.php");
    $user_uid = UIDHandler::generateUID();

    // date created
    $user_logs = date("Y-m-d H:i:s", strtotime("now"));

    //--------------------------------------------------------------------------
    // insert data
    $sql = "INSERT INTO users (uid, user_name, user_email, user_password, user_phone_number, user_logs) VALUES ('$user_uid', '$user_name', '$user_email', '$user_password', '$user_phone_number', '$user_logs');";
    if ($db->query($sql) == TRUE) {
        // log action
        $good = Loging::logToDataBase($user_uid, "user created.");

        $response = array("error"=>"SUCCESS", "UID"=>$user_uid);
        die(json_encode($response));
    } else {
        $response = array("error"=>"DATABASE_INSERT_ERROR");
        die(json_encode($response));
    }


?>
