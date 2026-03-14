package com.antsapps.triples.backend;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DailyGameHintTest {

  @Test
  public void addHint_hintsOnlyOneCardAtATime() {
    DailyGame game = DailyGame.createFromDay(new DailyGame.Day(2026, 03, 11));
    FakeGameRenderer renderer = new FakeGameRenderer();
    game.setGameRenderer(renderer);

    game.addHint();
    assertThat(renderer.mHintedCards).hasSize(1);

    game.addHint();
    assertThat(renderer.mHintedCards).hasSize(2);

    game.addHint();
    assertThat(renderer.mHintedCards).hasSize(3);

    // Verify it's a valid triple
    assertThat(Game.isValidTriple(renderer.mHintedCards)).isTrue();
  }
}
