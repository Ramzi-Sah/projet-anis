package dz.project.localData;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import dz.project.localData.querys.User;

public class LocalDataManager extends SQLiteOpenHelper {
    // https://www.androidhive.info/2011/11/android-sqlite-database-tutorial/

    // Database params
    private static final int DATABASE_VERSION = 6;
    private static final String DATABASE_NAME = "project_db";

    private static SQLiteOpenHelper localDataManager;

    public LocalDataManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        localDataManager = this;
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        // create User table
        db.execSQL(User.CREATE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + User.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    public static SQLiteDatabase getDataBase() {
        return localDataManager.getWritableDatabase();
    }
}
