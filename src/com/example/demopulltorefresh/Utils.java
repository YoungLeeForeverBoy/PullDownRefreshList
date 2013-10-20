package com.example.demopulltorefresh;

import android.util.Log;

public class Utils {
    public final static String TAG = "Young Lee";
    public final static boolean DEBUG = true;

    public static void log(String msg) {
        if(DEBUG)
            Log.i(TAG, msg);
    }
}
