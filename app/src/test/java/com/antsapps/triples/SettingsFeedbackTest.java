package com.antsapps.triples;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import androidx.preference.Preference;
import androidx.test.core.app.ActivityScenario;
import org.junit.Test;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowPackageManager;

public class SettingsFeedbackTest extends BaseRobolectricTest {

  @Test
  public void testFeedbackIntent() {
    try (ActivityScenario<SettingsActivity> scenario =
        ActivityScenario.launch(SettingsActivity.class)) {
      scenario.onActivity(
          activity -> {
            // Register a fake email activity
            ShadowPackageManager shadowPackageManager = shadowOf(activity.getPackageManager());
            Intent mailtoIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
            ResolveInfo resolveInfo = new ResolveInfo();
            resolveInfo.activityInfo = new ActivityInfo();
            resolveInfo.activityInfo.packageName = "com.email.app";
            resolveInfo.activityInfo.name = "EmailActivity";
            shadowPackageManager.addResolveInfoForIntent(mailtoIntent, resolveInfo);

            SettingsFragment fragment =
                (SettingsFragment)
                    activity.getSupportFragmentManager().findFragmentByTag(".SettingsFragment");
            assertThat(fragment).isNotNull();

            Preference feedbackPref = fragment.findPreference("pref_feedback");
            assertThat(feedbackPref).isNotNull();

            feedbackPref.getOnPreferenceClickListener().onPreferenceClick(feedbackPref);

            ShadowActivity shadowActivity = shadowOf(activity);
            Intent chooserIntent = shadowActivity.getNextStartedActivity();
            assertThat(chooserIntent).isNotNull();
            assertThat(chooserIntent.getAction()).isEqualTo(Intent.ACTION_CHOOSER);

            Intent innerIntent = chooserIntent.getParcelableExtra(Intent.EXTRA_INTENT);
            assertThat(innerIntent).isNotNull();
            assertThat(innerIntent.getAction()).isEqualTo(Intent.ACTION_SENDTO);
            assertThat(innerIntent.getData()).isEqualTo(Uri.parse("mailto:"));
            assertThat(innerIntent.getStringArrayExtra(Intent.EXTRA_EMAIL))
                .isEqualTo(new String[] {"anthonymorris13+triples@gmail.com"});
          });
    }
  }
}
