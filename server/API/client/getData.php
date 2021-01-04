<?php
    include("../config.php");
    include("../database/dbConnect.php");
    include("../common/Loging.php");

    $user_uid = "null";
    $user_password = "null";

    $user_data = "none"; // "client_livreurs", "client_commandes"

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
    // check if client registred on client table
    // check if uid exists
    $sql = "SELECT COUNT(*) FROM clients WHERE uid = '$user_uid';";
    $response = $db->prepare($sql);
    $response->execute();

    if ($response->fetch()[0] == 0) {
        // get user name
        $sql = "SELECT user_name FROM users WHERE uid = '$user_uid';";
        $response = $db->prepare($sql);
        $response->execute();
        $user_name = $response->fetch()[0];

        // client Livreurs
        $client_livreurs = json_encode(array());

        // create user
        $sql ="INSERT INTO clients (uid, user_name, client_livreurs) VALUES ('$user_uid', '$user_name', '$client_livreurs');";
        $db->query($sql); // should be true
    };

    //--------------------------------------------------------------------------
    class ClientGetData {

        public static function getLivreurs($db, $user_uid) {
            // add lvreur uid to client_livreurs
            $sql = "SELECT client_livreurs FROM clients WHERE uid = '$user_uid';";
            $response = $db->prepare($sql);
            $response->execute();
            $client_livreurs = $response->fetch()[0];
            if ($client_livreurs == NULL) {
                $client_livreurs = json_encode(array());
                $sql = "UPDATE clients SET client_livreurs='$client_livreurs' WHERE uid = '$user_uid';";
                $db->query($sql); // should be true
            }

            return array("error"=>"SUCCESS", "client_livreurs"=>$client_livreurs);
        }

        public static function getLivreursUID($db, $user_uid, $livreur_uid) {
            // check if livreur uid exists
            $sql = "SELECT COUNT(*) FROM livreurs WHERE livreur_uid = '$livreur_uid';";
            $response = $db->prepare($sql);
            $response->execute();

            if ($response->fetch()[0] == 0) {
                // update client livreur
                $sql = "SELECT client_livreurs FROM clients WHERE uid = '$user_uid';";
                $response = $db->prepare($sql);
                $response->execute();
                $client_livreurs = $response->fetch()[0];

                $client_livreurs = json_decode($client_livreurs);

                if (($key = array_search($livreur_uid, $client_livreurs)) !== false) {
                    unset($client_livreurs[$key]);
                    $client_livreurs = array_values($client_livreurs); // reorder array
                }

                $client_livreurs = json_encode($client_livreurs);

                $sql = "UPDATE clients SET client_livreurs='$client_livreurs' WHERE uid = '$user_uid';";
                $db->query($sql); // should be true

                $response = array("error"=>"LIVREUR_UID_DOES_NOT_EXIST");
                die(json_encode($response));
            };

            $sql = "SELECT uid FROM livreurs WHERE livreur_uid = '$livreur_uid';";
            $response = $db->prepare($sql);
            $response->execute();
            $response = $response->fetch()[0];

            return array("error"=>"SUCCESS", "uid"=>$response);
        }

        public static function getLivreursData($db, $user_uid, $livreur_uid) {
            $user_uid_livreur = ClientGetData::getLivreursUID($db, $user_uid, $livreur_uid);

            // check livreur user not deleted
            $sql = "SELECT COUNT(*) FROM users WHERE uid ='" . $user_uid_livreur["uid"] . "';";
            $response = $db->prepare($sql);
            $response->execute();

            if ($response->fetch()[0] == 0) {
                // TODO: may be delete livreur ?
                return array("error"=>"LIVREUR_USER_DOES_NOT_EXIST");
            }

            $sql = "SELECT user_name, user_email, user_phone_number FROM users WHERE uid = '" . $user_uid_livreur["uid"] . "';";
            $response = $db->prepare($sql);
            $response->execute();
            $user_all = $response->fetch();

            $user_all = array_merge($user_uid_livreur,  array(
                "user_name"=>$user_all[0],
                "user_email"=>$user_all[1],
                "user_phone"=>$user_all[2]
            ));

            // get livreur data
            $sql = "SELECT livreur_status FROM livreurs WHERE livreur_uid = '$livreur_uid';";
            $response = $db->prepare($sql);
            $response->execute();
            $response = $response->fetch();

            $response = array_merge($user_all,  array(
                "livreur_status"=>$response[0]
            ));

            return $response;
        }


    };

    if (isset($_POST['user_data']) AND !empty($_POST['user_data'])) {
        if (isset($_POST['user_data_input']) AND !empty($_POST['user_data_input'])) {
            $user_data = htmlspecialchars($_POST['user_data']);

            if ($user_data == "client_livreurs") {
                $response = ClientGetData::getLivreurs($db, $user_uid);
                Loging::logToDataBase($user_uid, "asked for client livreurs.");

                die(json_encode($response));

            } else if ($user_data == "livreur_uid") {
                if (preg_match("/^[a-zA-Z0-9 .]*$/", $_POST['user_data_input'])) {
                    $livreur_uid = htmlspecialchars($_POST['user_data_input']);
                    $response = ClientGetData::getLivreursUID($db, $user_uid, $livreur_uid);
                    Loging::logToDataBase($user_uid, "asked for livreur (" . $user_uid . ") uid.");

                    die(json_encode($response));
                } else {
                    $response = array("error"=>"USER_DATA_INPUT_NOT_VALID");
                    die(json_encode($response));
                };

            } else if ($user_data == "livreur_user_info") {
                if (preg_match("/^[a-zA-Z0-9 .]*$/", $_POST['user_data_input'])) {
                    $livreur_uid = htmlspecialchars($_POST['user_data_input']);
                    $response = ClientGetData::getLivreursData($db, $user_uid, $livreur_uid);
                    Loging::logToDataBase($user_uid, "asked for livreur (" . $livreur_uid . ") user info.");

                    die(json_encode($response));
                } else {
                    $response = array("error"=>"USER_DATA_INPUT_NOT_VALID");
                    die(json_encode($response));
                };

            // } else if ($user_data == "client_livreurs") {

            } else {
                $response = array("error"=>"USER_DATA_ERROR");
                die(json_encode($response));
            };
        } else {
            $response = array("error"=>"USER_DATA_INPUT_NOT_SET");
            die(json_encode($response));
        }
    } else {
        $response = array("error"=>"USER_DATA_NOT_SET");
        die(json_encode($response));
    };

?>
