package com.antsapps.triples;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SettingsAndHelpTest {

    @Rule
    public ActivityScenarioRule<ClassicGameListActivity> activityRule =
            new ActivityScenarioRule<>(ClassicGameListActivity.class);

    @Test
    public void testHelpScreen() {
        onView(withId(R.id.help)).perform(click());

        // Verify Help screen
        onView(withId(R.id.cards_view)).check(matches(isDisplayed()));
        onView(withId(R.id.number_explanation)).check(matches(isDisplayed()));

        ScreenshotComparator.compare("help_screen");
    }

    @Test
    public void testSettingsScreen() {
        onView(withId(R.id.settings)).perform(click());

        // Verify Settings screen
        ScreenshotComparator.compare("settings_screen");
    }
}
