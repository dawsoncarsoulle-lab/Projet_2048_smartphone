package com.example.jeu_2048;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.util.Log;

import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Database extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "2048";

        /**
         * This table contains the matches/games played and their information
         */
        private static final String TABLE_MATCHES = "matches";

        // the id of the match
        private static final String KEY_MATCH_ID = "id";
        // when game started/initialized
        private static final String KEY_MATCH_START_TIMESTAMP = "match_start";
        // when no more moves can be played
        private static final String KEY_MATCH_END_TIMESTAMP = "match_end";
        // total points on the board/grid at over all time
        private static final String KEY_MATCH_POINTS = "points";
        private static final String KEY_MATCH_IS_RUNNING = "is_running";
        private static Database sInstance;


    /**
     * There are 2 states a game can be in the database either it is a game that is ongoing or it is over,
     * which means the initial setup is a call to the database
     */
    public void insertGameEntry(Game game) {
        var db = this.getWritableDatabase();

        String DROP_TABLE_POSTS = "DROP TABLE IF EXISTS " + TABLE_MATCHES;
        db.execSQL(DROP_TABLE_POSTS);


        String CREATE_POSTS_TABLE = "CREATE TABLE " + TABLE_MATCHES +
                "(" +
                KEY_MATCH_ID + " INTEGER PRIMARY KEY," +
                KEY_MATCH_POINTS + " INTEGER NOT NULL," +
                KEY_MATCH_START_TIMESTAMP + " INTEGER NOT NULL," +
                KEY_MATCH_END_TIMESTAMP + " INTEGER," +
                KEY_MATCH_IS_RUNNING + " INTEGER NOT NULL" +
                ")";

        db.execSQL(CREATE_POSTS_TABLE);
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_MATCH_POINTS, game.getScore());
            values.put(KEY_MATCH_START_TIMESTAMP, new Date().getTime());
            values.put(KEY_MATCH_IS_RUNNING, 0);

            db.insertOrThrow(TABLE_MATCHES, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.i("ERROR", "GameStart: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    public void updateCurrentPoints(Game game) {
        var db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_MATCH_POINTS, game.getScore());
            values.put(KEY_MATCH_IS_RUNNING, 0);

            db.update(TABLE_MATCHES, values, KEY_MATCH_IS_RUNNING + " = ?", new String[]{String.valueOf(0)});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.i("ERROR", "GameStart: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
    }


    // game end needs to know the id of the initial game
    public void updateGameStateEnd(Game game) {
        var db = this.getWritableDatabase();

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_MATCH_POINTS, game.getScore());
            values.put(KEY_MATCH_END_TIMESTAMP, new Date().getTime());
            values.put(KEY_MATCH_IS_RUNNING, 1);

            db.update(TABLE_MATCHES, values, KEY_MATCH_IS_RUNNING + " = ?", new String[]{String.valueOf(true)});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.i("ERROR", "GameEnd: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
    }


    public static synchronized Database getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new Database(context.getApplicationContext());
        }
        return sInstance;
    }
        public Database(Context context) {
            super(context, DATABASE_NAME, null, 1);
        }

        @Override
        public void onConfigure(SQLiteDatabase db) {
            super.onConfigure(db);
            db.setForeignKeyConstraintsEnabled(true); // #pragma foreign keys on
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String DROP_TABLE_POSTS = "DROP TABLE IF EXISTS " + TABLE_MATCHES;
            db.execSQL(DROP_TABLE_POSTS);

            String CREATE_POSTS_TABLE = "CREATE TABLE " + TABLE_MATCHES +
                    "(" +
                    KEY_MATCH_ID                    + "INTEGER PRIMARY KEY," +
                    KEY_MATCH_POINTS                + "INTEGER NOT NULL," +
                    KEY_MATCH_START_TIMESTAMP       + "INTEGER NOT NULL," +
                    KEY_MATCH_END_TIMESTAMP         + "INTEGER," +
                    KEY_MATCH_IS_RUNNING            + "INTEGER NOT NULL" +
                    ")";

            db.execSQL(CREATE_POSTS_TABLE);
        }

    public int getBestScore() {
        int bestScore = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        android.database.Cursor cursor = db.rawQuery("SELECT MAX(" + KEY_MATCH_POINTS + ") FROM " + TABLE_MATCHES, null);
        if (cursor.moveToFirst()) {
            bestScore = cursor.getInt(0);
        }
        cursor.close();
        return bestScore;
    }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
