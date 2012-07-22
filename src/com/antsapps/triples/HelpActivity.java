package com.antsapps.triples;

import static com.antsapps.triples.backend.Card.MAX_VARIABLES;

import java.lang.reflect.Field;
import java.util.Random;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.antsapps.triples.backend.Card;
import com.google.common.collect.ImmutableList;

public class HelpActivity extends SherlockActivity {

  private HelpCardsView mHelpCardsView;
  private ImmutableList<Card> mCardsShown;
  private TextView mNumberExplanation;
  private TextView mShapeExplanation;
  private TextView mPatternExplanation;
  private TextView mColorExplanation;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.help);

    mHelpCardsView = (HelpCardsView) findViewById(R.id.cards_view);

    mNumberExplanation = (TextView) findViewById(R.id.number_explanation);
    mShapeExplanation = (TextView) findViewById(R.id.shape_explanation);
    mPatternExplanation = (TextView) findViewById(R.id.pattern_explanation);
    mColorExplanation = (TextView) findViewById(R.id.color_explanation);

    ImmutableList<Card> newCards = createValidTriple();
    mHelpCardsView.onUpdateCardsInPlay(newCards, ImmutableList.<Card> of(), 0);
    mCardsShown = newCards;
    updateTextExplanation();

    ActionBar actionBar = getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    switch (item.getItemId()) {
      case android.R.id.home:
        // app icon in action bar clicked; go up one level
        Intent intent = new Intent(this, GameListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  public void showAnother(View view) {
    ImmutableList<Card> newCards = createValidTriple();
    mHelpCardsView.onUpdateCardsInPlay(newCards, mCardsShown, 0);
    mCardsShown = newCards;
    updateTextExplanation();
  }

  private void updateTextExplanation() {
    try {
      mNumberExplanation.setText(checkField(
          Card.class.getField("mNumber"),
          mCardsShown) ? R.string.all_same : R.string.all_different);
      mShapeExplanation.setText(checkField(
          Card.class.getField("mShape"),
          mCardsShown) ? R.string.all_same : R.string.all_different);
      mPatternExplanation.setText(checkField(
          Card.class.getField("mPattern"),
          mCardsShown) ? R.string.all_same : R.string.all_different);
      mColorExplanation.setText(checkField(
          Card.class.getField("mColor"),
          mCardsShown) ? R.string.all_same : R.string.all_different);
    } catch (NoSuchFieldException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * @return true if all the same, false otherwise.
   */
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
    Card card0 = new Card(random.nextInt(MAX_VARIABLES),
        random.nextInt(MAX_VARIABLES), random.nextInt(MAX_VARIABLES),
        random.nextInt(MAX_VARIABLES));
    Card card1 = new Card(random.nextInt(MAX_VARIABLES),
        random.nextInt(MAX_VARIABLES), random.nextInt(MAX_VARIABLES),
        random.nextInt(MAX_VARIABLES));
    Card card2 = new Card(getValidProperty(card0.mNumber, card1.mNumber),
        getValidProperty(card0.mShape, card1.mShape), getValidProperty(
            card0.mPattern,
            card1.mPattern), getValidProperty(card0.mColor, card1.mColor));

    return ImmutableList.of(card0, card1, card2);
  }

  public static int getValidProperty(int card0, int card1) {
    return (MAX_VARIABLES - ((card0 + card1) % MAX_VARIABLES)) % MAX_VARIABLES;
  }
}
