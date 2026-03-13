package com.antsapps.triples.stats;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.core.app.ApplicationProvider;
import com.antsapps.triples.BaseRobolectricTest;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.ArcadeGame;
import com.antsapps.triples.backend.ArcadeStatistics;
import com.antsapps.triples.backend.Period;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.shadows.ShadowLooper;

public class ArcadeStatisticsViewModelTest extends BaseRobolectricTest {

  private Application mApplication;
  private ArcadeStatisticsViewModel mViewModel;

  @Before
  public void setUp() {
    mApplication = Application.getInstance(ApplicationProvider.getApplicationContext());
    mViewModel = new ArcadeStatisticsViewModel();
    mViewModel.init(mApplication);
  }

  @Test
  public void statistics_updateWhenGameAdded() {
    AtomicReference<ArcadeStatistics> statsRef = new AtomicReference<>();
    mViewModel.getStatistics().observeForever(statsRef::set);

    ArcadeGame game = ArcadeGame.createFromSeed(1234L);
    game.finish();
    mApplication.addArcadeGame(game);

    ShadowLooper.idleMainLooper();

    assertThat(statsRef.get().getNumGames()).isEqualTo(1);
  }

  @Test
  public void statistics_updateWhenPeriodChanged() {
    AtomicReference<ArcadeStatistics> statsRef = new AtomicReference<>();
    mViewModel.getStatistics().observeForever(statsRef::set);

    ArcadeGame game = ArcadeGame.createFromSeed(1234L);
    game.finish();
    mApplication.addArcadeGame(game);

    ShadowLooper.idleMainLooper();
    assertThat(statsRef.get().getNumGames()).isEqualTo(1);

    mViewModel.setPeriod(Period.ALL_TIME);
    ShadowLooper.idleMainLooper();
    assertThat(statsRef.get().getNumGames()).isEqualTo(1);
  }
}
