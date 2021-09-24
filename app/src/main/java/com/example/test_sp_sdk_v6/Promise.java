package com.example.test_sp_sdk_v6;

import android.util.Log;

public class Promise {

    private static String TAG = "Promise";

    public void resolve(Object data) {
        Log.d(TAG, "Promise resolved with: " + data.toString());
    }

    public void reject(String code, String message) {
        Log.e(TAG, "Promise rejected with: code=" + code + ", msg=" + message);
    }
}
