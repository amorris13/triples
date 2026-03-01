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
public class GameFlowTest {

    @Rule
    public ActivityScenarioRule<ClassicGameListActivity> activityRule =
            new ActivityScenarioRule<>(ClassicGameListActivity.class);

    @Test
    public void testClassicGameStart() {
        // Main screen
        ScreenshotComparator.compare("classic_list_screen");

        // Start new game
        onView(withId(R.id.new_game)).perform(click());

        // Verify game screen
        onView(withId(R.id.cards_view)).check(matches(isDisplayed()));
        ScreenshotComparator.compare("classic_game_screen");
    }

    @Test
    public void testArcadeGameStart() {
        // Open drawer
        onView(withId(R.id.drawer_layout)).perform(click());

        // Select Arcade (position 1 in the list)
        onView(withText("Arcade")).perform(click());

        // Verify Arcade list screen
        onView(withText("Arcade")).check(matches(isDisplayed()));
        ScreenshotComparator.compare("arcade_list_screen");

        // Start new game
        onView(withId(R.id.new_game)).perform(click());

        // Verify game screen
        onView(withId(R.id.cards_view)).check(matches(isDisplayed()));
        ScreenshotComparator.compare("arcade_game_screen");
    }
}
