package com.antsapps.triples.backend;

import static com.google.common.truth.Truth.assertThat;

import com.antsapps.triples.BaseRobolectricTest;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class GameReconstructionTest extends BaseRobolectricTest {

  @Test
  public void testClassicGameReconstruction() {
    ClassicGame game = ClassicGame.createFromSeed(12345L);
    game.setGameRenderer(new FakeGameRenderer());
    game.begin();

    // Play a few triples
    for (int i = 0; i < 5; i++) {
      playATriple(game);
    }

    List<TripleAnalysis> analysis = game.reconstruct();
    assertThat(analysis).hasSize(5);

    // Verify each step matches
    verifyReconstruction(game, analysis);

    List<Card> finalBoard = game.getFinalBoardState();
    assertThat(finalBoard).containsExactlyElementsIn(game.getCardsInPlay());
  }

  @Test
  public void testArcadeGameReconstruction() {
    ArcadeGame game = ArcadeGame.createFromSeed(54321L);
    game.setGameRenderer(new FakeGameRenderer());
    game.begin();

    // Play a few triples (Arcade recycles cards)
    for (int i = 0; i < 10; i++) {
      playATriple(game);
    }

    List<TripleAnalysis> analysis = game.reconstruct();
    assertThat(analysis).hasSize(10);

    verifyReconstruction(game, analysis);

    List<Card> finalBoard = game.getFinalBoardState();
    assertThat(finalBoard).containsExactlyElementsIn(game.getCardsInPlay());
  }

  @Test
  public void testZenGameReconstruction() {
    ZenGame game = ZenGame.createFromSeed(98765L, false);
    game.setGameRenderer(new FakeGameRenderer());
    game.begin();

    for (int i = 0; i < 5; i++) {
      playATriple(game);
    }

    List<TripleAnalysis> analysis = game.reconstruct();
    assertThat(analysis).hasSize(5);
  }

  @Test
  public void testDailyGameReconstruction() {
    DailyGame.Day day = new DailyGame.Day(2023, 10, 27);
    DailyGame game = DailyGame.createFromDay(day);
    game.setGameRenderer(new FakeGameRenderer());
    game.begin();

    List<Set<Card>> allTriples = Game.getAllValidTriples(game.getCardsInPlay());
    game.commitTriple(allTriples.get(0).toArray(new Card[0]));
    game.commitTriple(allTriples.get(1).toArray(new Card[0]));

    List<TripleAnalysis> analysis = game.reconstruct();
    assertThat(analysis).hasSize(2);

    // Daily game board doesn't change
    for (TripleAnalysis step : analysis) {
      assertThat(step.boardState).containsExactlyElementsIn(game.getCardsInPlay());
    }

    assertThat(game.getFinalBoardState()).isNull();
  }

  private void playATriple(Game game) {
    List<Card> cardsInPlay = game.getCardsInPlay();
    Set<Card> triple = Game.getAValidTriple(cardsInPlay, Sets.newHashSet());
    if (triple == null) {
      StringBuilder sb = new StringBuilder();
      sb.append("No valid triple found on board. Cards in play: [");
      for (Card c : cardsInPlay) {
        sb.append(c).append(", ");
      }
      sb.append("]");
      throw new RuntimeException(sb.toString());
    }
    game.commitTriple(triple.toArray(new Card[0]));
  }

  private void verifyReconstruction(Game game, List<TripleAnalysis> analysis) {
    List<Set<Card>> foundTriples = game.getFoundTriples();
    List<Long> findTimes = game.getTripleFindTimes();

    for (int i = 0; i < analysis.size(); i++) {
      TripleAnalysis step = analysis.get(i);
      assertThat(step.foundTriple).isEqualTo(foundTriples.get(i));
      assertThat(step.findTime).isEqualTo(findTimes.get(i));
    }
  }
}
