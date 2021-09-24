package com.example.test_sp_sdk_v6;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import com.sourcepoint.cmplibrary.model.exposed.ActionType;
import com.sourcepoint.cmplibrary.model.exposed.CCPAConsent;
import com.sourcepoint.cmplibrary.model.exposed.GDPRConsent;
import com.sourcepoint.cmplibrary.model.exposed.SPConsents;

import org.json.JSONObject;

public class RNCMP {
  // TAG for logcat
  private static final String TAG = "RNCMP";

  // Events that can be emitted back to js
  private static final String EVENT_CONSENT_UI_READY = "CMP_ConsentUIReady";
  private static final String EVENT_MESSAGE_WILL_SHOW = "CMP_MessageWillShow";
  private static final String EVENT_MESSAGE_DID_DISAPPEAR = "CMP_MessageDidDisappear";
  private static final String EVENT_PM_WILL_SHOW = "CMP_PmWillShow";
  private static final String EVENT_PM_DID_DISAPPEAR = "CMP_PmDidDisappear";
  private static final String EVENT_CONSENT_UI_FINISHED = "CMP_ConsentUIFinished";
  private static final String EVENT_ACTION = "CMP_Action";
  private static final String EVENT_CONSENT_READY = "CMP_ConsentReady";
  private static final String EVENT_ERROR = "CMP_Error";

  // Error code and messages
  private static final String E_CMP_SDK = "E_CMP_SDK";
  private static final String E_USER = "E_USER";
  private static final String E_UNEXPECTED = "E_UNEXPECTED";
  private static final String USER_BACK_OUT = "user backs out of consent screen";

  // Singleton
  private static final Object sInstanceLock = new Object();
  private static RNCMP sInstance = null;

  private Promise mPendingPromise = null;

  private Context mReactApplicationContext;

  private RNCMP(Context reactContext) {
    mReactApplicationContext = reactContext;
  }

  public static RNCMP getInstance(Context reactContext) {
    synchronized (sInstanceLock) {
      if (sInstance == null) {
        sInstance = new RNCMP(reactContext);
      }
    }
    return sInstance;
  }

  public void loadMessage(String authId, boolean onStaging, Promise promise) {
    savePendingPromise(promise);
    Intent intent = createStandardIntentWithExtras(authId, onStaging);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.putExtra(CMPActivity.ARG_CMD, CMPActivity.CMD_LOAD_MESSAGE);
    mReactApplicationContext.startActivity(intent);
  }

  public void loadGdprPrivacyManager(String authId, boolean onStaging, Promise promise) {
    savePendingPromise(promise);
    Intent intent = createStandardIntentWithExtras(authId, onStaging);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.putExtra(CMPActivity.ARG_CMD, CMPActivity.CMD_LOAD_GDPR_PRIVACY_MANAGER);
    mReactApplicationContext.startActivity(intent);
  }

  public void loadCcpaPrivacyManager(String authId, boolean onStaging, Promise promise) {
    savePendingPromise(promise);
    Intent intent = createStandardIntentWithExtras(authId, onStaging);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.putExtra(CMPActivity.ARG_CMD, CMPActivity.CMD_LOAD_CCPA_PRIVACY_MANAGER);
    mReactApplicationContext.startActivity(intent);
  }

  /////////////////////////////
  // Callbacks from CMPActivity

  void onConsentUIReady() {
    sendEvent(EVENT_CONSENT_UI_READY, null);
  }

  void onMessageWillShow(JSONObject message) {
    // TODO: How to convert JSONObject into WritableMap?
    sendEvent(EVENT_MESSAGE_WILL_SHOW, null);
  }

  void onConsentUIFinished() {
    sendEvent(EVENT_CONSENT_UI_FINISHED, null);
  }

  void onAction(ActionType actionType) {
    WritableMap params = Arguments.createMap();
    params.putString("type", actionType.name());
    sendEvent(EVENT_ACTION, params);
  }

  void onConsentReady(SPConsents consentData) {
    // The nativeMap obj created by Arguments is consumable. So we have to create it twice
    sendEvent(EVENT_CONSENT_READY, buildConsentDataPayload(consentData));
    resolvePendingPromise(buildConsentDataPayload(consentData));
  }

  void onError(Throwable exception) {
    String message = exception.getMessage();
    WritableMap params = createErrorEventParams(E_CMP_SDK, message);
    sendEvent(EVENT_ERROR, params);
    rejectPendingPromise(E_CMP_SDK, message);
  }

  void onUserBackOut() {
    WritableMap params = createErrorEventParams(E_USER, USER_BACK_OUT);
    sendEvent(EVENT_ERROR, params);
    rejectPendingPromise(E_USER, USER_BACK_OUT);
  }

  ///////////////////
  // Helper functions

  private Intent createStandardIntentWithExtras(String authId, boolean onStaging) {
    Intent intent = new Intent(mReactApplicationContext, CMPActivity.class);
    if (authId != null) {
      intent.putExtra(CMPActivity.ARG_AUTH_ID, authId);
    }
    if (onStaging) {
      intent.putExtra(CMPActivity.ARG_ON_STANGING, true);
    }
    return intent;
  }

  private void sendEvent(String eventName, @Nullable WritableMap params) {
    Log.d(TAG, "sendEvent: name=" + eventName + ", value=" + params);
  }

  private WritableMap createErrorEventParams(String code, String message) {
    WritableMap params = Arguments.createMap();
    params.putString("code", code);
    params.putString("message", message);
    return params;
  }

  private void rejectPendingPromise(String code, String message) {
    if (mPendingPromise != null) {
      mPendingPromise.reject(code, message);
      mPendingPromise = null;
    }
  }

  private void resolvePendingPromise(Object result) {
    if (mPendingPromise != null) {
      mPendingPromise.resolve(result);
      mPendingPromise = null;
    }
  }

  private void savePendingPromise(Promise promise) {
    mPendingPromise = promise;
  }

  // TODO: Looks like vendorGrants data structure changed (breaking). There is no vendorGrant
  // boolean and purposeGrants Map any more. Just a single Map<String, boolean> per vendor
  // Need to confirm with Kevin about this breaking change

  private WritableMap buildConsentDataPayload(SPConsents consentData) {
    WritableMap result = Arguments.createMap();
    result.putString("gdpr", "gdpr consent value");
    result.putString("ccpa", "ccpa consent value");
    return result;
  }
}
