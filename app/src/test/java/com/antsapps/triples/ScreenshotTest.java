package com.antsapps.triples;

import android.content.Context;
import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.ArcadeGame;
import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.DailyGame;
import com.antsapps.triples.backend.Game;
import com.github.takahirom.roborazzi.RoborazziOptions;
import com.github.takahirom.roborazzi.RoborazziRule;
import com.github.takahirom.roborazzi.RoborazziRule.Options;
import org.junit.Rule;
import org.junit.Test;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class ScreenshotTest extends BaseRobolectricTest {

  @Rule public RoborazziRule roborazziRule = new RoborazziRule(new Options());

  @Test
  @Config(qualifiers = "night")
  public void testMain_Dark() {
    try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
      scenario.onActivity(
          activity -> {
            com.github.takahirom.roborazzi.RoborazziKt.captureRoboImage(
                activity.getWindow().getDecorView(),
                "src/test/screenshots/main_dark.png",
                new RoborazziOptions());
          });
    }
  }

  @Test
  @Config(qualifiers = "notnight")
  public void testMain_Light() {
    try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
      scenario.onActivity(
          activity -> {
            com.github.takahirom.roborazzi.RoborazziKt.captureRoboImage(
                activity.getWindow().getDecorView(),
                "src/test/screenshots/main_light.png",
                new RoborazziOptions());
          });
    }
  }

  @Test
  @Config(qualifiers = "night")
  public void testClassicGame_Dark() {
    ClassicGame game = setupClassicGame();
    Intent intent =
        new Intent(ApplicationProvider.getApplicationContext(), ClassicGameActivity.class);
    intent.putExtra(Game.ID_TAG, game.getId());
    try (ActivityScenario<ClassicGameActivity> scenario = ActivityScenario.launch(intent)) {
      scenario.onActivity(
          activity -> {
            com.github.takahirom.roborazzi.RoborazziKt.captureRoboImage(
                activity.getWindow().getDecorView(),
                "src/test/screenshots/classic_game_dark.png",
                new RoborazziOptions());
          });
    }
  }

  @Test
  @Config(qualifiers = "notnight")
  public void testClassicGame_Light() {
    ClassicGame game = setupClassicGame();
    Intent intent =
        new Intent(ApplicationProvider.getApplicationContext(), ClassicGameActivity.class);
    intent.putExtra(Game.ID_TAG, game.getId());
    try (ActivityScenario<ClassicGameActivity> scenario = ActivityScenario.launch(intent)) {
      scenario.onActivity(
          activity -> {
            com.github.takahirom.roborazzi.RoborazziKt.captureRoboImage(
                activity.getWindow().getDecorView(),
                "src/test/screenshots/classic_game_light.png",
                new RoborazziOptions());
          });
    }
  }

  @Test
  @Config(qualifiers = "night")
  public void testArcadeGame_Dark() {
    ArcadeGame game = setupArcadeGame();
    Intent intent =
        new Intent(ApplicationProvider.getApplicationContext(), ArcadeGameActivity.class);
    intent.putExtra(Game.ID_TAG, game.getId());
    try (ActivityScenario<ArcadeGameActivity> scenario = ActivityScenario.launch(intent)) {
      scenario.onActivity(
          activity -> {
            com.github.takahirom.roborazzi.RoborazziKt.captureRoboImage(
                activity.getWindow().getDecorView(),
                "src/test/screenshots/arcade_game_dark.png",
                new RoborazziOptions());
          });
    }
  }

  @Test
  @Config(qualifiers = "notnight")
  public void testArcadeGame_Light() {
    ArcadeGame game = setupArcadeGame();
    Intent intent =
        new Intent(ApplicationProvider.getApplicationContext(), ArcadeGameActivity.class);
    intent.putExtra(Game.ID_TAG, game.getId());
    try (ActivityScenario<ArcadeGameActivity> scenario = ActivityScenario.launch(intent)) {
      scenario.onActivity(
          activity -> {
            com.github.takahirom.roborazzi.RoborazziKt.captureRoboImage(
                activity.getWindow().getDecorView(),
                "src/test/screenshots/arcade_game_light.png",
                new RoborazziOptions());
          });
    }
  }

  @Test
  @Config(qualifiers = "night")
  public void testZenGame_Dark() {
    try (ActivityScenario<ZenGameActivity> scenario =
        ActivityScenario.launch(ZenGameActivity.class)) {
      scenario.onActivity(
          activity -> {
            com.github.takahirom.roborazzi.RoborazziKt.captureRoboImage(
                activity.getWindow().getDecorView(),
                "src/test/screenshots/zen_game_dark.png",
                new RoborazziOptions());
          });
    }
  }

  @Test
  @Config(qualifiers = "notnight")
  public void testZenGame_Light() {
    try (ActivityScenario<ZenGameActivity> scenario =
        ActivityScenario.launch(ZenGameActivity.class)) {
      scenario.onActivity(
          activity -> {
            com.github.takahirom.roborazzi.RoborazziKt.captureRoboImage(
                activity.getWindow().getDecorView(),
                "src/test/screenshots/zen_game_light.png",
                new RoborazziOptions());
          });
    }
  }

  @Test
  @Config(qualifiers = "night")
  public void testDailyGame_Dark() {
    DailyGame game = setupDailyGame();
    Intent intent =
        new Intent(ApplicationProvider.getApplicationContext(), DailyGameActivity.class);
    intent.putExtra(Game.ID_TAG, game.getId());
    try (ActivityScenario<DailyGameActivity> scenario = ActivityScenario.launch(intent)) {
      scenario.onActivity(
          activity -> {
            com.github.takahirom.roborazzi.RoborazziKt.captureRoboImage(
                activity.getWindow().getDecorView(),
                "src/test/screenshots/daily_game_dark.png",
                new RoborazziOptions());
          });
    }
  }

  @Test
  @Config(qualifiers = "notnight")
  public void testDailyGame_Light() {
    DailyGame game = setupDailyGame();
    Intent intent =
        new Intent(ApplicationProvider.getApplicationContext(), DailyGameActivity.class);
    intent.putExtra(Game.ID_TAG, game.getId());
    try (ActivityScenario<DailyGameActivity> scenario = ActivityScenario.launch(intent)) {
      scenario.onActivity(
          activity -> {
            com.github.takahirom.roborazzi.RoborazziKt.captureRoboImage(
                activity.getWindow().getDecorView(),
                "src/test/screenshots/daily_game_light.png",
                new RoborazziOptions());
          });
    }
  }

  @Test
  @Config(qualifiers = "night")
  public void testStatistics_Dark() {
    try (ActivityScenario<StatisticsActivity> scenario =
        ActivityScenario.launch(StatisticsActivity.class)) {
      scenario.onActivity(
          activity -> {
            com.github.takahirom.roborazzi.RoborazziKt.captureRoboImage(
                activity.getWindow().getDecorView(),
                "src/test/screenshots/statistics_dark.png",
                new RoborazziOptions());
          });
    }
  }

  @Test
  @Config(qualifiers = "notnight")
  public void testStatistics_Light() {
    try (ActivityScenario<StatisticsActivity> scenario =
        ActivityScenario.launch(StatisticsActivity.class)) {
      scenario.onActivity(
          activity -> {
            com.github.takahirom.roborazzi.RoborazziKt.captureRoboImage(
                activity.getWindow().getDecorView(),
                "src/test/screenshots/statistics_light.png",
                new RoborazziOptions());
          });
    }
  }

  @Test
  @Config(qualifiers = "night")
  public void testSettings_Dark() {
    try (ActivityScenario<SettingsActivity> scenario =
        ActivityScenario.launch(SettingsActivity.class)) {
      scenario.onActivity(
          activity -> {
            com.github.takahirom.roborazzi.RoborazziKt.captureRoboImage(
                activity.getWindow().getDecorView(),
                "src/test/screenshots/settings_dark.png",
                new RoborazziOptions());
          });
    }
  }

  @Test
  @Config(qualifiers = "notnight")
  public void testSettings_Light() {
    try (ActivityScenario<SettingsActivity> scenario =
        ActivityScenario.launch(SettingsActivity.class)) {
      scenario.onActivity(
          activity -> {
            com.github.takahirom.roborazzi.RoborazziKt.captureRoboImage(
                activity.getWindow().getDecorView(),
                "src/test/screenshots/settings_light.png",
                new RoborazziOptions());
          });
    }
  }

  @Test
  @Config(qualifiers = "night")
  public void testHelp_Dark() {
    try (ActivityScenario<HelpActivity> scenario = ActivityScenario.launch(HelpActivity.class)) {
      scenario.onActivity(
          activity -> {
            com.github.takahirom.roborazzi.RoborazziKt.captureRoboImage(
                activity.getWindow().getDecorView(),
                "src/test/screenshots/help_dark.png",
                new RoborazziOptions());
          });
    }
  }

  @Test
  @Config(qualifiers = "notnight")
  public void testHelp_Light() {
    try (ActivityScenario<HelpActivity> scenario = ActivityScenario.launch(HelpActivity.class)) {
      scenario.onActivity(
          activity -> {
            com.github.takahirom.roborazzi.RoborazziKt.captureRoboImage(
                activity.getWindow().getDecorView(),
                "src/test/screenshots/help_light.png",
                new RoborazziOptions());
          });
    }
  }

  private ClassicGame setupClassicGame() {
    Context context = ApplicationProvider.getApplicationContext();
    Application app = Application.getInstance(context);
    ClassicGame game = ClassicGame.createFromSeed(12345L);
    app.addClassicGame(game);
    return game;
  }

  private ArcadeGame setupArcadeGame() {
    Context context = ApplicationProvider.getApplicationContext();
    Application app = Application.getInstance(context);
    ArcadeGame game = ArcadeGame.createFromSeed(12345L);
    app.addArcadeGame(game);
    return game;
  }

  private DailyGame setupDailyGame() {
    Context context = ApplicationProvider.getApplicationContext();
    Application app = Application.getInstance(context);
    return app.getDailyGameForDate(DailyGame.Day.forToday());
  }
}
