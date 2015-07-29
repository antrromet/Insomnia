package com.antrromet.insomnia.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

public class DBProvider extends ContentProvider {

    public static final String DB_NAME = "insomnia.db";
    public static final int DB_VERSION = 1;
    private static final UriMatcher URI_MATCHER = new UriMatcher(
            UriMatcher.NO_MATCH);

    private static final String AUTHORITY = "com.antrromet.insomnia.provider";
    private static final String URI_PREFIX = "content://com.antrromet.insomnia.provider/";

    public static final Uri URI_NINE_GAG = Uri.parse(URI_PREFIX + DBOpenHelper.NINE_GAG_TABLE_NAME);
    public static final Uri URI_FACEBOOK = Uri.parse(URI_PREFIX + DBOpenHelper.FACEBOOK_TABLE_NAME);
    public static final Uri URI_INSTAGRAM = Uri.parse(URI_PREFIX + DBOpenHelper
            .INSTAGRAM_TABLE_NAME);
    public static final Uri URI_TWITER = Uri.parse(URI_PREFIX + DBOpenHelper
            .TWITTER_TABLE_NAME);

    private static final int CONTENT_NINE_GAG = 101;
    private static final int CONTENT_FACEBOOK = 102;
    private static final int CONTENT_INSTAGRAM = 103;
    private static final int CONTENT_TWITTER = 104;

    static {
        URI_MATCHER.addURI(AUTHORITY, DBOpenHelper.NINE_GAG_TABLE_NAME,
                CONTENT_NINE_GAG);
        URI_MATCHER.addURI(AUTHORITY, DBOpenHelper.FACEBOOK_TABLE_NAME,
                CONTENT_FACEBOOK);
        URI_MATCHER.addURI(AUTHORITY, DBOpenHelper.INSTAGRAM_TABLE_NAME,
                CONTENT_INSTAGRAM);
        URI_MATCHER.addURI(AUTHORITY, DBOpenHelper.TWITTER_TABLE_NAME,
                CONTENT_TWITTER);
    }

    private DBOpenHelper dbHelper;

    /**
     * Call to get the table name associated to given contentType
     *
     * @param contentType type of the content
     * @return the table name
     */
    private static String getTableName(final int contentType) {

        if (contentType == CONTENT_NINE_GAG) {
            return DBOpenHelper.NINE_GAG_TABLE_NAME;
        } else if (contentType == CONTENT_FACEBOOK) {
            return DBOpenHelper.FACEBOOK_TABLE_NAME;
        } else if (contentType == CONTENT_INSTAGRAM) {
            return DBOpenHelper.INSTAGRAM_TABLE_NAME;
        } else if (contentType == CONTENT_TWITTER) {
            return DBOpenHelper.TWITTER_TABLE_NAME;
        }

        return null;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DBOpenHelper(getContext(), DB_NAME, null, DB_VERSION);
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor;

        final SQLiteDatabase database = dbHelper.getReadableDatabase();

        final String limit = uri.getQueryParameter("limit");
        final String tableName = getTableName(URI_MATCHER.match(uri));

        cursor = database.query(false, tableName, projection, selection,
                selectionArgs, null, null, sortOrder, limit);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();

        final String tableName = getTableName(URI_MATCHER.match(uri));
        long rowId = database.insert(tableName, null, values);
        if (rowId != -1) {
            final Uri insertUri = ContentUris.withAppendedId(uri, rowId);
            getContext().getContentResolver().notifyChange(uri, null);
            return insertUri;
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();

        final String tableName = getTableName(URI_MATCHER.match(uri));
        int count = database.delete(tableName, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();

        final String tableName = getTableName(URI_MATCHER.match(uri));

        int count = database.update(tableName, values, selection, selectionArgs);
        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return count;
    }

    @Override
    public int bulkInsert(final Uri uri, @NonNull final ContentValues[] values) {

        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        final String tableName = getTableName(URI_MATCHER.match(uri));
        int numInserted = 0;
        db.beginTransaction();

        try {
            for (final ContentValues aValue : values) {
                if (aValue == null || aValue.size() == 0) {
                    break;
                }
                db.insert(tableName, null, aValue);
            }
            numInserted = values.length;
            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }
        return numInserted;
    }

}
