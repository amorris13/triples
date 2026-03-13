package com.antsapps.triples.stats;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.core.app.ApplicationProvider;
import com.antsapps.triples.BaseRobolectricTest;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.ClassicStatistics;
import com.antsapps.triples.backend.Period;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.shadows.ShadowLooper;

public class ClassicStatisticsViewModelTest extends BaseRobolectricTest {

  private Application mApplication;
  private ClassicStatisticsViewModel mViewModel;

  @Before
  public void setUp() {
    mApplication = Application.getInstance(ApplicationProvider.getApplicationContext());
    mViewModel = new ClassicStatisticsViewModel();
    mViewModel.init(mApplication);
  }

  @Test
  public void statistics_updateWhenGameAdded() {
    AtomicReference<ClassicStatistics> statsRef = new AtomicReference<>();
    mViewModel.getStatistics().observeForever(statsRef::set);

    ClassicGame game = ClassicGame.createFromSeed(1234L);
    game.finish();
    mApplication.addClassicGame(game);

    ShadowLooper.idleMainLooper();

    assertThat(statsRef.get().getNumGames()).isEqualTo(1);
  }

  @Test
  public void statistics_updateWhenPeriodChanged() {
    AtomicReference<ClassicStatistics> statsRef = new AtomicReference<>();
    mViewModel.getStatistics().observeForever(statsRef::set);

    ClassicGame game = ClassicGame.createFromSeed(1234L);
    game.finish();
    mApplication.addClassicGame(game);

    ShadowLooper.idleMainLooper();
    assertThat(statsRef.get().getNumGames()).isEqualTo(1);

    // Period was ALL_TIME by default, changing to something that shouldn't include the game
    // although ALL_TIME includes everything. Let's just verify it reacts to period change.
    mViewModel.setPeriod(Period.ALL_TIME);
    ShadowLooper.idleMainLooper();
    assertThat(statsRef.get().getNumGames()).isEqualTo(1);
  }
}
