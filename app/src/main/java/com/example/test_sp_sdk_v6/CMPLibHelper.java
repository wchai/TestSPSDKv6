package com.example.test_sp_sdk_v6;

import android.util.Log;

import com.sourcepoint.cmplibrary.creation.SpConfigDataBuilder;
import com.sourcepoint.cmplibrary.data.network.util.*;
import com.sourcepoint.cmplibrary.exception.CampaignType;
import com.sourcepoint.cmplibrary.model.MessageLanguage;
import com.sourcepoint.cmplibrary.model.exposed.SpConfig;
import com.sourcepoint.cmplibrary.model.exposed.TargetingParam;

import java.util.ArrayList;
import java.util.List;

public class CMPLibHelper {
  // TAG for LogCat
  private static final String TAG = "CMPLibHelper";

  // Credentials from SourcePoint
  private static final Integer nAccountId = 1425;
  private static final String sProperty = "bloombergv6.android";

  // New way with 6.x is to create SpConfig
  static SpConfig createSpConfig(boolean onStaging) {
    // In 6.x targetingParams is set with campaign
    List<TargetingParam> gdprTargetingParams = new ArrayList<>();
    gdprTargetingParams.add(new TargetingParam("type", "GDPR"));
    List<TargetingParam> ccpaTargetingParams = new ArrayList<>();
    ccpaTargetingParams.add(new TargetingParam("type", "CCPA"));

    SpConfigDataBuilder spConfigDataBuilder =
        new SpConfigDataBuilder()
            .addAccountId(nAccountId)
            .addPropertyName(sProperty)
            .addMessageLanguage(MessageLanguage.ENGLISH)
            .addMessageTimeout(10000L)
            .addCampaign(CampaignType.GDPR, gdprTargetingParams)
            .addCampaign(CampaignType.CCPA, ccpaTargetingParams);

    // TODO: Figure out how to set DEBUG_LEVEL
    // if (BuildConfig.DEBUG) {
    //   Log.i(TAG, "setDebugLevel() to DEBUG");
    //   builder.setDebugLevel(GDPRConsentLib.DebugLevel.DEBUG);
    // }

    // Optional stuffs
    if (true/*onStaging*/) { // FIXME: hard code STAGE for debugging only
      Log.i(TAG, "addCampaignsEnv() to STAGE");
      spConfigDataBuilder.addCampaignsEnv(CampaignsEnv.STAGE);
    }

    return spConfigDataBuilder.build();
  }
}
