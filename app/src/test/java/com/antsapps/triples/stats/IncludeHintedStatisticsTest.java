package com.antsapps.triples.stats;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.core.app.ApplicationProvider;
import com.antsapps.triples.BaseRobolectricTest;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.ArcadeGame;
import com.antsapps.triples.backend.ArcadeStatistics;
import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.ClassicStatistics;
import com.antsapps.triples.backend.FakeGameRenderer;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.shadows.ShadowLooper;

public class IncludeHintedStatisticsTest extends BaseRobolectricTest {

  private Application mApplication;
  private ClassicStatisticsViewModel mClassicViewModel;
  private ArcadeStatisticsViewModel mArcadeViewModel;

  @Before
  public void setUp() {
    mApplication = Application.getInstance(ApplicationProvider.getApplicationContext());
    mApplication.clearAllData();
    mClassicViewModel = new ClassicStatisticsViewModel();
    mClassicViewModel.init(mApplication);
    mArcadeViewModel = new ArcadeStatisticsViewModel();
    mArcadeViewModel.init(mApplication);
  }

  @Test
  public void classicStatistics_includeHinted() {
    AtomicReference<ClassicStatistics> statsRef = new AtomicReference<>();
    mClassicViewModel.getStatistics().observeForever(statsRef::set);

    ClassicGame game = ClassicGame.createFromSeed(1234L);
    game.setGameRenderer(new FakeGameRenderer());
    game.finish();
    game.addHint(); // This sets mHintsUsed = true
    mApplication.addClassicGame(game);

    ShadowLooper.idleMainLooper();
    assertThat(statsRef.get().getNumGames()).isEqualTo(0);

    mClassicViewModel.setIncludeHinted(true);
    ShadowLooper.idleMainLooper();
    assertThat(statsRef.get().getNumGames()).isEqualTo(1);

    mClassicViewModel.setIncludeHinted(false);
    ShadowLooper.idleMainLooper();
    assertThat(statsRef.get().getNumGames()).isEqualTo(0);
  }

  @Test
  public void arcadeStatistics_includeHinted() {
    AtomicReference<ArcadeStatistics> statsRef = new AtomicReference<>();
    mArcadeViewModel.getStatistics().observeForever(statsRef::set);

    ArcadeGame game = ArcadeGame.createFromSeed(1234L);
    game.setGameRenderer(new FakeGameRenderer());
    game.finish();
    game.addHint(); // This sets mHintsUsed = true
    mApplication.addArcadeGame(game);

    ShadowLooper.idleMainLooper();
    assertThat(statsRef.get().getNumGames()).isEqualTo(0);

    mArcadeViewModel.setIncludeHinted(true);
    ShadowLooper.idleMainLooper();
    assertThat(statsRef.get().getNumGames()).isEqualTo(1);

    mArcadeViewModel.setIncludeHinted(false);
    ShadowLooper.idleMainLooper();
    assertThat(statsRef.get().getNumGames()).isEqualTo(0);
  }
}
