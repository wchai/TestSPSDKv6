package com.example.test_sp_sdk_v6;

import android.app.Application;

public class TestApplication extends Application {
    // Use a boolean here to remember if gdpr applies.
    // Set this value from the SpConsents.gdpr data from onConsentReady() of loadMessage()
    public boolean mSubjectToGDPR = false;
}
