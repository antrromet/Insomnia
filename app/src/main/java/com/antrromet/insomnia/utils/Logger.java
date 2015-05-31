package com.antrromet.insomnia.utils;

import android.text.TextUtils;

import com.antrromet.insomnia.BuildConfig;

/**
 * Utility class to mask the Android Logging mechanism. Simply swap the DEBUG
 * parameter before compiling to block logs.
 *
 * @author Antrromet
 */
public class Logger {
    public static final boolean DEBUG = BuildConfig.DEBUG;

    private Logger() {
        // Nothing to do, adding since the utility classes should not have a
        // public constructor
    }

    public static void v(String tag, String message) {
        if (DEBUG && !TextUtils.isEmpty(message)) {
            android.util.Log.v(tag, message);
        }
    }

    public static void v(String tag, String message, Throwable e) {
        if (DEBUG && !TextUtils.isEmpty(message)) {
            android.util.Log.v(tag, message, e);
        }
    }

    public static void w(String tag, String message) {
        if (DEBUG && !TextUtils.isEmpty(message)) {
            android.util.Log.w(tag, message);
        }
    }

    public static void w(String tag, String message, Throwable e) {
        if (DEBUG && !TextUtils.isEmpty(message)) {
            android.util.Log.w(tag, message, e);
        }
    }

    public static void i(String tag, String message) {
        if (DEBUG && !TextUtils.isEmpty(message)) {
            android.util.Log.i(tag, message);
        }
    }

    public static void i(String tag, String message, Throwable e) {
        if (DEBUG && !TextUtils.isEmpty(message)) {
            android.util.Log.i(tag, message, e);
        }
    }

    public static void d(String tag, String message) {
        if (DEBUG && !TextUtils.isEmpty(message)) {
            android.util.Log.d(tag, message);
        }
    }

    public static void d(String tag, String message, Throwable e) {
        if (DEBUG && !TextUtils.isEmpty(message)) {
            android.util.Log.d(tag, message, e);
        }
    }

    public static void e(String tag, String message) {
        if (DEBUG && !TextUtils.isEmpty(message)) {
            android.util.Log.e(tag, message);
        }
    }

    public static void e(String tag, String message, Throwable e) {
        if (DEBUG && !TextUtils.isEmpty(message)) {
            android.util.Log.e(tag, message, e);
        }
    }

    public static void wtf(String tag, String message) {
        if (DEBUG && !TextUtils.isEmpty(message)) {
            android.util.Log.wtf(tag, message);
        }
    }

    public static void wtf(String tag, String message, Throwable e) {
        if (DEBUG && !TextUtils.isEmpty(message)) {
            android.util.Log.wtf(tag, message, e);
        }
    }
}
