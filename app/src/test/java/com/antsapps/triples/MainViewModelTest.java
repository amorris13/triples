package com.antsapps.triples;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.core.app.ApplicationProvider;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.ArcadeGame;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.DailyGame;
import com.antsapps.triples.backend.Game;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.shadows.ShadowLooper;

public class MainViewModelTest extends BaseRobolectricTest {

  private Application mApplication;
  private MainViewModel mViewModel;

  private static class TestRenderer implements Game.GameRenderer {
    @Override
    public void updateCardsInPlay(ImmutableList<Card> newCards) {}

    @Override
    public void addHint(Card card) {}

    @Override
    public void clearHintedCards() {}

    @Override
    public void clearSelectedCards() {}

    @Override
    public Set<Card> getSelectedCards() {
      return Collections.emptySet();
    }
  }

  @Before
  public void setUp() {
    mApplication = Application.getInstance(ApplicationProvider.getApplicationContext());
    mViewModel = new MainViewModel();
    mViewModel.init(mApplication);
  }

  @Test
  public void classicResumeState_activeGameWithTriples_isVisible() {
    AtomicReference<MainViewModel.ClassicResumeState> state = new AtomicReference<>();
    mViewModel.getClassicResumeState().observeForever(state::set);

    ClassicGame game = ClassicGame.createFromSeed(1234L);
    game.setGameRenderer(new TestRenderer());
    game.begin();
    // Find a valid triple from the cards in play
    List<Set<Card>> allTriples = Game.getAllValidTriples(game.getCardsInPlay());
    Set<Card> triple = allTriples.get(0);
    game.commitTriple(triple.toArray(new Card[0]));
    mApplication.addClassicGame(game);

    ShadowLooper.idleMainLooper();

    assertThat(state.get()).isNotNull();
    assertThat(state.get().visible).isTrue();
    assertThat(state.get().cardsRemaining).isEqualTo(game.getCardsRemaining());
  }

  @Test
  public void classicResumeState_activeGameNoTriplesFound_isNotVisible() {
    AtomicReference<MainViewModel.ClassicResumeState> state = new AtomicReference<>();
    mViewModel.getClassicResumeState().observeForever(state::set);

    ClassicGame game = ClassicGame.createFromSeed(1234L);
    game.setGameRenderer(new TestRenderer());
    game.begin();
    mApplication.addClassicGame(game);

    ShadowLooper.idleMainLooper();

    assertThat(state.get()).isNotNull();
    assertThat(state.get().visible).isFalse();
  }

  @Test
  public void arcadeResumeState_activeGameWithTriples_isVisible() {
    AtomicReference<MainViewModel.ArcadeResumeState> state = new AtomicReference<>();
    mViewModel.getArcadeResumeState().observeForever(state::set);

    ArcadeGame game = ArcadeGame.createFromSeed(1234L);
    game.setGameRenderer(new TestRenderer());
    game.begin();
    // Find a valid triple from the cards in play
    List<Set<Card>> allTriples = Game.getAllValidTriples(game.getCardsInPlay());
    Set<Card> triple = allTriples.get(0);
    game.commitTriple(triple.toArray(new Card[0]));
    mApplication.addArcadeGame(game);

    ShadowLooper.idleMainLooper();

    assertThat(state.get()).isNotNull();
    assertThat(state.get().visible).isTrue();
    assertThat(state.get().triplesFound).isEqualTo(1);
  }

  @Test
  public void dailyState_todayGame_updatesCorrectly() {
    AtomicReference<MainViewModel.DailyState> state = new AtomicReference<>();
    mViewModel.getDailyState().observeForever(state::set);

    DailyGame todayGame = mApplication.getDailyGameForDate(DailyGame.Day.forToday());
    todayGame.setGameRenderer(new TestRenderer());
    todayGame.begin();
    // Find a valid triple from the cards in play
    List<Set<Card>> allTriples = Game.getAllValidTriples(todayGame.getCardsInPlay());
    Set<Card> triple = allTriples.get(0);
    todayGame.commitTriple(triple.toArray(new Card[0]));
    mApplication.saveDailyGame(todayGame);

    ShadowLooper.idleMainLooper();

    assertThat(state.get()).isNotNull();
    assertThat(state.get().triplesFound).isEqualTo(1);
    assertThat(state.get().totalTriples).isEqualTo(todayGame.getTotalTriplesCount());
    assertThat(state.get().completed).isFalse();
  }

  @Test
  public void completedCounts_updateWhenGamesCompleted() {
    AtomicReference<Integer> classicCount = new AtomicReference<>();
    AtomicReference<Integer> arcadeCount = new AtomicReference<>();
    mViewModel.getClassicCompletedCount().observeForever(classicCount::set);
    mViewModel.getArcadeCompletedCount().observeForever(arcadeCount::set);

    ClassicGame classicGame = ClassicGame.createFromSeed(1234L);
    classicGame.finish();
    mApplication.addClassicGame(classicGame);

    ArcadeGame arcadeGame = ArcadeGame.createFromSeed(5678L);
    arcadeGame.finish();
    mApplication.addArcadeGame(arcadeGame);

    ShadowLooper.idleMainLooper();

    assertThat(classicCount.get()).isEqualTo(1);
    assertThat(arcadeCount.get()).isEqualTo(1);
  }
}
