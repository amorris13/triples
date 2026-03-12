package com.antsapps.triples.util;

import android.os.Bundle;
import com.antsapps.triples.AnalyticsConstants;
import com.google.firebase.analytics.FirebaseAnalytics;

public class AnalyticsUtil {
  public static void logGameEvent(FirebaseAnalytics analytics, String event, String gameType) {
    if (analytics == null) return;
    Bundle bundle = new Bundle();
    bundle.putString(AnalyticsConstants.Param.GAME_TYPE, gameType);
    analytics.logEvent(event, bundle);
  }
}
