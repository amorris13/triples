package com.antsapps.triples;

import static com.google.common.truth.Truth.assertThat;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.ArcadeGame;
import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.DailyGame;
import com.antsapps.triples.backend.Game;
import org.junit.Test;

public class ColoringTest extends BaseRobolectricTest {

  @Test
  public void testClassicGameColoring() {
    Application app = Application.getInstance(ApplicationProvider.getApplicationContext());
    ClassicGame game = ClassicGame.createFromSeed(12345L);
    app.addClassicGame(game);

    Intent intent =
        new Intent(ApplicationProvider.getApplicationContext(), ClassicGameActivity.class);
    intent.putExtra(Game.ID_TAG, game.getId());

    int expectedColor =
        ContextCompat.getColor(ApplicationProvider.getApplicationContext(), R.color.classic_accent);

    try (ActivityScenario<ClassicGameActivity> scenario = ActivityScenario.launch(intent)) {
      scenario.onActivity(
          activity -> {
            // Check Action Bar Title is not colored
            CharSequence title = activity.getTitle();
            if (title instanceof SpannableString) {
              SpannableString ss = (SpannableString) title;
              ForegroundColorSpan[] spans = ss.getSpans(0, ss.length(), ForegroundColorSpan.class);
              assertThat(spans).isEmpty();
            }

            // Check bottom separator color
            View bottomSeparator = activity.findViewById(R.id.bottom_separator);
            assertThat(((ColorDrawable) bottomSeparator.getBackground()).getColor())
                .isEqualTo(expectedColor);

            // Check paused text color
            TextView pausedText = activity.findViewById(R.id.paused);
            assertThat(pausedText.getCurrentTextColor()).isEqualTo(expectedColor);

            // Check button tints
            assertThat(activity.findViewById(R.id.statistics_button).getBackgroundTintList())
                .isEqualTo(ColorStateList.valueOf(expectedColor));
            assertThat(activity.findViewById(R.id.new_game_button).getBackgroundTintList())
                .isEqualTo(ColorStateList.valueOf(expectedColor));
          });
    }
  }

  @Test
  public void testArcadeGameColoring() {
    Application app = Application.getInstance(ApplicationProvider.getApplicationContext());
    ArcadeGame game = ArcadeGame.createFromSeed(12345L);
    app.addArcadeGame(game);

    Intent intent =
        new Intent(ApplicationProvider.getApplicationContext(), ArcadeGameActivity.class);
    intent.putExtra(Game.ID_TAG, game.getId());

    int expectedColor =
        ContextCompat.getColor(ApplicationProvider.getApplicationContext(), R.color.arcade_accent);

    try (ActivityScenario<ArcadeGameActivity> scenario = ActivityScenario.launch(intent)) {
      scenario.onActivity(
          activity -> {
            // Check Action Bar Title is not colored
            CharSequence title = activity.getTitle();
            if (title instanceof SpannableString) {
              SpannableString ss = (SpannableString) title;
              ForegroundColorSpan[] spans = ss.getSpans(0, ss.length(), ForegroundColorSpan.class);
              assertThat(spans).isEmpty();
            }

            // Check bottom separator color
            View bottomSeparator = activity.findViewById(R.id.bottom_separator);
            assertThat(((ColorDrawable) bottomSeparator.getBackground()).getColor())
                .isEqualTo(expectedColor);

            // Check paused text color
            TextView pausedText = activity.findViewById(R.id.paused);
            assertThat(pausedText.getCurrentTextColor()).isEqualTo(expectedColor);

            // Check button tints
            assertThat(activity.findViewById(R.id.statistics_button).getBackgroundTintList())
                .isEqualTo(ColorStateList.valueOf(expectedColor));
            assertThat(activity.findViewById(R.id.new_game_button).getBackgroundTintList())
                .isEqualTo(ColorStateList.valueOf(expectedColor));
          });
    }
  }

  @Test
  public void testDailyGameColoring() {
    Application app = Application.getInstance(ApplicationProvider.getApplicationContext());
    DailyGame game = DailyGame.createFromDay(new DailyGame.Day(2026, 03, 11));
    app.addDailyGame(game);

    Intent intent =
        new Intent(ApplicationProvider.getApplicationContext(), DailyGameActivity.class);
    intent.putExtra(Game.ID_TAG, game.getId());

    int expectedColor =
        ContextCompat.getColor(ApplicationProvider.getApplicationContext(), R.color.daily_accent);

    try (ActivityScenario<DailyGameActivity> scenario = ActivityScenario.launch(intent)) {
      scenario.onActivity(
          activity -> {
            // Check Action Bar Title is not colored
            CharSequence title = activity.getTitle();
            if (title instanceof SpannableString) {
              SpannableString ss = (SpannableString) title;
              ForegroundColorSpan[] spans = ss.getSpans(0, ss.length(), ForegroundColorSpan.class);
              assertThat(spans).isEmpty();
            }

            // Check bottom separator color
            View bottomSeparator = activity.findViewById(R.id.bottom_separator);
            assertThat(((ColorDrawable) bottomSeparator.getBackground()).getColor())
                .isEqualTo(expectedColor);

            // Check paused text color
            TextView pausedText = activity.findViewById(R.id.paused);
            assertThat(pausedText.getCurrentTextColor()).isEqualTo(expectedColor);

            // Check button tints
            assertThat(activity.findViewById(R.id.statistics_button).getBackgroundTintList())
                .isEqualTo(ColorStateList.valueOf(expectedColor));
            assertThat(activity.findViewById(R.id.new_game_button).getBackgroundTintList())
                .isEqualTo(ColorStateList.valueOf(expectedColor));
          });
    }
  }

  @Test
  public void testStatisticsColoring() {
    Intent intent =
        new Intent(ApplicationProvider.getApplicationContext(), StatisticsActivity.class);
    intent.putExtra(StatisticsActivity.GAME_TYPE, "Classic");

    try (ActivityScenario<StatisticsActivity> scenario = ActivityScenario.launch(intent)) {
      scenario.onActivity(
          activity -> {
            // Check Action Bar Title is not colored
            CharSequence title = activity.getTitle();
            if (title instanceof SpannableString) {
              SpannableString ss = (SpannableString) title;
              ForegroundColorSpan[] spans = ss.getSpans(0, ss.length(), ForegroundColorSpan.class);
              assertThat(spans).isEmpty();
            }
          });
    }

    intent.putExtra(StatisticsActivity.GAME_TYPE, "Arcade");

    try (ActivityScenario<StatisticsActivity> scenario = ActivityScenario.launch(intent)) {
      scenario.onActivity(
          activity -> {
            // Check Action Bar Title is not colored
            CharSequence title = activity.getTitle();
            if (title instanceof SpannableString) {
              SpannableString ss = (SpannableString) title;
              ForegroundColorSpan[] spans = ss.getSpans(0, ss.length(), ForegroundColorSpan.class);
              assertThat(spans).isEmpty();
            }
          });
    }
  }
}
