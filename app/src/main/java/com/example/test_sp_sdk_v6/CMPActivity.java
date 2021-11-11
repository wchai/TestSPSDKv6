package com.example.test_sp_sdk_v6;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.sourcepoint.cmplibrary.NativeMessageController;
import com.sourcepoint.cmplibrary.SpClient;
import com.sourcepoint.cmplibrary.SpConsentLib;
import com.sourcepoint.cmplibrary.core.nativemessage.MessageStructure;
import com.sourcepoint.cmplibrary.creation.FactoryKt;
import com.sourcepoint.cmplibrary.exception.CampaignType;
import com.sourcepoint.cmplibrary.model.ConsentAction;
import com.sourcepoint.cmplibrary.model.PMTab;
import com.sourcepoint.cmplibrary.model.exposed.ActionType;
import com.sourcepoint.cmplibrary.model.exposed.SPConsents;
import com.sourcepoint.cmplibrary.model.exposed.SpConfig;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class CMPActivity extends Activity {
  // TAG for logcat
  private static final String TAG = "CMPActivity";

  // Args passed through Intent
  public static final String ARG_AUTH_ID = "authId";
  public static final String ARG_ON_STANGING = "onStaging";
  public static final String ARG_CMD = "cmd";
  public static final String CMD_LOAD_MESSAGE = "loadMessage";
  //  public static final String CMD_LOAD_PRIVACY_MANAGER = "loadPrivacyManager";
  // Now this activity handles both GDPR and CCPA, we we need two diff cmd:
  public static final String CMD_LOAD_GDPR_PRIVACY_MANAGER = "loadGdprPrivacyManager";
  public static final String CMD_LOAD_CCPA_PRIVACY_MANAGER = "loadCcpaPrivacyManager";

  // Shallow copy to the RNCMP singleton
  private RNCMP mCMP = null;

  // ConsentLib builder
  private boolean mIsConsentLibInitialized = false;

  // View hierearchy
  //  private ViewGroup mMainViewGroup = null;
  private View mSpinner = null;

  // Args passed through Intent
  private String mAuthId = null;
  private boolean mOnStaging = false;
  private String mCmd = null;

  // User can use Android BACK button to quit Msg or PM screen. In that case, we will
  // not get any callback from SourcePoint SDK. Just Android onPause()/onDestroy().
  // So we have to handle it by checking if the onDestroy() is due to BACK
  private boolean mVoluntaryLeaving = false;

  // New SDK 6.x
  private SpConfig mSpConfig = null;
  private SpConsentLib mSpConsentLib = null;
  private static final String sGdprPMId = "220710";
  private static final String sCcpaPMId = "553173";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mCMP = RNCMP.getInstance(null);
    setContentView(R.layout.cmp_activity);
    //    mMainViewGroup = findViewById(R.id.cmp_container);
    mSpinner = findViewById(R.id.spinner);
    Intent intent = getIntent();
    mAuthId = intent.getStringExtra(ARG_AUTH_ID);
    mOnStaging = intent.getBooleanExtra(ARG_ON_STANGING, false);
    mCmd = intent.getStringExtra(ARG_CMD);

    mSpConfig = CMPLibHelper.createSpConfig(mOnStaging);
    mSpConsentLib = FactoryKt.makeConsentLib(mSpConfig, this, new LocalClient());
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (!mIsConsentLibInitialized) {
      mIsConsentLibInitialized = true;
      if (CMD_LOAD_GDPR_PRIVACY_MANAGER.equalsIgnoreCase(mCmd)) {
        mSpConsentLib.loadPrivacyManager(sGdprPMId, PMTab.DEFAULT, CampaignType.GDPR);
      } else if (CMD_LOAD_CCPA_PRIVACY_MANAGER.equalsIgnoreCase(mCmd)) {
        mSpConsentLib.loadPrivacyManager(sCcpaPMId, PMTab.DEFAULT, CampaignType.CCPA);
      } else if (mAuthId != null) {
        mSpConsentLib.loadMessage(mAuthId);
      } else {
        mSpConsentLib.loadMessage();
      }
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (!mVoluntaryLeaving) {
      // User must kill the Activity by BACK button
      if (mCMP != null) {
        mCMP.onUserBackOut();
      }
    }
    // Clear the shallow copies
    //    mMainViewGroup = null;
    mSpinner = null;
    mCMP = null;

    // Dispose 6.x instances
    mSpConfig = null;
    if (mSpConsentLib != null) {
      mSpConsentLib.dispose();
      mSpConsentLib = null;
    }
  }

  class LocalClient implements SpClient {
    @Override
    public void onMessageReady(JSONObject message) {
      if (mCMP != null) {
        mCMP.onMessageWillShow(message); // We have no idea this is GDPR or CCPA in 6.x
      }
    }

    @Override
    public void onError(Throwable error) {
      Log.e(TAG, "Something went wrong with CMP: ", error);
      if (mCMP != null) {
        mCMP.onError(error);
      }
      mVoluntaryLeaving = true;
      finish(); // Close current Activity
    }

    @Override
    public void onConsentReady(SPConsents consent) {
      if (mCMP != null) {
        mCMP.onConsentReady(consent);
      }
      mVoluntaryLeaving = true;

      // FIXME: This is just a hack in this POC to figure out which PrivacyManager
      // we need to invoke when calling loadPrivacyManager(): GDPR or CCPA
      ((TestApplication)getApplication()).mSubjectToGDPR = (((Integer)(consent.getGdpr().getConsent().getTcData().get("IABTCF_gdprApplies"))).intValue() == 1);

      finish(); // Close current Activity
    }

    @NotNull
    @Override
    public ConsentAction onAction(@NotNull View view, @NotNull ConsentAction consentAction) {
      if (mCMP != null) {
        mCMP.onAction(consentAction.getActionType());
      }
      return consentAction;
    }

    @Override
    public void onNativeMessageReady(@NotNull MessageStructure messageStructure, @NotNull NativeMessageController nativeMessageController) {

    }

    @Override
    public void onUIFinished(View v) {
      mSpConsentLib.removeView(v);
      if (mCMP != null) {
        mCMP.onConsentUIFinished(); // We have no idea this is GDPR or CCPA in 6.x
      }
    }

    @Override
    public void onUIReady(View v) {
      mSpConsentLib.showView(v);
      if (mSpinner != null) {
        mSpinner.setVisibility(View.GONE);
      }
      if (mCMP != null) {
        mCMP.onConsentUIReady(); // We have no idea this is GDPR or CCPA in 6.x
      }
    }
  }

  // No need for this in 6.x where we use a LocalClient class
  // private GDPRConsentLib buildGDPRConsentLib() {
  //   ConsentLibBuilder builder =
  //       CMPLibHelper.createBasicBuilder(mAuthId, mOnStaging, this)
  //           .setOnConsentUIReady(
  //               view -> {
  //                 showView(view);
  //                 if (mCMP != null) {
  //                   mCMP.onConsentUIReady();
  //                 }
  //               })
  //           .setOnConsentUIFinished(
  //               view -> {
  //                 removeView(view);
  //                 if (mCMP != null) {
  //                   mCMP.onConsentUIFinished();
  //                 }
  //               })
  //           .setOnMessageReady(
  //               () -> {
  //                 if (mCMP != null) {
  //                   mCMP.onMessageWillShow();
  //                 }
  //               })
  //           .setOnMessageFinished(
  //               () -> {
  //                 if (mCMP != null) {
  //                   mCMP.onMessageDidDisappear();
  //                 }
  //               })
  //           .setOnPMReady(
  //               () -> {
  //                 if (mCMP != null) {
  //                   mCMP.onPmWillShow();
  //                 }
  //               })
  //           .setOnPMFinished(
  //               () -> {
  //                 if (mCMP != null) {
  //                   mCMP.onPmDidDisappear();
  //                 }
  //               })
  //           .setMessageTimeOut(20000L)
  //           .setOnAction(
  //               actionType -> {
  //                 if (mCMP != null) {
  //                   mCMP.onAction(actionType);
  //                 }
  //               })
  //           .setOnConsentReady(
  //               consent -> {
  //                 if (mCMP != null) {
  //                   mCMP.onConsentReady(consent);
  //                 }
  //                 mVoluntaryLeaving = true;
  //                 finish(); // Close current Activity
  //               })
  //           .setOnError(
  //               error -> {
  //                 Log.e(TAG, "Something went wrong: ", error);
  //                 if (mCMP != null) {
  //                   mCMP.onError(error);
  //                 }
  //                 mVoluntaryLeaving = true;
  //                 finish(); // Close current Activity
  //               });
  //
  //   return builder.build();
  // }

  // Not needed for 6.x
  // private void showView(View view) {
  //   if (view.getParent() == null) {
  //     view.setLayoutParams(new ViewGroup.LayoutParams(0, 0));
  //     view.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
  //     view.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
  //     view.bringToFront();
  //     view.requestLayout();
  //     mMainViewGroup.addView(view);
  //     if (mSpinner != null) {
  //       mSpinner.setVisibility(View.GONE);
  //     }
  //   }
  // }

  // private void removeView(View view) {
  //   if (view.getParent() != null) {
  //     mMainViewGroup.removeView(view);
  //   }
  // }
}
