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

    public static final String NINE_GAG_TABLE_NAME = "nine_gag_feeds";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_INSERTION_TIME = "insertion_time";
    public static final String COLUMN_CAPTION = "caption";
    public static final String COLUMN_IMAGE_LARGE = "image_large";
    public static final String COLUMN_IMAGE_NORMAL = "image_normal";
    public static final String COLUMN_LINK = "link";
    public static final String COLUMN_VOTES_COUNT = "votes_count";
    private static final String TEXT = " text, ";
    private static final String INTEGER = " integer, ";
    private static final String NINE_GAG_FEEDS_TABLE_BUILDER = "create table " + NINE_GAG_TABLE_NAME + "(" + BaseColumns._ID + " integer primary key, " + COLUMN_INSERTION_TIME + " timestamp default (strftime('%s', 'now')), " + COLUMN_ID + INTEGER + COLUMN_CAPTION + TEXT + COLUMN_IMAGE_LARGE + TEXT + COLUMN_IMAGE_NORMAL + TEXT + COLUMN_LINK + TEXT + TEXT + COLUMN_VOTES_COUNT + INTEGER + " unique(" + COLUMN_ID + ") on conflict ignore);";
    private static final String NINE_GAG_FEEDS_TABLE_DESTROYER = "drop table " + DBProvider.DB_NAME + "." + NINE_GAG_TABLE_NAME + ";";

    public DBOpenHelper(final Context context, final String name,
                        final CursorFactory factory, final int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(NINE_GAG_FEEDS_TABLE_BUILDER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(NINE_GAG_FEEDS_TABLE_DESTROYER);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
