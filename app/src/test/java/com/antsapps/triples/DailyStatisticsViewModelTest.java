package com.antsapps.triples;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.core.app.ApplicationProvider;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.DailyGame;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.shadows.ShadowLooper;

public class DailyStatisticsViewModelTest extends BaseRobolectricTest {

  private Application mApplication;
  private DailyStatisticsViewModel mViewModel;

  @Before
  public void setUp() {
    mApplication = Application.getInstance(ApplicationProvider.getApplicationContext());
    mViewModel = new DailyStatisticsViewModel();
    mViewModel.init(mApplication);
  }

  @Test
  public void dailyGames_areSortedByDayDescending() {
    AtomicReference<List<DailyGame>> gamesRef = new AtomicReference<>();
    mViewModel.getDailyGames().observeForever(gamesRef::set);

    DailyGame.Day day1 = new DailyGame.Day(2023, 1, 1);
    DailyGame.Day day2 = new DailyGame.Day(2023, 1, 2);
    DailyGame.Day day3 = new DailyGame.Day(2023, 1, 3);

    mApplication.addDailyGame(DailyGame.createFromDay(day1));
    mApplication.addDailyGame(DailyGame.createFromDay(day3));
    mApplication.addDailyGame(DailyGame.createFromDay(day2));

    ShadowLooper.idleMainLooper();

    List<DailyGame> games = gamesRef.get();
    assertThat(games).isNotNull();
    assertThat(games).hasSize(3);
    assertThat(games.get(0).getGameDay()).isEqualTo(day3);
    assertThat(games.get(1).getGameDay()).isEqualTo(day2);
    assertThat(games.get(2).getGameDay()).isEqualTo(day1);
  }
}
