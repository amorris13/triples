package com.antsapps.triples.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.antsapps.triples.CardCustomizationUtils;
import com.antsapps.triples.R;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.cardsview.CardsView;
import java.util.List;

public class TripleExplanationView extends FrameLayout {

  private ExplanationCardsView mExplanationCardsView;
  private NumberIconView[] mNumberIcons = new NumberIconView[3];
  private ShapeIconView[] mShapeIcons = new ShapeIconView[3];
  private PatternIconView[] mPatternIcons = new PatternIconView[3];
  private ColorIconView[] mColorIcons = new ColorIconView[3];

  private TextView mNumberConclusion;
  private TextView mShapeConclusion;
  private TextView mPatternConclusion;
  private TextView mColorConclusion;

  private ImageView mNumberTickCross;
  private ImageView mShapeTickCross;
  private ImageView mPatternTickCross;
  private ImageView mColorTickCross;

  public TripleExplanationView(@NonNull Context context) {
    this(context, null);
  }

  public TripleExplanationView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    LayoutInflater.from(context).inflate(R.layout.triple_explanation, this, true);

    mExplanationCardsView = findViewById(R.id.explanation_cards_view);

    mNumberIcons[0] = findViewById(R.id.number_icon_0);
    mNumberIcons[1] = findViewById(R.id.number_icon_1);
    mNumberIcons[2] = findViewById(R.id.number_icon_2);

    mShapeIcons[0] = findViewById(R.id.shape_icon_0);
    mShapeIcons[1] = findViewById(R.id.shape_icon_1);
    mShapeIcons[2] = findViewById(R.id.shape_icon_2);

    mPatternIcons[0] = findViewById(R.id.pattern_icon_0);
    mPatternIcons[1] = findViewById(R.id.pattern_icon_1);
    mPatternIcons[2] = findViewById(R.id.pattern_icon_2);

    mColorIcons[0] = findViewById(R.id.color_icon_0);
    mColorIcons[1] = findViewById(R.id.color_icon_1);
    mColorIcons[2] = findViewById(R.id.color_icon_2);

    mNumberConclusion = findViewById(R.id.number_conclusion);
    mShapeConclusion = findViewById(R.id.shape_conclusion);
    mPatternConclusion = findViewById(R.id.pattern_conclusion);
    mColorConclusion = findViewById(R.id.color_conclusion);

    mNumberTickCross = findViewById(R.id.number_tick_cross);
    mShapeTickCross = findViewById(R.id.shape_tick_cross);
    mPatternTickCross = findViewById(R.id.pattern_tick_cross);
    mColorTickCross = findViewById(R.id.color_tick_cross);
  }

  public void setCardsView(CardsView cardsView) {
    mExplanationCardsView.setCardsView(cardsView);
  }

  public void setCards(List<Card> cards) {
    mExplanationCardsView.setCards(cards);

    for (int i = 0; i < 3; i++) {
      boolean hasCard = i < cards.size();
      Card card = hasCard ? cards.get(i) : null;

      mNumberIcons[i].setVisibility(hasCard ? VISIBLE : INVISIBLE);
      mShapeIcons[i].setVisibility(hasCard ? VISIBLE : INVISIBLE);
      mPatternIcons[i].setVisibility(hasCard ? VISIBLE : INVISIBLE);
      mColorIcons[i].setVisibility(hasCard ? VISIBLE : INVISIBLE);

      if (hasCard) {
        mNumberIcons[i].setNumber(card.mNumber);
        mShapeIcons[i].setShape(CardCustomizationUtils.getShapeForId(getContext(), card.mShape));
        mPatternIcons[i].setPattern(card.mPattern);
        mColorIcons[i].setColor(CardCustomizationUtils.getColorForId(getContext(), card.mColor));
      }
    }

    updateConclusions(cards);
  }

  private void updateConclusions(List<Card> cards) {
    if (cards.size() < 3) {
      mNumberConclusion.setText("");
      mShapeConclusion.setText("");
      mPatternConclusion.setText("");
      mColorConclusion.setText("");
      mNumberTickCross.setImageDrawable(null);
      mShapeTickCross.setImageDrawable(null);
      mPatternTickCross.setImageDrawable(null);
      mColorTickCross.setImageDrawable(null);
      return;
    }

    updateConclusion(
        mNumberConclusion,
        mNumberTickCross,
        cards.get(0).mNumber,
        cards.get(1).mNumber,
        cards.get(2).mNumber);
    updateConclusion(
        mShapeConclusion,
        mShapeTickCross,
        cards.get(0).mShape,
        cards.get(1).mShape,
        cards.get(2).mShape);
    updateConclusion(
        mPatternConclusion,
        mPatternTickCross,
        cards.get(0).mPattern,
        cards.get(1).mPattern,
        cards.get(2).mPattern);
    updateConclusion(
        mColorConclusion,
        mColorTickCross,
        cards.get(0).mColor,
        cards.get(1).mColor,
        cards.get(2).mColor);
  }

  private void updateConclusion(TextView text, ImageView icon, int v0, int v1, int v2) {
    if (v0 == v1 && v1 == v2) {
      text.setText(R.string.all_same);
      icon.setImageResource(R.drawable.ic_tick);
    } else if (v0 != v1 && v1 != v2 && v0 != v2) {
      text.setText(R.string.all_different);
      icon.setImageResource(R.drawable.ic_tick);
    } else {
      text.setText(R.string.two_and_one);
      icon.setImageResource(R.drawable.ic_cross);
    }
  }
}
