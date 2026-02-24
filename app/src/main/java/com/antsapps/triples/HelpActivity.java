package com.antsapps.triples;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import android.content.Intent;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.OnValidTripleSelectedListener;
import com.antsapps.triples.cardsview.CardsView;
import com.google.common.collect.ImmutableList;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Random;

import static com.antsapps.triples.backend.Card.MAX_VARIABLES;

public class HelpActivity extends Activity implements OnValidTripleSelectedListener {

  private FirebaseAnalytics mFirebaseAnalytics;

  private CardsView mHelpCardsView;
  private ImmutableList<Card> mCardsShown;
  private TextView mNumberExplanation;
  private TextView mShapeExplanation;
  private TextView mPatternExplanation;
  private TextView mColorExplanation;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.help);

    getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

    mHelpCardsView = (CardsView) findViewById(R.id.cards_view);
    mHelpCardsView.setOnValidTripleSelectedListener(this);

    mNumberExplanation = (TextView) findViewById(R.id.number_explanation);
    mShapeExplanation = (TextView) findViewById(R.id.shape_explanation);
    mPatternExplanation = (TextView) findViewById(R.id.pattern_explanation);
    mColorExplanation = (TextView) findViewById(R.id.color_explanation);

    ImmutableList<Card> newCards = createValidTriple();
    mHelpCardsView.updateCardsInPlay(newCards);
    mCardsShown = newCards;
    updateTextExplanation();

    mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    mFirebaseAnalytics.logEvent(AnalyticsConstants.Event.VIEW_HELP, null);
  }

  @Override
  public void onValidTripleSelected(Collection<Card> validTriple) {
    showAnotherTriple();
  }

  public void showAnother(View view) {
    showAnotherTriple();
  }

  public void playTutorial(View view) {
    ClassicGame game = ClassicGame.createTutorial(System.currentTimeMillis());
    Application.getInstance(getApplication()).addClassicGame(game);
    Intent newGameIntent = new Intent(getBaseContext(), ClassicGameActivity.class);
    newGameIntent.putExtra(Game.ID_TAG, game.getId());
    startActivity(newGameIntent);
    finish();
  }

  private void showAnotherTriple() {
    ImmutableList<Card> newCards = createValidTriple();
    mHelpCardsView.updateCardsInPlay(newCards);
    mCardsShown = newCards;
    updateTextExplanation();
  }

  private void updateTextExplanation() {
    try {
      mNumberExplanation.setText(
          checkField(Card.class.getField("mNumber"), mCardsShown)
              ? R.string.all_same
              : R.string.all_different);
      mShapeExplanation.setText(
          checkField(Card.class.getField("mShape"), mCardsShown)
              ? R.string.all_same
              : R.string.all_different);
      mPatternExplanation.setText(
          checkField(Card.class.getField("mPattern"), mCardsShown)
              ? R.string.all_same
              : R.string.all_different);
      mColorExplanation.setText(
          checkField(Card.class.getField("mColor"), mCardsShown)
              ? R.string.all_same
              : R.string.all_different);
    } catch (NoSuchFieldException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /** @return true if all the same, false otherwise. */
  private static boolean checkField(Field property, ImmutableList<Card> cards) {
    try {
      return property.getInt(cards.get(0)) == property.getInt(cards.get(1))
          && property.getInt(cards.get(0)) == property.getInt(cards.get(2));
    } catch (Exception e) {
      return false;
    }
  }

  public static ImmutableList<Card> createValidTriple() {
    Random random = new Random();
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

    return ImmutableList.of(card0, card1, card2);
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
