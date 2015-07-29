package com.antrromet.insomnia.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * DB Helper class that creates the SQLIte tables that will be used in this
 * application
 *
 * @author antriksh
 */
public class DBOpenHelper extends SQLiteOpenHelper {

    public static final String NINE_GAG_TABLE_NAME = "nine_gag";
    public static final String FACEBOOK_TABLE_NAME = "facebook";
    public static final String INSTAGRAM_TABLE_NAME = "instagram";
    public static final String TWITTER_TABLE_NAME = "twitter";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_INSERTION_TIME = "insertion_time";
    public static final String COLUMN_CAPTION = "caption";
    public static final String COLUMN_TEXT = "text";
    public static final String COLUMN_IMAGE_LARGE = "image_large";
    public static final String COLUMN_IMAGE_NORMAL = "image_normal";
    public static final String COLUMN_LINK = "link";
    public static final String COLUMN_VOTES_COUNT = "votes_count";
    public static final String COLUMN_FROM = "name";
    public static final String COLUMN_TO = "t_o";
    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_PICTURE = "picture";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_STATUS_TYPE = "status_type";
    public static final String COLUMN_CREATED_TIME = "created_time";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_LIKES_COUNT = "likes_count";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_SCREEN_NAME = "screenname";
    public static final String COLUMN_FULL_NAME = "full_name";
    public static final String COLUMN_PROFILE_PICTURE = "profile_picture";
    public static final String COLUMN_STANDARD_RES_IMAGE = "standard_Res_image";
    public static final String COLUMN_PROFILE_IMAGE_URL = "profile_image_url";

    private static final String TEXT = " text, ";
    private static final String INTEGER = " integer, ";

    private static final String NINE_GAG_TABLE_BUILDER = "create table " + NINE_GAG_TABLE_NAME +
            "(" + BaseColumns._ID + " integer primary key, " + COLUMN_INSERTION_TIME + " " +
            INTEGER + COLUMN_ID + INTEGER + COLUMN_CAPTION + TEXT + COLUMN_IMAGE_LARGE + TEXT +
            COLUMN_IMAGE_NORMAL + TEXT + COLUMN_LINK + TEXT + COLUMN_VOTES_COUNT + INTEGER + " " +
            "unique(" + COLUMN_ID + ") on conflict " + "ignore);";
    private static final String NINE_GAG_TABLE_DESTROYER = "drop table " + DBProvider.DB_NAME +
            "" + "." + NINE_GAG_TABLE_NAME + ";";

    private static final String FACEBOOK_TABLE_BUILDER = "create table " + FACEBOOK_TABLE_NAME +
            "(" + BaseColumns._ID + " integer primary key, " + COLUMN_ID + INTEGER + COLUMN_FROM
            + TEXT + COLUMN_TO + TEXT + COLUMN_MESSAGE + TEXT + COLUMN_PICTURE + TEXT +
            COLUMN_LINK + TEXT + COLUMN_TYPE + TEXT + TEXT + COLUMN_STATUS_TYPE + TEXT +
            COLUMN_CREATED_TIME + INTEGER + " unique(" + COLUMN_ID + ") on conflict replace);";
    private static final String FACEBOOK_TABLE_DESTROYER = "drop table " + DBProvider.DB_NAME + "" +
            "." + FACEBOOK_TABLE_NAME + ";";

    private static final String INSTAGRAM_TABLE_BUILDER = "create table " + INSTAGRAM_TABLE_NAME +
            "(" + BaseColumns._ID + " integer primary key, " + COLUMN_ID + INTEGER + COLUMN_TYPE +
            TEXT + COLUMN_CREATED_TIME + INTEGER + COLUMN_LINK + TEXT + COLUMN_LIKES_COUNT +
            INTEGER + COLUMN_CAPTION + TEXT + COLUMN_USERNAME + TEXT + COLUMN_FULL_NAME + TEXT
            + COLUMN_PROFILE_PICTURE + TEXT + COLUMN_STANDARD_RES_IMAGE + TEXT + " unique(" +
            COLUMN_ID + ") on conflict replace);";
    private static final String INSTAGRAM_TABLE_DESTROYER = "drop table " + DBProvider.DB_NAME +
            "" + "." + INSTAGRAM_TABLE_NAME + ";";

    private static final String TWITTER_TABLE_BUILDER = "create table " + TWITTER_TABLE_NAME +
            "(" + BaseColumns._ID + " integer primary key, " + COLUMN_CREATED_AT + " " +
            INTEGER + COLUMN_ID + TEXT + COLUMN_TEXT + TEXT + COLUMN_PROFILE_IMAGE_URL + TEXT +
            COLUMN_SCREEN_NAME + TEXT + " unique(" + COLUMN_ID + ") on conflict " + "ignore);";
    private static final String TWITTER_TABLE_DESTROYER = "drop table " + DBProvider.DB_NAME +
            "" + "." + TWITTER_TABLE_NAME + ";";

    public DBOpenHelper(final Context context, final String name,
                        final CursorFactory factory, final int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(NINE_GAG_TABLE_BUILDER);
        db.execSQL(FACEBOOK_TABLE_BUILDER);
        db.execSQL(INSTAGRAM_TABLE_BUILDER);
        db.execSQL(TWITTER_TABLE_BUILDER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(NINE_GAG_TABLE_DESTROYER);
        db.execSQL(FACEBOOK_TABLE_DESTROYER);
        db.execSQL(INSTAGRAM_TABLE_DESTROYER);
        db.execSQL(TWITTER_TABLE_DESTROYER);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
