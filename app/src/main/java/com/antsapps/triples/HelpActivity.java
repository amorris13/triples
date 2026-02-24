package com.antsapps.triples;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.OnValidTripleSelectedListener;
import com.antsapps.triples.backend.Utils;
import com.antsapps.triples.cardsview.CardsView;
import com.google.common.collect.ImmutableList;
import com.google.firebase.analytics.FirebaseAnalytics;

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

    ImmutableList<Card> newCards = Utils.createValidTriple();
    mHelpCardsView.updateCardsInPlay(newCards);
    mCardsShown = newCards;
    updateTextExplanation();

    findViewById(R.id.show_me_another).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showAnotherTriple();
        }
    });

    mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    mFirebaseAnalytics.logEvent(AnalyticsConstants.Event.VIEW_HELP, null);
  }

  @Override
  public void onValidTripleSelected(Collection<Card> validTriple) {
    showAnotherTriple();
  }

  private void showAnotherTriple() {
    ImmutableList<Card> newCards = Utils.createValidTriple();
    mHelpCardsView.updateCardsInPlay(newCards);
    mCardsShown = newCards;
    updateTextExplanation();
  }

  private void updateTextExplanation() {
    mNumberExplanation.setText(checkNumber(mCardsShown) ? R.string.all_same : R.string.all_different);
    mShapeExplanation.setText(checkShape(mCardsShown) ? R.string.all_same : R.string.all_different);
    mPatternExplanation.setText(checkPattern(mCardsShown) ? R.string.all_same : R.string.all_different);
    mColorExplanation.setText(checkColor(mCardsShown) ? R.string.all_same : R.string.all_different);
  }

  private static boolean checkNumber(ImmutableList<Card> cards) {
    return cards.get(0).mNumber == cards.get(1).mNumber && cards.get(0).mNumber == cards.get(2).mNumber;
  }

  private static boolean checkShape(ImmutableList<Card> cards) {
    return cards.get(0).mShape == cards.get(1).mShape && cards.get(0).mShape == cards.get(2).mShape;
  }

  private static boolean checkPattern(ImmutableList<Card> cards) {
    return cards.get(0).mPattern == cards.get(1).mPattern && cards.get(0).mPattern == cards.get(2).mPattern;
  }

  private static boolean checkColor(ImmutableList<Card> cards) {
    return cards.get(0).mColor == cards.get(1).mColor && cards.get(0).mColor == cards.get(2).mColor;
  }

}
