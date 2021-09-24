package com.example.test_sp_sdk_v6;

import android.util.ArrayMap;

public class WritableMap {
    private ArrayMap<String, Object> mMap = null;

    public WritableMap() {
        mMap = new ArrayMap<String, Object>();
    }

    public void putString(String key, String value) {
        mMap.put(key, value);
    }

}
