package com.antsapps.triples.backend;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DailyGameHintTest {

  private static class DummyRenderer implements Game.GameRenderer {
    Set<Card> selected = Sets.newHashSet();
    Set<Card> hinted = Sets.newHashSet();
    @Override public void updateCardsInPlay(ImmutableList<Card> newCards) {}
    @Override public void addHint(Card card) {
        hinted.add(card);
    }
    @Override public void clearHintedCards() { hinted.clear(); }
    @Override public Set<Card> getSelectedCards() { return selected; }
    @Override public void clearSelectedCards() { selected.clear(); }
  }

  @Test
  public void addHint_hintsOnlyOneCardAtATime() {
    DailyGame game = DailyGame.createFromSeed(12345L);
    DummyRenderer renderer = new DummyRenderer();
    game.setGameRenderer(renderer);

    game.addHint();
    assertThat(renderer.hinted).hasSize(1);

    game.addHint();
    assertThat(renderer.hinted).hasSize(2);

    game.addHint();
    assertThat(renderer.hinted).hasSize(3);

    // Verify it's a valid triple
    assertThat(Game.isValidTriple(renderer.hinted)).isTrue();
  }
}
