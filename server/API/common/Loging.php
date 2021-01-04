<?php
    class Loging {
        /* // not used any more
        public static function logFormat($message) {
            return date("Y-m-d H:i:s", strtotime("now")) . " (" . $_SERVER['REMOTE_ADDR'] . ") " . $message . "\n";
        }
        */

        public static function logToDataBase($user_uid, $log) {
            include("../database/dbConnect.php");

            $sql = "SELECT user_name FROM users WHERE uid = '$user_uid';";
            $response = $db->prepare($sql);
            $response->execute();
            $user_name = $response->fetch()[0];

            $user_ip = $_SERVER['REMOTE_ADDR'];
            $log_date = date("Y-m-d H:i:s", strtotime("now"));
            $user_agent = $_SERVER["HTTP_USER_AGENT"];

            // update user logs
            $sql = "INSERT INTO Logs (uid, user_name, user_ip, user_agent, log_date, log_data) VALUES ('$user_uid', '$user_name', '$user_ip', '$user_agent', '$log_date', '$log');";
            if ($db->query($sql) == TRUE) {
                $response = array("error"=>"LOG_SUCCESS");

                return True;
            } else {
                return False;
            }
        }

    }
?>
