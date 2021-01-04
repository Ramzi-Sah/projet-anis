<?php
    class UIDHandler {
        static $UID_length = 15;
        static $UID_characters = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

        public static function generateUID() {
            $randomString = "";
            for ($i = 0; $i < self::$UID_length; $i++) {
                $randomString .= self::$UID_characters[rand(0, strlen(self::$UID_characters) - 1)];
            }
            return $randomString;
        }

        public static function checkUID($UID) {
            if (strlen($UID) == self::$UID_length) {
                // check valid uid chars
                $chars = str_split($UID);
                $gama = str_split(self::$UID_characters);
                foreach($chars as $char) {
                    if(in_array($char, $gama) == false) return false;
                };

                // should be valid
                return True;
            };

            return False;
        }
    }
?>
