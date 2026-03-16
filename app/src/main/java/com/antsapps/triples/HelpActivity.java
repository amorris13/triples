package com.antsapps.triples;

import static com.antsapps.triples.backend.Card.MAX_VARIABLES;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.cardsview.CardDimensionsProvider;
import com.antsapps.triples.cardsview.CardView;
import com.antsapps.triples.views.TripleExplanationView;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.firebase.analytics.FirebaseAnalytics;
import java.util.Random;

public class HelpActivity extends BaseTriplesActivity {

  public static Random sRandom = new Random();

  private FirebaseAnalytics mFirebaseAnalytics;

  private TripleExplanationView mExplanationView;
  private ImmutableList<Card> mCardsShown;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.help);

    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    mExplanationView = findViewById(R.id.triple_explanation);
    mExplanationView.setNaturalCardDimensionsProvider(
        new CardDimensionsProvider() {
          @Override
          public int cardWidth() {
            return mExplanationView.getWidth() / 3;
          }

          @Override
          public int cardHeight() {
            return (int) ((mExplanationView.getWidth() / 3) / CardView.HEIGHT_OVER_WIDTH);
          }
        });

    showAnotherTriple();

    findViewById(R.id.beginner_tutorial_button)
        .setOnClickListener(
            v -> {
              Intent intent = new Intent(HelpActivity.this, ZenGameActivity.class);
              intent.putExtra(ZenGameActivity.IS_BEGINNER, true);
              startActivity(intent);
            });

    mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    mFirebaseAnalytics.logEvent(AnalyticsConstants.Event.VIEW_HELP, null);
  }

  public void showAnother(View view) {
    showAnotherTriple();
  }

  private void showAnotherTriple() {
    mExplanationView.setCards(createValidTriple());
  }

  public static ImmutableSet<Card> createValidTriple() {
    Random random = sRandom;
    Card card0 = createRandomCard(random);
    Card card1 = createRandomCard(random);
    while (card1.equals(card0)) {
      card1 = createRandomCard(random);
    }
    Card card2 =
        new Card(
            getValidProperty(card0.mNumber, card1.mNumber),
            getValidProperty(card0.mShape, card1.mShape),
            getValidProperty(card0.mPattern, card1.mPattern),
            getValidProperty(card0.mColor, card1.mColor));

    return ImmutableSet.of(card0, card1, card2);
  }

  private static Card createRandomCard(Random random) {
    return new Card(
        random.nextInt(MAX_VARIABLES),
        random.nextInt(MAX_VARIABLES),
        random.nextInt(MAX_VARIABLES),
        random.nextInt(MAX_VARIABLES));
  }

  public static int getValidProperty(int card0, int card1) {
    return (MAX_VARIABLES - ((card0 + card1) % MAX_VARIABLES)) % MAX_VARIABLES;
  }
}
