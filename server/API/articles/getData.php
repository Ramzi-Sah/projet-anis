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
    class Articles {
        public static function getArticlesUIDList($db, $user_uid) {
            // get article uids
            $sql = "SELECT uid, title, info, prices FROM articles";
            $response = $db->prepare($sql);
            $response->execute();

            $response = $response->fetchAll();
            $articles_uids = array();
            foreach($response as $row ) {
                array_push($articles_uids, $row['uid']);
            }

            $response = array("error"=>"SUCCESS", "article_uids"=>json_encode($articles_uids));
            die(json_encode($response));
        }

        public static function getArticle($db, $user_uid, $article_uid) {
            // check if commande uid exists
            $sql = "SELECT COUNT(*) FROM articles WHERE uid = '$article_uid';";
            $response = $db->prepare($sql);
            $response->execute();

            if ($response->fetch()[0] == 0) {
                $response = array("error"=>"ARTICLE_UID_DOES_NOT_EXIST");
                die(json_encode($response));
            };

            // get data
            $sql = "SELECT title, info, prices FROM articles WHERE uid = '$article_uid';";
            $response = $db->prepare($sql);
            if ($response->execute() == TRUE) {
                // log action
                Loging::logToDataBase($user_uid, "get article (" . $article_uid . ") data.");

                $commande_data = $response->fetch();
                $response = array("error"=>"SUCCESS", "title"=>$commande_data[0], "info"=>$commande_data[1], "prices"=>$commande_data[2]);
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
        if ($user_data == "get_articles_uid_list") {
            $response = Articles::getArticlesUIDList($db, $user_uid);
            die(json_encode($response));
        } else if ($user_data == "get_article_data") {
            if (isset($_POST['user_data_input']) AND !empty($_POST['user_data_input'])) {
                if (preg_match("/^[a-zA-Z0-9 .]*$/", $_POST['user_data_input'])) {
                    $article_uid = htmlspecialchars($_POST['user_data_input']);
                    $response = Articles::getArticle($db, $user_uid, $article_uid);

                    die(json_encode($response));
                } else {
                    $response = array("error"=>"ARTICLE_UID_INPUT_NOT_VALID");
                    die(json_encode($response));
                };

            } else {
                $response = array("error"=>"USER_DATA_INPUT_NOT_SET");
                die(json_encode($response));
            }

        } else {
            $response = array("error"=>"USER_DATA_ERROR");
            die(json_encode($response));
        };
    } else {
        $response = array("error"=>"USER_DATA_NOT_SET");
        die(json_encode($response));
    };

?>
