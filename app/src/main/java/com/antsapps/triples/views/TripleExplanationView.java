package com.antsapps.triples.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.antsapps.triples.R;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.TripleAnalysis;
import com.antsapps.triples.cardsview.CardDimensionsProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class TripleExplanationView extends FrameLayout {

  private Set<Card> mCards = new LinkedHashSet<>();
  private SingleScaledCardView[] mCardViews = new SingleScaledCardView[3];
  private PropertyIllustrationView[][] mPropertyIcons = new PropertyIllustrationView[3][4];
  private TextView[] mConclusionTexts = new TextView[4];
  private ImageView[] mConclusionTicks = new ImageView[4];
  private ImageView mConclusionImage;
  private TextView mTitleView;
  private View mHeaderRow;
  private TextView mTripleTypeLabel;

  private boolean mShowTicks = true;
  private boolean mShowOverallConclusion = true;

  private final Card.PropertyType[] mPropertyTypes =
      new Card.PropertyType[] {
        Card.PropertyType.NUMBER,
        Card.PropertyType.SHAPE,
        Card.PropertyType.PATTERN,
        Card.PropertyType.COLOR
      };

  public TripleExplanationView(@NonNull Context context) {
    this(context, null);
  }

  public TripleExplanationView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    LayoutInflater.from(context).inflate(R.layout.triple_explanation, this, true);

    mCardViews[0] = findViewById(R.id.card_0);
    mCardViews[1] = findViewById(R.id.card_1);
    mCardViews[2] = findViewById(R.id.card_2);

    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 4; j++) {
        int resId =
            getResources().getIdentifier("prop_" + i + "_" + j, "id", context.getPackageName());
        mPropertyIcons[i][j] = findViewById(resId);
        if (mPropertyIcons[i][j] != null) {
          mPropertyIcons[i][j].setPropertyType(mPropertyTypes[j]);
        }
      }
    }

    for (int j = 0; j < 4; j++) {
      int textId =
          getResources().getIdentifier("conclusion_text_" + j, "id", context.getPackageName());
      int tickId =
          getResources().getIdentifier("conclusion_tick_" + j, "id", context.getPackageName());
      mConclusionTexts[j] = findViewById(textId);
      mConclusionTicks[j] = findViewById(tickId);
    }

    mConclusionImage = findViewById(R.id.conclusion_image);
    mTitleView = findViewById(R.id.explanation_title);
    mHeaderRow = findViewById(R.id.header_row);
    mTripleTypeLabel = findViewById(R.id.triple_type_label);
  }

  public void setNaturalCardDimensionsProvider(CardDimensionsProvider cardDimensionsProvider) {
    for (SingleScaledCardView cardView : mCardViews) {
      if (cardView != null) {
        cardView.setNaturalCardDimensionsProvider(cardDimensionsProvider);
      }
    }
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 4; j++) {
        if (mPropertyIcons[i][j] != null) {
          mPropertyIcons[i][j].setNaturalCardDimensionsProvider(cardDimensionsProvider);
        }
      }
    }
  }

  public void setShowTicks(boolean showTicks) {
    mShowTicks = showTicks;
  }

  public void setShowOverallConclusion(boolean showOverallConclusion) {
    mShowOverallConclusion = showOverallConclusion;
  }

  public void setShowHeader(boolean show) {
    mHeaderRow.setVisibility(show ? VISIBLE : GONE);
  }

  public void setCards(Set<Card> cards) {
    mCards.clear();
    mCards.addAll(cards);

    List<Card> cardList = new ArrayList<>(mCards);
    Collections.sort(cardList);

    for (int i = 0; i < 3; i++) {
      boolean hasCard = i < cardList.size();
      Card card = hasCard ? cardList.get(i) : null;

      if (mCardViews[i] != null) {
        mCardViews[i].setVisibility(hasCard ? VISIBLE : INVISIBLE);
        mCardViews[i].setCard(card);
      }

      for (int j = 0; j < 4; j++) {
        if (mPropertyIcons[i][j] != null) {
          mPropertyIcons[i][j].setVisibility(hasCard ? VISIBLE : INVISIBLE);
          if (hasCard) {
            mPropertyIcons[i][j].setPropertyValue(card.getValue(mPropertyTypes[j]));
          }
        }
      }
    }

    updateConclusions(cardList);
  }

  private void updateConclusions(List<Card> cards) {
    if (cards.size() < 3) {
      for (int j = 0; j < 4; j++) {
        if (mConclusionTexts[j] != null) {
          mConclusionTexts[j].setText("");
        }
        if (mConclusionTicks[j] != null) {
          mConclusionTicks[j].setImageDrawable(null);
        }
      }
      if (mConclusionImage != null) {
        mConclusionImage.setImageDrawable(null);
      }
      return;
    }

    boolean valid = true;
    for (int j = 0; j < 4; j++) {
      int v0 = cards.get(0).getValue(mPropertyTypes[j]);
      int v1 = cards.get(1).getValue(mPropertyTypes[j]);
      int v2 = cards.get(2).getValue(mPropertyTypes[j]);
      valid &= updateConclusion(mConclusionTexts[j], mConclusionTicks[j], v0, v1, v2);
    }
    mConclusionImage.setVisibility(mShowOverallConclusion ? VISIBLE : INVISIBLE);
    mConclusionImage.setImageResource(valid ? R.drawable.ic_tick : R.drawable.ic_cross);

    if (valid) {
      int diffCount = TripleAnalysis.getNumDifferentProperties(new LinkedHashSet<>(cards));
      mTripleTypeLabel.setText(
          getContext().getString(R.string.analysis_triple_type_format, 4 - diffCount, diffCount));
      mTripleTypeLabel.setVisibility(VISIBLE);
    } else {
      mTripleTypeLabel.setVisibility(GONE);
    }
  }

  public void setTitle(String title) {
    if (title == null || title.isEmpty()) {
      mTitleView.setVisibility(GONE);
    } else {
      mTitleView.setText(title);
      mTitleView.setVisibility(VISIBLE);
    }
  }

  private boolean updateConclusion(TextView text, ImageView icon, int v0, int v1, int v2) {
    if (icon != null) {
      icon.setVisibility(mShowTicks ? VISIBLE : GONE);
    }
    if (v0 == v1 && v1 == v2) {
      if (text != null) text.setText(R.string.same);
      if (icon != null) icon.setImageResource(R.drawable.ic_tick);
      return true;
    } else if (v0 != v1 && v1 != v2 && v0 != v2) {
      if (text != null) text.setText(R.string.diff);
      if (icon != null) icon.setImageResource(R.drawable.ic_tick);
      return true;
    } else {
      if (text != null) text.setText(R.string.two_and_one_short);
      if (icon != null) icon.setImageResource(R.drawable.ic_cross);
      return false;
    }
  }
}
