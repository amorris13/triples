package com.antsapps.triples.views;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.antsapps.triples.CardCustomizationUtils;
import com.antsapps.triples.R;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.cardsview.CardView;
import com.antsapps.triples.cardsview.PatternIconView;
import com.antsapps.triples.cardsview.ShapeIconView;
import com.google.common.collect.ImmutableList;
import java.util.List;

public class TripleExplanationView extends RelativeLayout {

  private CardView[] mCardViews = new CardView[3];
  private NumberIconView[] mNumberIcons = new NumberIconView[3];
  private ShapeIconView[] mShapeIcons = new ShapeIconView[3];
  private PatternIconView[] mPatternIcons = new PatternIconView[3];
  private ColorIconView[] mColorIcons = new ColorIconView[3];

  private ImageView mNumberResult;
  private ImageView mShapeResult;
  private ImageView mPatternResult;
  private ImageView mColorResult;

  public TripleExplanationView(Context context) {
    this(context, null);
  }

  public TripleExplanationView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    LayoutInflater.from(context).inflate(R.layout.triple_explanation, this, true);

    findViewById(R.id.dismiss_button)
        .setOnClickListener(
            v -> {
              PreferenceManager.getDefaultSharedPreferences(getContext())
                  .edit()
                  .putBoolean(getContext().getString(R.string.pref_show_triple_explanation), false)
                  .apply();
              setVisibility(View.GONE);
            });

    mCardViews[0] = findViewById(R.id.card_0);
    mCardViews[1] = findViewById(R.id.card_1);
    mCardViews[2] = findViewById(R.id.card_2);

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

    mNumberResult = findViewById(R.id.number_result);
    mShapeResult = findViewById(R.id.shape_result);
    mPatternResult = findViewById(R.id.pattern_result);
    mColorResult = findViewById(R.id.color_result);
  }

  public void setCards(List<Card> cards) {
    if (cards == null) {
      cards = ImmutableList.of();
    }
    for (int i = 0; i < 3; i++) {
      if (i < cards.size()) {
        Card card = cards.get(i);
        mCardViews[i].setAlpha(1f);
        mCardViews[i].setCard(card);
        mNumberIcons[i].setVisibility(VISIBLE);
        mNumberIcons[i].setNumber(card.mNumber);
        mShapeIcons[i].setVisibility(VISIBLE);
        mShapeIcons[i].setShape(CardCustomizationUtils.getShapeForId(getContext(), card.mShape));
        mShapeIcons[i].setColor(ContextCompat.getColor(getContext(), R.color.color_text_primary));
        mPatternIcons[i].setVisibility(VISIBLE);
        mPatternIcons[i].setPattern(getPatternName(card.mPattern));
        mColorIcons[i].setVisibility(VISIBLE);
        mColorIcons[i].setColor(CardCustomizationUtils.getColorForId(getContext(), card.mColor));
      } else {
        mCardViews[i].setAlpha(0.05f);
        mCardViews[i].setCard(new Card(0, 0, 0, 0)); // Dummy card for background
        mNumberIcons[i].setVisibility(INVISIBLE);
        mShapeIcons[i].setVisibility(INVISIBLE);
        mPatternIcons[i].setVisibility(INVISIBLE);
        mColorIcons[i].setVisibility(INVISIBLE);
      }
    }

    if (cards.size() == 3) {
      mNumberResult.setVisibility(VISIBLE);
      mShapeResult.setVisibility(VISIBLE);
      mPatternResult.setVisibility(VISIBLE);
      mColorResult.setVisibility(VISIBLE);

      updateRow(mNumberResult, cards.get(0).mNumber, cards.get(1).mNumber, cards.get(2).mNumber);
      updateRow(mShapeResult, cards.get(0).mShape, cards.get(1).mShape, cards.get(2).mShape);
      updateRow(
          mPatternResult, cards.get(0).mPattern, cards.get(1).mPattern, cards.get(2).mPattern);
      updateRow(mColorResult, cards.get(0).mColor, cards.get(1).mColor, cards.get(2).mColor);
    } else {
      mNumberResult.setVisibility(INVISIBLE);
      mShapeResult.setVisibility(INVISIBLE);
      mPatternResult.setVisibility(INVISIBLE);
      mColorResult.setVisibility(INVISIBLE);
    }
  }

  private void updateRow(ImageView resultIv, int v0, int v1, int v2) {
    if (v0 == v1 && v1 == v2) {
      resultIv.setImageResource(R.drawable.ic_tick);
    } else if (v0 != v1 && v1 != v2 && v0 != v2) {
      resultIv.setImageResource(R.drawable.ic_tick);
    } else {
      resultIv.setImageResource(R.drawable.ic_cross);
    }
  }

  private String getPatternName(int patternId) {
    switch (patternId) {
      case 0:
        return "none";
      case 1:
        return CardCustomizationUtils.getShadedPattern(getContext());
      case 2:
        return "solid";
    }
    return "stripes";
  }
}
