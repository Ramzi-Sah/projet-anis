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
    class Commandes {

        public static function getCommandes($db, $user_uid) {
            $sql = "SELECT uid FROM commandes WHERE client_user_uid = '$user_uid';";
            $response = $db->prepare($sql);
            $response->execute();

            $response = $response->fetchAll();
            $commandes_uids = array();
            foreach($response as $row ) {
                array_push($commandes_uids, $row['uid']);
            }

            Loging::logToDataBase($user_uid, "recover client commandes list.");

            $response = array("error"=>"SUCCESS", "client_commandes_uids"=>json_encode($commandes_uids));
            die(json_encode($response));
        }

        public static function addCommande($db, $user_uid) {
            // generate uid
            $commande_uid = UIDHandler::generateUID();
            $date_created = date("Y-m-d H:i:s", strtotime("now"));
            $livreur_user_uid = "NO_LIVREUR_ASSIGNED";
            $commande_status = "NOT_CONFIRMED";

            // insert data
            $sql = "INSERT INTO commandes (uid, date_created, client_user_uid, livreur_uid, status) VALUES ('$commande_uid', '$date_created', '$user_uid', '$livreur_user_uid', '$commande_status');";
            if ($db->query($sql) == TRUE) {
                // log action
                Loging::logToDataBase($user_uid, "created commande (" . $commande_uid . ").");

                $response = array("error"=>"SUCCESS", "commande_uid"=>$commande_uid);
                die(json_encode($response));
            } else {
                $response = array("error"=>"DATABASE_INSERT_ERROR");
                die(json_encode($response));
            }
        }

        public static function cancelCommande($db, $user_uid, $commande_uid) {
            // check if commande uid exists
            $sql = "SELECT COUNT(*) FROM commandes WHERE uid = '$commande_uid';";
            $response = $db->prepare($sql);
            $response->execute();

            if ($response->fetch()[0] == 0) {
                $response = array("error"=>"COMMANDE_UID_DOES_NOT_EXIST");
                die(json_encode($response));
            };

            // check if commande of user
            $sql = "SELECT client_user_uid FROM commandes WHERE uid = '$commande_uid';";
            $response = $db->prepare($sql);
            $response->execute();

            if ($user_uid != $response->fetch()[0]) {
                $response = array("error"=>"NOT_YOUR_COMMANDE");
                die(json_encode($response));
            };

            // delete commande
            $sql = "DELETE FROM commandes WHERE uid='$commande_uid';";
            if ($db->query($sql) == TRUE) {
                // log action
                Loging::logToDataBase($user_uid, "commande \"" . $commande_uid . "\" deleted.");

                $response = array("error"=>"SUCCESS");
                die(json_encode($response));
            } else {
                $response = array("error"=>"DATABASE_INSERT_ERROR");
                die(json_encode($response));
            }
        }

        public static function setLivreur($db, $user_uid, $commande_uid, $livreur_uid) {
            // check if commande uid exists
            $sql = "SELECT COUNT(*) FROM commandes WHERE uid = '$commande_uid';";
            $response = $db->prepare($sql);
            $response->execute();

            if ($response->fetch()[0] == 0) {
                $response = array("error"=>"COMMANDE_UID_DOES_NOT_EXIST");
                die(json_encode($response));
            };

            // check if commande of user
            $sql = "SELECT client_user_uid FROM commandes WHERE uid = '$commande_uid';";
            $response = $db->prepare($sql);
            $response->execute();

            if ($user_uid != $response->fetch()[0]) {
                $response = array("error"=>"NOT_YOUR_COMMANDE");
                die(json_encode($response));
            };

            // check if livreur uid exists
            $sql = "SELECT COUNT(*) FROM livreurs WHERE livreur_uid = '$livreur_uid';";
            $response = $db->prepare($sql);
            $response->execute();

            if ($response->fetch()[0] == 0) {
                $response = array("error"=>"LIVREUR_UID_DOES_NOT_EXIST");
                die(json_encode($response));
            };

            // update data
            $sql = "UPDATE commandes SET livreur_uid='$livreur_uid' WHERE uid = '$commande_uid';";
            if ($db->query($sql) == TRUE) {
                // notify livreur
                $notification_uid = UIDHandler::generateUID();
                $sql = "UPDATE livreurs SET new_commande_notify='$notification_uid' WHERE livreur_uid = '$livreur_uid';";
                $db->query($sql); // should be true

                // log action
                Loging::logToDataBase($user_uid, "set commande (" . $commande_uid . ") livreur " . $livreur_uid . ".");

                $response = array("error"=>"SUCCESS");
                die(json_encode($response));
            } else {
                $response = array("error"=>"DATABASE_INSERT_ERROR");
                die(json_encode($response));
            }
        }

        public static function setContent($db, $user_uid, $commande_uid, $articles, $quentities) {
            // check if commande uid exists
            $sql = "SELECT COUNT(*) FROM commandes WHERE uid = '$commande_uid';";
            $response = $db->prepare($sql);
            $response->execute();

            if ($response->fetch()[0] == 0) {
                $response = array("error"=>"COMMANDE_UID_DOES_NOT_EXIST");
                die(json_encode($response));
            };

            // check if commande of user
            $sql = "SELECT client_user_uid FROM commandes WHERE uid = '$commande_uid';";
            $response = $db->prepare($sql);
            $response->execute();

            if ($user_uid != $response->fetch()[0]) {
                $response = array("error"=>"NOT_YOUR_COMMANDE");
                die(json_encode($response));
            };

            // check if livreur uid exists
            $articles = json_encode($articles);
            $quentities = json_encode($quentities);

            // update data
            $sql = "UPDATE commandes SET articles_uids='$articles', articles_quantities='$quentities' WHERE uid = '$commande_uid';";
            if ($db->query($sql) == TRUE) {
                // log action
                Loging::logToDataBase($user_uid, "set commande (" . $commande_uid . ") articles " . $articles . " quentities " . $quentities . ".");

                $response = array("error"=>"SUCCESS");
                die(json_encode($response));
            } else {
                $response = array("error"=>"DATABASE_INSERT_ERROR");
                die(json_encode($response));
            }
        }

        public static function getCommande($db, $user_uid, $commande_uid) {
            // check if commande uid exists
            $sql = "SELECT COUNT(*) FROM commandes WHERE uid = '$commande_uid';";
            $response = $db->prepare($sql);
            $response->execute();

            if ($response->fetch()[0] == 0) {
                $response = array("error"=>"COMMANDE_UID_DOES_NOT_EXIST");
                die(json_encode($response));
            };

            // check if commande of user
            $sql = "SELECT client_user_uid FROM commandes WHERE uid = '$commande_uid';";
            $response = $db->prepare($sql);
            $response->execute();

            if ($user_uid != $response->fetch()[0]) {
                $response = array("error"=>"NOT_YOUR_COMMANDE");
                die(json_encode($response));
            };

            // get data
            $sql = "SELECT date_created, livreur_uid, articles_uids, articles_quantities, status FROM commandes WHERE uid = '$commande_uid';";
            $response = $db->prepare($sql);
            if ($response->execute() == TRUE) {
                // log action
                Loging::logToDataBase($user_uid, "get commande (" . $commande_uid . ") data.");

                $commande_data = $response->fetch();
                $response = array("error"=>"SUCCESS", "date_created"=>$commande_data[0], "livreur_uid"=>$commande_data[1], "articles_uids"=>$commande_data[2], "articles_quantities"=>$commande_data[3], "commande_status"=>$commande_data[4]);
                die(json_encode($response));
            } else {
                $response = array("error"=>"DATABASE_INSERT_ERROR");
                die(json_encode($response));
            }
        }

    };

    //--------------------------------------------------------------------------
    if (isset($_POST['user_data']) AND !empty($_POST['user_data'])) {
        $user_data = htmlspecialchars($_POST['user_data']);
        if ($user_data == "get_client_commandes") {
            $response = Commandes::getCommandes($db, $user_uid);
            die(json_encode($response));
        } else if ($user_data == "add_commande") {
            $response = Commandes::addCommande($db, $user_uid);
            die(json_encode($response));

        } else if ($user_data == "cancel_commande") {
            if (UIDHandler::checkUID($_POST['commande_uid'])) {
                $commande_uid = htmlspecialchars($_POST['commande_uid']);
                $response = Commandes::cancelCommande($db, $user_uid, $commande_uid);
                die(json_encode($response));
            };
        } else if ($user_data == "set_livreur") {
            if (isset($_POST['user_data_input']) AND !empty($_POST['user_data_input'])) {
                if (preg_match("/^[a-zA-Z0-9 .]*$/", $_POST['user_data_input'])) {

                    $livreur_uid = htmlspecialchars($_POST['user_data_input']);

                    if (UIDHandler::checkUID($_POST['commande_uid'])) {
                        $commande_uid = htmlspecialchars($_POST['commande_uid']);
                        $response = Commandes::setLivreur($db, $user_uid, $commande_uid, $livreur_uid);
                        die(json_encode($response));
                    } else {
                        $response = array("error"=>"COMMANDE_UID_INPUT_NOT_VALID");
                        die(json_encode($response));
                    };
                } else {
                    $response = array("error"=>"USER_DATA_INPUT_NOT_VALID");
                    die(json_encode($response));
                };
            } else {
                $response = array("error"=>"USER_DATA_INPUT_NOT_SET");
                die(json_encode($response));
            };
        } else if ($user_data == "set_articles") {
            if (isset($_POST['user_data_input']) AND !empty($_POST['user_data_input'])) {
                if (isset($_POST['user_data_2_input']) AND !empty($_POST['user_data_2_input'])) {

                    // valid input: {"0":"dsqsqd", "1":"jklqdkjlsdq", "2":"dsdsqdqssdq"}
                    $articles = json_decode($_POST['user_data_input'], true);
                    if ($articles == NULL) {
                        $response = array("error"=>"USER_DATA_INPUT_NOT_VALID");
                        die(json_encode($response));
                    }

                    foreach ($articles as $key => $value){
                        if (preg_match("/^[a-zA-Z0-9 .]*$/", $value)) {
                            $articles[$key] = htmlspecialchars($value);
                        } else {
                            $response = array("error"=>"COMMANDE_ARTICLE_INPUT_NOT_VALID");
                            die(json_encode($response));
                        }
                    }
                    $articles = array_values($articles);

                    // valid input: {"0":"10", "1":"0.5", "2":"20"}
                    $quentities = json_decode($_POST['user_data_2_input'], true);
                    if ($quentities == NULL) {
                        $response = array("error"=>"USER_DATA_INPUT_NOT_VALID");
                        die(json_encode($response));
                    }

                    foreach ($quentities as $key => $value){
                        if (preg_match("/^[a-zA-Z0-9 .]*$/", $value)) {
                            $quentities[$key] = htmlspecialchars($value);
                        } else {
                            $response = array("error"=>"COMMANDE_ARTICLE_QUENTITY_INPUT_NOT_VALID");
                            die(json_encode($response));
                        }
                    }
                    $quentities = array_values($quentities);

                    if (count($quentities) == count($articles)) {
                        if (UIDHandler::checkUID($_POST['commande_uid'])) {
                            $commande_uid = htmlspecialchars($_POST['commande_uid']);
                            $response = Commandes::setContent($db, $user_uid, $commande_uid, $articles, $quentities);
                            die(json_encode($response));

                        } else {
                            $response = array("error"=>"COMMANDE_UID_INPUT_NOT_VALID");
                            die(json_encode($response));
                        };
                    } else {
                        $response = array("error"=>"ARTICLES_QUENTITY_INPUT_NOT_VALID");
                        die(json_encode($response));
                    }
                } else {
                    $response = array("error"=>"USER_DATA_INPUT_NOT_SET");
                    die(json_encode($response));
                };
            } else {
                $response = array("error"=>"USER_DATA_INPUT_NOT_SET");
                die(json_encode($response));
            };
        } else  if ($user_data == "get_commande_data") {
            if (UIDHandler::checkUID($_POST['commande_uid'])) {
                $commande_uid = htmlspecialchars($_POST['commande_uid']);
                $response = Commandes::getCommande($db, $user_uid, $commande_uid);
                die(json_encode($response));

            } else {
                $response = array("error"=>"COMMANDE_UID_INPUT_NOT_VALID");
                die(json_encode($response));
            };

        // } else  if ($user_data == "client_livreurs") {

        } else {
            $response = array("error"=>"USER_DATA_ERROR");
            die(json_encode($response));
        };
    } else {
        $response = array("error"=>"USER_DATA_NOT_SET");
        die(json_encode($response));
    };

?>
