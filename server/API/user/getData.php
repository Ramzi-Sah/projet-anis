<?php
    include("../config.php");
    include("../database/dbConnect.php");
    include("../common/Loging.php");

    $user_uid = "null";
    $user_password = "null";

    $user_data = "none"; // "name", "email", "phone", "all"

    //--------------------------------------------------------------------------
    // check uid
    if (isset($_POST['user_uid']) AND !empty($_POST['user_uid'])) {
        include("../common/UIDHandler.php");
        if (UIDHandler::checkUID($_POST['user_uid'])) {
            // should be a valid uid
            $user_uid = htmlspecialchars($_POST['user_uid']);

            // check if uid exists
            $sql = "SELECT COUNT(*) FROM users WHERE uid = '$user_uid';";
            $response = $db->prepare($sql);
            $response->execute();

            if ($response->fetch()[0] == 0) {
                $response = array("error"=>"USER_UID_DOES_NOT_EXIST");
                die(json_encode($response));
            };

        } else {
            $response = array("error"=>"USER_UID_NOT_VALID");
            die(json_encode($response));
        };
    } else {
        $response = array("error"=>"USER_UID_NOT_SET");
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
                $sql = "SELECT user_password FROM users WHERE uid = '$user_uid';";
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
    class UserData {

        public static function getUserName($db, $user_uid) {
            $sql = "SELECT user_name FROM users WHERE uid = '$user_uid';";
            $response = $db->prepare($sql);
            $response->execute();
            $user_name = $response->fetch()[0];

            return array("error"=>"SUCCESS", "user_name"=>$user_name);
        }

        public static function getEmail($db, $user_uid) {
            $sql = "SELECT user_email FROM users WHERE uid = '$user_uid';";
            $response = $db->prepare($sql);
            $response->execute();
            $user_email = $response->fetch()[0];

            return array("error"=>"SUCCESS", "user_email"=>$user_email);
        }

        public static function getPhone($db, $user_uid) {
            $sql = "SELECT user_phone_number FROM users WHERE uid = '$user_uid';";
            $response = $db->prepare($sql);
            $response->execute();
            $user_phone = $response->fetch()[0];

            return array("error"=>"SUCCESS", "user_phone"=>$user_phone);
        }

        public static function getVerifyed($db, $user_uid) {
            $sql = "SELECT email_verified, phone_number_verified FROM users WHERE uid = '$user_uid';";
            $response = $db->prepare($sql);
            $response->execute();
            $user_phone_email_verifyed = $response->fetch();

            return array("error"=>"SUCCESS", "user_email_verified"=>$user_phone_email_verifyed[0], "user_phone_verified"=>$user_phone_email_verifyed[1]);
        }

        public static function getAll($db, $user_uid) {
            $sql = "SELECT user_name, user_email, user_phone_number, email_verified, phone_number_verified FROM users WHERE uid = '$user_uid';";
            $response = $db->prepare($sql);
            $response->execute();
            $user_all = $response->fetch();

            return array(
                "error"=>"SUCCESS",
                "user_name"=>$user_all[0],
                "user_email"=>$user_all[1],
                "user_phone"=>$user_all[2],
                "user_email_verified"=>$user_all[3],
                "user_phone_verified"=>$user_all[4]
            );
        }
    };

    if (isset($_POST['user_data']) AND !empty($_POST['user_data'])) {
        $user_data = htmlspecialchars($_POST['user_data']);
        if ($user_data == "name") {
            $response = UserData::getUserName($db, $user_uid);
            Loging::logToDataBase($user_uid, "asked for user name.");

            die(json_encode($response));

        } else if ($user_data == "email") {
            $response = UserData::getEmail($db, $user_uid);
            Loging::logToDataBase($user_uid, "asked for user email.");

            die(json_encode($response));

        } else if ($user_data == "phone") {
            $response = UserData::getPhone($db, $user_uid);
            Loging::logToDataBase($user_uid, "asked for user phone.");

            die(json_encode($response));

        } else if ($user_data == "verifyed") {
            $response = UserData::getVerifyed($db, $user_uid);
            Loging::logToDataBase($user_uid, "asked for user email/phone  verifyed.");

            die(json_encode($response));

        } else if ($user_data == "all") {
            $response = UserData::getAll($db, $user_uid);
            Loging::logToDataBase($user_uid, "asked for user name/email/phone.");

            die(json_encode($response));

        } else {
            $response = array("error"=>"USER_DATA_ERROR");
            die(json_encode($response));
        };
    } else {
        $response = array("error"=>"USER_DATA_NOT_SET");
        die(json_encode($response));
    };

?>
