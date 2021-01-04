<?php
    include("../config.php");
    include("../database/dbConnect.php");
    include("../common/Loging.php");

    $user_uid = "null";
    $user_password = "null";

    $user_data = "none"; // "ADD_EMAIL", "ADD_PHONE_NUMBER", "VALIDATE_EMAIL" or "VALIDATE_PHONE_NUMBER"

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
    class UserAddInfo {

        public static function addEmail($db, $user_uid, $user_email) {
            // check if email alredy exists
            $sql = "SELECT COUNT(*) FROM users WHERE user_email = '$user_email';";
            $response = $db->prepare($sql);
            $response->execute();
            $response = $response->fetch();

            if ($response[0] > 0) {
                Loging::logToDataBase($user_uid, "tryed to register alredy existing email (" . $user_email . ").");
                $response = array("error"=>"USER_EMAIL_ALREDY_EXISTS");
                die(json_encode($response));
            };

            // register email
            $sql = "UPDATE users SET user_email='$user_email' WHERE uid = '$user_uid';";

            if ($db->query($sql) == TRUE) {
                $sql = "UPDATE users SET email_verified='0' WHERE uid = '$user_uid';";
                $db->query($sql); // should be true

                // reset pin
                $sql = "UPDATE users SET email_verification_code='00000000' WHERE uid = '$user_uid';";
                $db->query($sql); // should be true

                Loging::logToDataBase($user_uid, "registred email (" . $user_email . ").");
                return array("error"=>"SUCCESS");
            } else {
                return array("error"=>"DATABASE_INSERT_ERROR");
            }
        }

        public static function emailValidationRequest($db, $user_uid) {
            // get user email
            $sql = "SELECT user_email FROM users WHERE uid = '$user_uid';";
            $response = $db->prepare($sql);
            $response->execute();
            $user_email = $response->fetch()[0];

            if ($user_email == "NO_EMAIL") {
                return array("error"=>"USER_EMAIL_NOT_REGISTRED");
            };

            // $pin = "";
            // $pin_characters = "0123456789";
            // for ($i = 0; $i < 4; $i++) {
            //     $pin .= $pin_characters[rand(0, 9)];
            // }
            $pin = "1234";

            $sql = "UPDATE users SET email_verification_code='$pin' WHERE uid = '$user_uid';";
            $db->query($sql); // should be true

            // TODO: should send email with pin to user
            if (True) {
                Loging::logToDataBase($user_uid, "email validation pin: " . $pin . " sended.");
                return array("error"=>"SUCCESS");
            } else {
                return array("error"=>"USER_SEND_PIN_ERROR");
            }

        }

        public static function validateEmail($db, $user_uid, $pin) {
            // get user email
            $sql = "SELECT user_email FROM users WHERE uid = '$user_uid';";
            $response = $db->prepare($sql);
            $response->execute();
            $user_email = $response->fetch()[0];

            if ($user_email == "NO_EMAIL") {
                return array("error"=>"USER_EMAIL_NOT_REGISTRED");
            };

            // check pin
            $sql = "SELECT email_verification_code FROM users WHERE uid = '$user_uid';";
            $response = $db->prepare($sql);
            $response->execute();
            $user_pin = $response->fetch()[0];

            if ($pin != $user_pin) {
                Loging::logToDataBase($user_uid, "tryed invalid pin (" . $pin . ").");
                return array("error"=>"USER_PIN_NOT_VALID");
            };

            // validate email
            $sql = "UPDATE users SET email_verified='1' WHERE uid = '$user_uid';";
            if ($db->query($sql) == TRUE) {
                Loging::logToDataBase($user_uid, "email (" . $user_email . ") validated.");
                return array("error"=>"SUCCESS");
            } else {
                return array("error"=>"DATABASE_INSERT_ERROR");
            }
        }

        public static function addPhone($db, $user_uid, $user_phone) {
            // check if alredy exists
            $sql = "SELECT COUNT(*) FROM users WHERE user_phone_number = '$user_phone';";
            $response = $db->prepare($sql);
            $response->execute();
            $response = $response->fetch();

            if ($response[0] > 0) {
                Loging::logToDataBase($user_uid, "tryed to register alredy existing phone number email (" . $user_phone . ").");
                $response = array("error"=>"USER_PHONE_ALREDY_EXISTS");
                die(json_encode($response));
            };

            // register phone number
            $sql = "UPDATE users SET user_phone_number='$user_phone' WHERE uid = '$user_uid';";

            if ($db->query($sql) == TRUE) {
                $sql = "UPDATE users SET phone_number_verified='0' WHERE uid = '$user_uid';";
                $db->query($sql); // should be true

                // reset pin
                $sql = "UPDATE users SET phone_number_verification_code='00000000' WHERE uid = '$user_uid';";
                $db->query($sql); // should be true

                Loging::logToDataBase($user_uid, "registred phone number (" . $user_phone . ").");
                return array("error"=>"SUCCESS");
            } else {
                return array("error"=>"DATABASE_INSERT_ERROR");
            }
        }

        public static function phoneValidationRequest($db, $user_uid) {
            include("../config.php");

            // get user email
            $sql = "SELECT user_phone_number FROM users WHERE uid = '$user_uid';";
            $response = $db->prepare($sql);
            $response->execute();
            $user_email = $response->fetch()[0];

            if ($user_email == "NO_PHONE_NUMBER") {
                return array("error"=>"USER_PHONE_NOT_REGISTRED");
            };

            // calculate cooldown time
            $sql = "SELECT latest_phone_msg_time FROM users WHERE uid = '$user_uid';";
            $response = $db->prepare($sql);
            $response->execute();
            $latest_phone_msg_time = $response->fetch()[0];
            if ($latest_phone_msg_time == NULL) {
                $latest_phone_msg_time = strtotime("-1 day");
            };

            $time_now = time();

            $cooldown_time = $config_cooldown_time;
            $time_waited = $time_now - $latest_phone_msg_time;
            $time_to_wait = $cooldown_time - $time_waited;
            if ($time_waited < $cooldown_time) {
                return array("error"=>"PHONE_PIN_MESSAGE_COOLDOWN", "time_to_wait"=>$cooldown_time - $time_waited);
            };

            $sql = "UPDATE users SET latest_phone_msg_time='$time_now' WHERE uid = '$user_uid';";
            $db->query($sql); // should be true

            // generate random pin
            // $pin = "";
            // $pin_characters = "0123456789";
            // for ($i = 0; $i < 4; $i++) {
            //     $pin .= $pin_characters[rand(0, 9)];
            // }
            $pin = "1234";

            $sql = "UPDATE users SET phone_number_verification_code='$pin' WHERE uid = '$user_uid';";
            $db->query($sql); // should be true

            // TODO: should send email with pin to user
            if (True) {
                // get today nbr of msgs
                $sql = "SELECT nbr_phone_msgs_today FROM users WHERE uid = '$user_uid';";
                $response = $db->prepare($sql);
                $response->execute();
                $nbr_phone_msgs_today = $response->fetch()[0];
                if ($nbr_phone_msgs_today == NULL) {
                    $sql = "UPDATE users SET nbr_phone_msgs_today='0' WHERE uid = '$user_uid';";
                    $db->query($sql); // should be true

                    $nbr_phone_msgs_today = 0;
                }
                $nbr_phone_msgs_today = (int)$nbr_phone_msgs_today;

                // set message date
                $sql = "SELECT latest_phone_msg_date FROM users WHERE uid = '$user_uid';";
                $response = $db->prepare($sql);
                $response->execute();
                $phone_msgs_date = $response->fetch()[0];
                if ($phone_msgs_date == NULL) {
                    $phone_msgs_date = date("Y-m-d", strtotime("-1 day"));
                    $sql = "UPDATE users SET latest_phone_msg_date='$phone_msgs_date' WHERE uid = '$user_uid';";
                    $db->query($sql); // should be true
                }

                $max_msg_day = $config_max_msg_day;
                $date_today = date("Y-m-d", strtotime("now"));
                if ($date_today == $phone_msgs_date) {
                    if ($nbr_phone_msgs_today >= $max_msg_day) {
                        return array("error"=>"MAX_PHONE_MESSAGES_TODAY_REACHED");
                    }
                } else {
                    $sql = "UPDATE users SET latest_phone_msg_date='$date_today' WHERE uid = '$user_uid';";
                    $db->query($sql); // should be true

                    $sql = "UPDATE users SET nbr_phone_msgs_today='0' WHERE uid = '$user_uid';";
                    $db->query($sql); // should be true
                    $nbr_phone_msgs_today = 0;
                }

                // increment nbr of messages today
                $nbr_phone_msgs_today = $nbr_phone_msgs_today + 1;
                $sql = "UPDATE users SET nbr_phone_msgs_today='$nbr_phone_msgs_today' WHERE uid = '$user_uid';";
                $db->query($sql); // should be true


                Loging::logToDataBase($user_uid, "phone number validation pin: " . $pin . " sended.");
                return array("error"=>"SUCCESS");
            } else {
                return array("error"=>"USER_SEND_PIN_ERROR");
            }

        }

        public static function validatePhoneNumber($db, $user_uid, $pin) {
            // get user phone number
            $sql = "SELECT user_phone_number FROM users WHERE uid = '$user_uid';";
            $response = $db->prepare($sql);
            $response->execute();
            $user_phone_number = $response->fetch()[0];

            if ($user_phone_number == "NO_PHONE_NUMBER") {
                return array("error"=>"USER_PHONE_NOT_REGISTRED");
            };

            // check pin
            $sql = "SELECT phone_number_verification_code FROM users WHERE uid = '$user_uid';";
            $response = $db->prepare($sql);
            $response->execute();
            $user_pin = $response->fetch()[0];

            if ($pin != $user_pin) {
                Loging::logToDataBase($user_uid, "tryed wrong pin (" . $pin . ").");
                return array("error"=>"USER_PIN_NOT_VALID");
            };

            // validate phone number
            $sql = "UPDATE users SET phone_number_verified='1' WHERE uid = '$user_uid';";
            if ($db->query($sql) == TRUE) {
                Loging::logToDataBase($user_uid, "phone number (" . $user_phone_number . ") validated.");
                return array("error"=>"SUCCESS");
            } else {
                return array("error"=>"DATABASE_INSERT_ERROR");
            }
        }
    };

    if (isset($_POST['user_data']) AND !empty($_POST['user_data'])) {
        if (isset($_POST['user_data_input']) AND !empty($_POST['user_data_input'])) {
            // "add_email", "validate_email", "add_phone_number", "validate_phone_number"
            $user_data = htmlspecialchars($_POST['user_data']);

            // add email
            if ($user_data == "add_email") {

                // verify email
                if (filter_var($_POST['user_data_input'], FILTER_VALIDATE_EMAIL)) {
                    // user email should be ok
                    $user_email = htmlspecialchars($_POST['user_data_input']);

                    // register user email
                    $response = UserAddInfo::addEmail($db, $user_uid, $user_email);
                    die(json_encode($response));
                } else {
                    $response = array("error"=>"USER_DATA_INPUT_NOT_VALID");
                    die(json_encode($response));
                };

            } else if ($user_data == "validate_email") {
                if (preg_match("/^[0-9]*$/", $_POST['user_data_input']) AND strlen($_POST['user_data_input']) == 4) {
                    $pin = htmlspecialchars($_POST['user_data_input']);
                    $response = UserAddInfo::validateEmail($db, $user_uid, $pin);
                    die(json_encode($response));
                } else {
                    $response = array("error"=>"USER_DATA_INPUT_NOT_VALID");
                    die(json_encode($response));
                };

            } else if ($user_data == "email_validation_request") {
                $response = UserAddInfo::emailValidationRequest($db, $user_uid);
                die(json_encode($response));

            } else if ($user_data == "add_phone_number") {
                if (preg_match("/^[0-9]*$/", $_POST['user_data_input'])) {
                    if (strlen($_POST['user_data_input']) == 10) {

                        $phone_number_vendor = substr($_POST['user_data_input'], 0, 2);
                        if ($phone_number_vendor == "05" OR $phone_number_vendor == "06" OR $phone_number_vendor == "07") {

                            // user phone number should be ok
                            $user_phone_number = htmlspecialchars($_POST['user_data_input']);

                            // register user phone number
                            $response = UserAddInfo::addPhone($db, $user_uid, $user_phone_number);
                            die(json_encode($response));

                        } else {
                            $response = array("error"=>"USER_DATA_INPUT_NOT_VALID");
                            die(json_encode($response));
                        }
                    } else {
                        $response = array("error"=>"USER_DATA_INPUT_NOT_VALID");
                        die(json_encode($response));
                    }
                } else {
                    $response = array("error"=>"USER_DATA_INPUT_NOT_VALID");
                    die(json_encode($response));
                };


            } else if ($user_data == "phone_number_validation_request") {
                $response = UserAddInfo::phoneValidationRequest($db, $user_uid);
                die(json_encode($response));
            } else if ($user_data == "validate_phone_number") {
                if (preg_match("/^[0-9]*$/", $_POST['user_data_input']) AND strlen($_POST['user_data_input']) == 4) {
                    $pin = htmlspecialchars($_POST['user_data_input']);
                    $response = UserAddInfo::validatePhoneNumber($db, $user_uid, $pin);
                    die(json_encode($response));
                } else {
                    $response = array("error"=>"USER_DATA_INPUT_NOT_VALID");
                    die(json_encode($response));
                };
            } else {
                $response = array("error"=>"USER_DATA_ERROR");
                die(json_encode($response));
            };
        } else {
            $response = array("error"=>"USER_DATA_INPUT_NOT_SET");
            die(json_encode($response));
        };
    } else {
        $response = array("error"=>"USER_DATA_NOT_SET");
        die(json_encode($response));
    };

?>
