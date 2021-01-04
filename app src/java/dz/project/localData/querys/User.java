package dz.project.localData.querys;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import dz.project.localData.LocalDataManager;

public class User {
    public static final String TABLE_NAME = "user";

    private static final String COLUMN_UID = "uid";
    private static final String COLUMN_USER_PASSWORD = "user_password";
    private static final String COLUMN_USER_STATUS = "user_status";

    private String uid;
    private String password;
    private String status; // "deliverer" or "client"

    // Create table SQL query
    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" +
            COLUMN_UID + " String PRIMARY KEY," +
            COLUMN_USER_PASSWORD + " TEXT," +
            COLUMN_USER_STATUS + " TEXT" +
            ")";

    public User() {}

    public User(String _uid, String _password, String _status) {
        uid = _uid;
        password = _password;
        status = _status;
    }

    public String getUid() {
        return uid;
    }

    public String getPassword() {
        return password;
    }

    public String getStatus() {
        return status;
    }

    public void setUid(String _uid) {
        uid = _uid;
    }

    public void setPassword(String _userName) {
        password = _userName;
    }

    public void setStatus(String _userStatus) {
        status = _userStatus;
    }


    //----------------------------------------------------------------------------------------------
    public static long insertUser(User user) {
        SQLiteDatabase dataBase = LocalDataManager.getDataBase();

        // data
        ContentValues values = new ContentValues();
        values.put(User.COLUMN_UID, user.getUid());
        values.put(User.COLUMN_USER_PASSWORD, user.getPassword());
        values.put(User.COLUMN_USER_STATUS, user.getStatus());

        // insert row
        long id = dataBase.insert(User.TABLE_NAME, null, values);

        // close db connection
        dataBase.close();

        // return newly inserted row id
        return id;
    }

    public static User getUser() {
        SQLiteDatabase dataBase = LocalDataManager.getDataBase();

        // get user data
        String selectUserQuery = "SELECT * FROM " + User.TABLE_NAME;
        Cursor cursor = dataBase.rawQuery(selectUserQuery, null);

        // chek if user not created yet
        if (cursor.getCount() == 1) {
            if (cursor.moveToFirst()) {

                // prepare user object
                User user = new User(
                        cursor.getString(cursor.getColumnIndex(User.COLUMN_UID)),
                        cursor.getString(cursor.getColumnIndex(User.COLUMN_USER_PASSWORD)),
                        cursor.getString(cursor.getColumnIndex(User.COLUMN_USER_STATUS))
                );

                // close the db connection
                cursor.close();

                return user;
            }
        }

        return null;
    }

    public static int updateUser(User user) {
        SQLiteDatabase dataBase = LocalDataManager.getDataBase();

        ContentValues values = new ContentValues();
        values.put(User.COLUMN_UID, user.getUid());
        values.put(User.COLUMN_USER_PASSWORD, user.getPassword());
        values.put(User.COLUMN_USER_STATUS, user.getStatus());

        // updating row
        return dataBase.update(
                User.TABLE_NAME,
                values,
                User.COLUMN_UID + " = ?",
                new String[]{
                        String.valueOf(user.getUid())
                }
        );
    }

    public static void deleteUser() {
        SQLiteDatabase dataBase = LocalDataManager.getDataBase();

        dataBase.delete(TABLE_NAME, null, null);

        dataBase.close();
    }

}
