package com.antsapps.triples;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;

import android.content.Context;
import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.ArcadeGame;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.DailyGame;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.Game.GameState;
import com.antsapps.triples.backend.ZenGame;
import com.github.takahirom.roborazzi.RoborazziOptions;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

@RunWith(ParameterizedRobolectricTestRunner.class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = 36, qualifiers = "w412dp-h915dp-420dpi")
public class ScreenshotTest extends BaseRobolectricTest {

  @Parameters(name = "{0}")
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {"light", "notnight"},
          {"dark", "night"}
        });
  }

  private final String mMode;
  private final String mQualifier;

  public ScreenshotTest(String mode, String qualifier) {
    mMode = mode;
    mQualifier = qualifier;
  }

  private void capture(String screenName) {
    com.github.takahirom.roborazzi.RoborazziKt.captureRoboImage(
        onView(isRoot()),
        "src/test/screenshots/" + screenName + "_" + mMode + ".png",
        new RoborazziOptions());
  }

  @Test
  public void testMain() {
    try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
      capture("main");
    }
  }

  @Test
  public void testClassicGame() {
    setupClassicGame();
    Application app = Application.getInstance(ApplicationProvider.getApplicationContext());
    ClassicGame game = app.getCurrentClassicGames().iterator().next();
    Intent intent =
        new Intent(ApplicationProvider.getApplicationContext(), ClassicGameActivity.class);
    intent.putExtra(Game.ID_TAG, game.getId());
    try (ActivityScenario<ClassicGameActivity> scenario = ActivityScenario.launch(intent)) {
      capture("classic_game");
    }
  }

  @Test
  public void testArcadeGame() {
    setupArcadeGame();
    Application app = Application.getInstance(ApplicationProvider.getApplicationContext());
    ArcadeGame game = app.getCurrentArcadeGames().iterator().next();
    Intent intent =
        new Intent(ApplicationProvider.getApplicationContext(), ArcadeGameActivity.class);
    intent.putExtra(Game.ID_TAG, game.getId());
    try (ActivityScenario<ArcadeGameActivity> scenario = ActivityScenario.launch(intent)) {
      capture("arcade_game");
    }
  }

  @Test
  public void testZenGame() {
    try (ActivityScenario<ZenGameActivity> scenario =
        ActivityScenario.launch(ZenGameActivity.class)) {
      capture("zen_game");
    }
  }

  @Test
  public void testBeginnerMode() {
    Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ZenGameActivity.class);
    intent.putExtra(ZenGameActivity.IS_BEGINNER, true);
    try (ActivityScenario<ZenGameActivity> scenario = ActivityScenario.launch(intent)) {
      capture("beginner_mode");
    }
  }

  @Test
  public void testZenGame_HintAndSelected() {
    Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ZenGameActivity.class);
    try (ActivityScenario<ZenGameActivity> scenario = ActivityScenario.launch(intent)) {
      scenario.onActivity(
          activity -> {
            ZenGame game = (ZenGame) activity.getGame();
            game.addHint();
            // Try to select the first card
            List<Card> cards = game.getCardsInPlay();
            if (!cards.isEmpty()) {
              activity.mCardsView.getChildAt(0).performClick();
            }
          });
      capture("zen_game_hint_selected");
    }
  }

  @Test
  public void testDailyGame() {
    DailyGame game = setupDailyGame();
    Intent intent =
        new Intent(ApplicationProvider.getApplicationContext(), DailyGameActivity.class);
    intent.putExtra(Game.ID_TAG, game.getId());
    try (ActivityScenario<DailyGameActivity> scenario = ActivityScenario.launch(intent)) {
      capture("daily_game");
    }
  }

  @Test
  public void testClassicStatistics() {
    setupCompletedGames();
    Intent intent =
        new Intent(ApplicationProvider.getApplicationContext(), StatisticsActivity.class);
    intent.putExtra(StatisticsActivity.GAME_TYPE, "Classic");
    try (ActivityScenario<StatisticsActivity> scenario = ActivityScenario.launch(intent)) {
      capture("statistics_classic");
    }
  }

  @Test
  public void testArcadeStatistics() {
    setupCompletedGames();
    Intent intent =
        new Intent(ApplicationProvider.getApplicationContext(), StatisticsActivity.class);
    intent.putExtra(StatisticsActivity.GAME_TYPE, "Arcade");
    try (ActivityScenario<StatisticsActivity> scenario = ActivityScenario.launch(intent)) {
      capture("statistics_arcade");
    }
  }

  @Test
  public void testDailyStatistics() {
    setupCompletedGames();
    Intent intent =
        new Intent(ApplicationProvider.getApplicationContext(), StatisticsActivity.class);
    intent.putExtra(StatisticsActivity.GAME_TYPE, "Daily");
    try (ActivityScenario<StatisticsActivity> scenario = ActivityScenario.launch(intent)) {
      capture("statistics_daily");
    }
  }

  @Test
  public void testSettings() {
    try (ActivityScenario<SettingsActivity> scenario =
        ActivityScenario.launch(SettingsActivity.class)) {
      capture("settings");
    }
  }

  @Test
  public void testHelp() {
    try (ActivityScenario<HelpActivity> scenario = ActivityScenario.launch(HelpActivity.class)) {
      capture("help");
    }
  }

  private void setupClassicGame() {
    Context context = ApplicationProvider.getApplicationContext();
    Application app = Application.getInstance(context);
    app.clearAllData();
    ClassicGame game = ClassicGame.createFromSeed(12345L);
    game.begin();
    app.addClassicGame(game);
  }

  private void setupArcadeGame() {
    Context context = ApplicationProvider.getApplicationContext();
    Application app = Application.getInstance(context);
    app.clearAllData();
    ArcadeGame game = ArcadeGame.createFromSeed(12345L);
    game.begin();
    app.addArcadeGame(game);
  }

  private DailyGame setupDailyGame() {
    Context context = ApplicationProvider.getApplicationContext();
    Application app = Application.getInstance(context);
    app.clearAllData();
    DailyGame game = app.getDailyGameForDate(DailyGame.Day.forToday());
    game.begin();
    return game;
  }

  private void setupCompletedGames() {
    Context context = ApplicationProvider.getApplicationContext();
    Application app = Application.getInstance(context);
    app.clearAllData();
    Random random = new Random(12345L);

    // Classic games
    for (int i = 0; i < 5; i++) {
      ClassicGame game =
          new ClassicGame(
              -1,
              random.nextLong(),
              Lists.newArrayList(),
              Lists.newArrayList(),
              new com.antsapps.triples.backend.Deck(Lists.newArrayList()),
              60000 + random.nextInt(60000),
              new Date(System.currentTimeMillis() - i * 86400000L),
              GameState.COMPLETED,
              false);
      app.addClassicGame(game);
    }

    // Arcade games
    for (int i = 0; i < 5; i++) {
      ArcadeGame game =
          new ArcadeGame(
              -1,
              random.nextLong(),
              Lists.newArrayList(),
              Lists.newArrayList(),
              new com.antsapps.triples.backend.Deck(random),
              ArcadeGame.TIME_LIMIT_MS + 100,
              new Date(System.currentTimeMillis() - i * 86400000L),
              GameState.COMPLETED,
              10 + random.nextInt(10),
              false);
      app.addArcadeGame(game);
    }

    // Daily games
    DailyGame.Day today = DailyGame.Day.forToday();
    Calendar cal = (Calendar) today.getCalendar().clone();
    for (int i = 1; i <= 3; i++) {
      cal.add(Calendar.DAY_OF_MONTH, -1);
      DailyGame.Day day = DailyGame.Day.forCalendar((Calendar) cal.clone());
      DailyGame game = DailyGame.createFromDay(day);
      game.finish();
      app.addDailyGame(game);
    }
  }
}
