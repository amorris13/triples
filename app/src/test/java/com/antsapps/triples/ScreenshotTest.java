package com.antsapps.triples;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.ArcadeGame;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.DailyGame;
import com.antsapps.triples.backend.Deck;
import com.antsapps.triples.backend.FakeTimeProvider;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.Game.GameState;
import com.antsapps.triples.backend.ZenGame;
import com.github.takahirom.roborazzi.RoborazziOptions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

@RunWith(ParameterizedRobolectricTestRunner.class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = 36)
public class ScreenshotTest extends BaseRobolectricTest {

  public static final long INITIAL_TIME_MILLIS = 1873794536000L; // 18 May 2029
  private FakeTimeProvider mFakeTimeProvider;

  @Parameters(name = "{0}")
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {"light", "w412dp-h915dp-notnight-420dpi"},
          {"dark", "w412dp-h915dp-night-420dpi"}
        });
  }

  private final String mMode;
  private final String mQualifier;

  public ScreenshotTest(String mode, String qualifier) {
    mMode = mode;
    mQualifier = qualifier;
  }

  @Before
  public void setUp() {
    RuntimeEnvironment.setQualifiers(mQualifier);
    if (mMode.equals("dark")) {
      AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    } else {
      AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    Application.sSeed = 12345L;
    HelpActivity.sRandom = new Random(12345L);
    mFakeTimeProvider = new FakeTimeProvider(INITIAL_TIME_MILLIS);
    Application.setTimeProvider(mFakeTimeProvider);
  }

  private void capture(String screenName) {
    // We use the view capture to avoid ambiguity with Compose overloads in some environments
    onView(isRoot())
        .perform(
            new androidx.test.espresso.ViewAction() {
              @Override
              public org.hamcrest.Matcher<View> getConstraints() {
                return isRoot();
              }

              @Override
              public String getDescription() {
                return "capture screenshot";
              }

              @Override
              public void perform(androidx.test.espresso.UiController uiController, View view) {
                com.github.takahirom.roborazzi.RoborazziKt.captureRoboImage(
                    view,
                    "src/test/screenshots/" + screenName + "_" + mMode + ".png",
                    new RoborazziOptions());
              }
            });
  }

  @Test
  public void testClassicStatistics_PartialAnalysis() {
    Context context = ApplicationProvider.getApplicationContext();
    Application app = Application.getInstance(context);
    app.clearAllData();

    // 1 game with analysis
    ClassicGame game1 = ClassicGame.createFromSeed(111L);
    game1.begin();
    findAndCommitTriples(game1, 27);
    app.addClassicGame(game1);

    // 1 game without analysis (e.g. from an older version where we didn't save found triples)
    // We can simulate this by creating a completed game with empty foundTriples
    ClassicGame game2 =
        new ClassicGame(
            -1,
            222L,
            Lists.newArrayList(),
            Lists.newArrayList(),
            new Deck(new Random(222L)),
            123456L,
            Application.getTimeProvider().now(),
            GameState.COMPLETED,
            false,
            Lists.newArrayList());
    app.addClassicGame(game2);

    Intent intent =
        new Intent(ApplicationProvider.getApplicationContext(), StatisticsActivity.class);
    intent.putExtra(StatisticsActivity.GAME_TYPE, "Classic");
    try (ActivityScenario<StatisticsActivity> scenario = ActivityScenario.launch(intent)) {
      onView(withText("Analysis")).perform(click());
      capture("statistics_classic_partial_analysis");
    }
  }

  @Test
  public void testMain() {
    try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
      capture("main");
    }
  }

  @Test
  public void testClassicGame() {
    setupClassicGame(false);
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
  public void testClassicGame_Completed() {
    setupClassicGame(true);
    Application app = Application.getInstance(ApplicationProvider.getApplicationContext());
    ClassicGame game = app.getCompletedClassicGames().iterator().next();
    Intent intent =
        new Intent(ApplicationProvider.getApplicationContext(), ClassicGameActivity.class);
    intent.putExtra(Game.ID_TAG, game.getId());
    try (ActivityScenario<ClassicGameActivity> scenario = ActivityScenario.launch(intent)) {
      capture("classic_game_completed");
    }
  }

  @Test
  public void testClassicGame_Analysis() {
    setupClassicGame(true);
    Application app = Application.getInstance(ApplicationProvider.getApplicationContext());
    ClassicGame game = app.getCompletedClassicGames().iterator().next();
    Intent intent =
        new Intent(ApplicationProvider.getApplicationContext(), GameAnalysisActivity.class);
    intent.putExtra(GameAnalysisActivity.GAME_ID, game.getId());
    intent.putExtra(GameAnalysisActivity.GAME_TYPE, "Classic");
    try (ActivityScenario<GameAnalysisActivity> scenario = ActivityScenario.launch(intent)) {
      // Need to wait for any background work (like reconstruction) if applicable
      capture("classic_game_analysis");
    }
  }

  @Test
  public void testViewBoard() {
    setupClassicGame(true);
    Application app = Application.getInstance(ApplicationProvider.getApplicationContext());
    ClassicGame game = app.getCompletedClassicGames().iterator().next();

    Intent intent =
        new Intent(ApplicationProvider.getApplicationContext(), BoardHistoryActivity.class);
    intent.putExtra(BoardHistoryActivity.EXTRA_GAME_ID, game.getId());
    intent.putExtra(BoardHistoryActivity.EXTRA_GAME_TYPE, "Classic");
    intent.putExtra(BoardHistoryActivity.EXTRA_INITIAL_STEP, 1);
    try (ActivityScenario<BoardHistoryActivity> scenario = ActivityScenario.launch(intent)) {
      capture("view_board");
    }
  }

  @Test
  public void testArcadeGame() {
    setupArcadeGame(false);
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
  public void testArcadeGame_Completed() {
    setupArcadeGame(true);
    Application app = Application.getInstance(ApplicationProvider.getApplicationContext());
    ArcadeGame game = app.getCompletedArcadeGames().iterator().next();
    Intent intent =
        new Intent(ApplicationProvider.getApplicationContext(), ArcadeGameActivity.class);
    intent.putExtra(Game.ID_TAG, game.getId());
    try (ActivityScenario<ArcadeGameActivity> scenario = ActivityScenario.launch(intent)) {
      capture("arcade_game_completed");
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
      scenario.onActivity(
          activity -> {
            ZenGame game = (ZenGame) activity.getGame();
            List<Card> cards = game.getCardsInPlay();
            if (!cards.isEmpty()) {
              activity.mCardsView.getChildAt(0).performClick();
              activity.mCardsView.getChildAt(1).performClick();
              activity.mCardsView.getChildAt(2).performClick();
            }
          });
      capture("beginner_mode");
    }
  }

  @Test
  public void testZenGame_Hint() {
    Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ZenGameActivity.class);
    try (ActivityScenario<ZenGameActivity> scenario = ActivityScenario.launch(intent)) {
      scenario.onActivity(
          activity -> {
            ZenGame game = (ZenGame) activity.getGame();
            game.addHint();
          });
      capture("zen_game_hint");
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
    DailyGame game = setupDailyGame(false);
    Intent intent =
        new Intent(ApplicationProvider.getApplicationContext(), DailyGameActivity.class);
    intent.putExtra(Game.ID_TAG, game.getId());
    try (ActivityScenario<DailyGameActivity> scenario = ActivityScenario.launch(intent)) {
      capture("daily_game");
    }
  }

  @Test
  public void testDailyGame_Completed() {
    DailyGame game = setupDailyGame(true);
    Intent intent =
        new Intent(ApplicationProvider.getApplicationContext(), DailyGameActivity.class);
    intent.putExtra(Game.ID_TAG, game.getId());
    try (ActivityScenario<DailyGameActivity> scenario = ActivityScenario.launch(intent)) {
      capture("daily_game_completed");
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
  public void testClassicStatistics_Analysis() {
    setupCompletedGames();
    Intent intent =
        new Intent(ApplicationProvider.getApplicationContext(), StatisticsActivity.class);
    intent.putExtra(StatisticsActivity.GAME_TYPE, "Classic");
    try (ActivityScenario<StatisticsActivity> scenario = ActivityScenario.launch(intent)) {
      onView(withText("Analysis")).perform(click());
      capture("statistics_classic_analysis");
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
  public void testArcadeStatistics_Analysis() {
    setupCompletedGames();
    Intent intent =
        new Intent(ApplicationProvider.getApplicationContext(), StatisticsActivity.class);
    intent.putExtra(StatisticsActivity.GAME_TYPE, "Arcade");
    try (ActivityScenario<StatisticsActivity> scenario = ActivityScenario.launch(intent)) {
      onView(withText("Analysis")).perform(click());
      capture("statistics_arcade_analysis");
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

  @Test
  public void testAnalysisAggregateRetention() {
    Context context = ApplicationProvider.getApplicationContext();
    Application app = Application.getInstance(context);
    app.clearAllData();

    // 1. Complete one game
    ClassicGame game1 = ClassicGame.createFromSeed(111L);
    game1.begin();
    findAndCommitTriples(game1, 27);
    app.addClassicGame(game1);

    // 2. Verify it has triples
    assert !game1.getFoundTriples().isEmpty();

    // 3. Start a new game
    ClassicGame game2 = ClassicGame.createFromSeed(222L);
    game2.begin();
    app.addClassicGame(game2);

    // 4. Verify game1 STILL has its triples (this was the bug)
    ClassicGame retrievedGame1 = app.getClassicGame(game1.getId());
    assert !retrievedGame1.getFoundTriples().isEmpty()
        : "Found triples were cleared when starting a new game!";
  }

  private void setupClassicGame(boolean completed) {
    Context context = ApplicationProvider.getApplicationContext();
    Application app = Application.getInstance(context);
    app.clearAllData();
    ClassicGame game = ClassicGame.createFromSeed(12345L);
    game.begin();
    if (completed) {
      findAndCommitTriples(game, 27);
    } else {
      findAndCommitTriples(game, 2);
    }
    app.addClassicGame(game);
  }

  private void setupArcadeGame(boolean completed) {
    Context context = ApplicationProvider.getApplicationContext();
    Application app = Application.getInstance(context);
    app.clearAllData();
    Random random = new Random(12345L);
    if (completed) {
      int numFound = 15;
      List<Long> findTimes = Lists.newArrayList();
      long time = 0;
      for (int i = 0; i < numFound; i++) {
        time += 3000 + random.nextInt(5000);
        findTimes.add(time);
      }
      ArcadeGame game =
          new ArcadeGame(
              -1,
              12345L,
              Lists.newArrayList(),
              findTimes,
              new Deck(new Random(12345L)),
              ArcadeGame.TIME_LIMIT_MS + 100,
              Application.getTimeProvider().now(),
              GameState.COMPLETED,
              numFound,
              false,
              Lists.newArrayList(),
              ArcadeGame.ArcadeStyle.FIXED);
      app.addArcadeGame(game);
    } else {
      ArcadeGame game = ArcadeGame.createFromSeed(12345L);
      game.begin();
      findAndCommitTriples(game, 2);
      app.addArcadeGame(game);
    }
  }

  private DailyGame setupDailyGame(boolean completed) {
    Context context = ApplicationProvider.getApplicationContext();
    Application app = Application.getInstance(context);
    app.clearAllData();
    DailyGame game = app.getDailyGameForDate(DailyGame.Day.forToday());
    game.begin();
    if (completed) {
      findAndCommitTriples(game, 100);
    } else {
      findAndCommitTriples(game, 2);
    }
    return game;
  }

  private void forceCompleted(Game game) {
    try {
      java.lang.reflect.Field field = Game.class.getDeclaredField("mGameState");
      field.setAccessible(true);
      field.set(game, GameState.COMPLETED);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void findAndCommitTriples(Game game, int count) {
    game.setGameRenderer(
        new Game.GameRenderer() {
          @Override
          public void updateCardsInPlay(com.google.common.collect.ImmutableList<Card> newCards) {}

          @Override
          public void addHint(Card card) {}

          @Override
          public void clearHintedCards() {}

          @Override
          public void clearSelectedCards() {}

          @Override
          public Set<Card> getSelectedCards() {
            return Sets.newHashSet();
          }
        });
    Random random = new Random(54321L);
    for (int i = 0; i < count; i++) {
      if (game.getGameState() == GameState.COMPLETED) break;

      // Advance time for realistic find times
      mFakeTimeProvider.advanceTime(3000 + random.nextInt(5000));

      Set<Card> triple = null;
      if (game instanceof DailyGame) {
        List<Set<Card>> all = Game.getAllValidTriples(game.getCardsInPlay());
        for (Set<Card> t : all) {
          if (!((DailyGame) game).getFoundTriples().contains(t)) {
            triple = t;
            break;
          }
        }
      } else {
        triple = Game.getAValidTriple(game.getCardsInPlay(), Sets.newHashSet());
      }

      if (triple != null) {
        game.commitTriple(triple.toArray(new Card[0]));
      } else {
        break;
      }
    }
    game.setGameRenderer(null);
  }

  private void setupCompletedGames() {
    Context context = ApplicationProvider.getApplicationContext();
    Application app = Application.getInstance(context);
    app.clearAllData();
    Random random = new Random(12345L);

    // Classic games
    for (int i = 0; i < 5; i++) {
      ClassicGame game = ClassicGame.createFromSeed(random.nextLong());
      game.begin();
      findAndCommitTriples(game, 27);
      app.addClassicGame(game);
    }

    // Arcade games
    for (int i = 0; i < 5; i++) {
      ArcadeGame game = ArcadeGame.createFromSeed(random.nextLong());
      game.begin();
      findAndCommitTriples(game, 10);
      forceCompleted(game);
      app.addArcadeGame(game);
    }

    // Daily games
    DailyGame.Day today = DailyGame.Day.forToday();
    Calendar cal = (Calendar) today.getCalendar().clone();
    for (int i = 1; i <= 3; i++) {
      cal.add(Calendar.DAY_OF_MONTH, -1);
      DailyGame.Day day = DailyGame.Day.forCalendar((Calendar) cal.clone());
      DailyGame game = app.getDailyGameForDate(day);
      game.begin();
      findAndCommitTriples(game, 100);
    }
  }
}
