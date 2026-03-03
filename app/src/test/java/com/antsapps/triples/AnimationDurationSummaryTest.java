package com.antsapps.triples;

import static com.google.common.truth.Truth.assertThat;

import androidx.preference.SeekBarPreference;
import androidx.test.core.app.ActivityScenario;
import org.junit.Test;
import org.robolectric.annotation.Config;

public class AnimationDurationSummaryTest extends BaseRobolectricTest {

    @Test
    public void testAnimationDurationSummaryUpdates() {
        try (ActivityScenario<SettingsActivity> scenario = ActivityScenario.launch(SettingsActivity.class)) {
            scenario.onActivity(activity -> {
                SettingsFragment fragment = (SettingsFragment) activity.getSupportFragmentManager().findFragmentByTag(".SettingsFragment");
                SeekBarPreference animationDurationPref = fragment.findPreference(activity.getString(R.string.pref_animation_speed));

                assertThat(animationDurationPref).isNotNull();

                // Initial value
                int initialValue = animationDurationPref.getValue();
                assertThat(animationDurationPref.getSummary().toString()).isEqualTo(activity.getString(R.string.pref_animation_duration_summary, initialValue));

                // Change value and verify summary updates
                int newValue = 500;
                // In a real app, sliding the seekbar triggers onPreferenceChange.
                // We simulate this by calling the listener directly.
                fragment.onPreferenceChange(animationDurationPref, newValue);

                assertThat(animationDurationPref.getSummary().toString()).isEqualTo(activity.getString(R.string.pref_animation_duration_summary, newValue));
            });
        }
    }
}
