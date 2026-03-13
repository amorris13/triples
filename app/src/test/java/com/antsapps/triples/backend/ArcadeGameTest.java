package com.antsapps.triples.backend;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import com.antsapps.triples.BaseRobolectricTest;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;
import org.junit.Test;

public class ArcadeGameTest extends BaseRobolectricTest {

  @Test
  public void testCommitTripleIncrementsOnce() {
    ArcadeGame game = ArcadeGame.createFromSeed(12345L);
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
            return new java.util.HashSet<>();
          }
        });

    List<Card> cardsInPlay = game.getCardsInPlay();
    List<Set<Card>> validTriples = Game.getAllValidTriples(cardsInPlay);
    assertThat(validTriples).isNotEmpty();

    Set<Card> triple = validTriples.get(0);
    int initialTriplesFound = game.getNumTriplesFound();

    game.onValidTripleSelected(triple);

    assertWithMessage("ArcadeGame.mNumTriplesFound should only increment by 1")
        .that(game.getNumTriplesFound())
        .isEqualTo(initialTriplesFound + 1);
  }
}
