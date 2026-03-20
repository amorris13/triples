package com.antsapps.triples.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.antsapps.triples.R;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.cardsview.CardDimensionsProvider;
import com.google.common.collect.ImmutableList;
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
  private TextView mTypeSummaryText;
  private TextView mTitleView;

  private boolean mShowTicks = true;
  private boolean mShowOverallConclusion = true;
  private boolean mShowTypeSummary = false;
  private LinearLayout mHeaderRow;

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
    setClipChildren(false);
    setClipToPadding(false);
    LayoutInflater.from(context).inflate(R.layout.triple_explanation, this, true);

    mTitleView = findViewById(R.id.explanation_title);
    LinearLayout table = findViewById(R.id.explanation_table);
    table.setClipChildren(false);
    table.setClipToPadding(false);

    // Header Row
    mHeaderRow = createRow(context);
    mHeaderRow.addView(createEmptyView(context));
    mHeaderRow.addView(createHeaderTextView(context, R.string.number));
    mHeaderRow.addView(createHeaderTextView(context, R.string.shape));
    mHeaderRow.addView(createHeaderTextView(context, R.string.pattern));
    mHeaderRow.addView(createHeaderTextView(context, R.string.colour));
    table.addView(mHeaderRow);

    // Card Rows
    for (int i = 0; i < 3; i++) {
      LinearLayout cardRow = createRow(context);
      cardRow.setGravity(Gravity.CENTER_VERTICAL);
      if (i > 0) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) cardRow.getLayoutParams();
        params.topMargin = dpToPx(-13);
      }

      mCardViews[i] = new SingleScaledCardView(context);
      mCardViews[i].setLayoutParams(
          new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
      cardRow.addView(mCardViews[i]);

      for (int j = 0; j < 4; j++) {
        mPropertyIcons[i][j] = new PropertyIllustrationView(context);
        mPropertyIcons[i][j].setLayoutParams(new LinearLayout.LayoutParams(0, dpToPx(24), 1f));
        ((LinearLayout.LayoutParams) mPropertyIcons[i][j].getLayoutParams()).gravity =
            Gravity.CENTER_VERTICAL;
        mPropertyIcons[i][j].setPropertyType(mPropertyTypes[j]);
        cardRow.addView(mPropertyIcons[i][j]);
      }
      table.addView(cardRow);
    }

    // Conclusion Row
    LinearLayout conclusionRow = createRow(context);
    conclusionRow.setGravity(Gravity.CENTER_VERTICAL);

    // First cell: overall conclusion image + type summary text (one visible at a time)
    LinearLayout firstCell = new LinearLayout(context);
    firstCell.setLayoutParams(
        new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
    firstCell.setGravity(Gravity.CENTER);

    mConclusionImage = new ImageView(context);
    mConclusionImage.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(24), dpToPx(24)));
    mConclusionImage.setPadding(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2));
    firstCell.addView(mConclusionImage);

    mTypeSummaryText = new TextView(context);
    mTypeSummaryText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
    mTypeSummaryText.setGravity(Gravity.CENTER);
    mTypeSummaryText.setTypeface(null, android.graphics.Typeface.BOLD);
    mTypeSummaryText.setVisibility(GONE);
    firstCell.addView(mTypeSummaryText);

    conclusionRow.addView(firstCell);

    for (int j = 0; j < 4; j++) {
      LinearLayout container = new LinearLayout(context);
      container.setLayoutParams(
          new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
      container.setOrientation(LinearLayout.HORIZONTAL);
      container.setGravity(Gravity.CENTER);

      mConclusionTexts[j] = new TextView(context);
      mConclusionTexts[j].setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
      mConclusionTexts[j].setGravity(Gravity.CENTER);
      container.addView(mConclusionTexts[j]);

      mConclusionTicks[j] = new ImageView(context);
      mConclusionTicks[j].setLayoutParams(new LinearLayout.LayoutParams(dpToPx(12), dpToPx(12)));
      container.addView(mConclusionTicks[j]);

      conclusionRow.addView(container);
    }
    table.addView(conclusionRow);

    setCards(new java.util.LinkedHashSet<>());
  }

  public void setNaturalCardDimensionsProvider(CardDimensionsProvider cardDimensionsProvider) {
    for (SingleScaledCardView cardView : mCardViews) {
      cardView.setNaturalCardDimensionsProvider(cardDimensionsProvider);
    }
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 4; j++) {
        mPropertyIcons[i][j].setNaturalCardDimensionsProvider(cardDimensionsProvider);
      }
    }
  }

  public void setShowHeader(boolean showHeader) {
    mHeaderRow.setVisibility(showHeader ? VISIBLE : GONE);
  }

  public void setShowTicks(boolean showTicks) {
    mShowTicks = showTicks;
  }

  public void setShowOverallConclusion(boolean showOverallConclusion) {
    mShowOverallConclusion = showOverallConclusion;
  }

  public void setShowTypeSummary(boolean showTypeSummary) {
    mShowTypeSummary = showTypeSummary;
  }

  public void setCards(Set<Card> cards) {
    mCards.clear();
    mCards.addAll(cards);

    List<Card> cardList = ImmutableList.copyOf(mCards);

    for (int i = 0; i < 3; i++) {
      boolean hasCard = i < cardList.size();
      Card card = hasCard ? cardList.get(i) : null;

      mCardViews[i].setVisibility(hasCard ? VISIBLE : INVISIBLE);
      mCardViews[i].setCard(card);

      for (int j = 0; j < 4; j++) {
        mPropertyIcons[i][j].setVisibility(hasCard ? VISIBLE : INVISIBLE);
        if (hasCard) {
          mPropertyIcons[i][j].setPropertyValue(card.getValue(mPropertyTypes[j]));
        }
      }
    }

    updateConclusions(cardList);
  }

  private void updateConclusions(List<Card> cards) {
    if (cards.size() < 3) {
      for (int j = 0; j < 4; j++) {
        mConclusionTexts[j].setText("");
        mConclusionTicks[j].setImageDrawable(null);
      }
      mConclusionImage.setImageDrawable(null);
      mTypeSummaryText.setText("");
      return;
    }

    boolean valid = true;
    int sameCount = 0;
    int diffCount = 0;
    for (int j = 0; j < 4; j++) {
      int v0 = cards.get(0).getValue(mPropertyTypes[j]);
      int v1 = cards.get(1).getValue(mPropertyTypes[j]);
      int v2 = cards.get(2).getValue(mPropertyTypes[j]);
      boolean propValid = updateConclusion(mConclusionTexts[j], mConclusionTicks[j], v0, v1, v2);
      valid &= propValid;
      if (propValid) {
        if (v0 == v1 && v1 == v2) sameCount++;
        else diffCount++;
      }
    }

    if (mShowTypeSummary) {
      mConclusionImage.setVisibility(GONE);
      mTypeSummaryText.setVisibility(VISIBLE);
      String summary;
      if (sameCount == 0) {
        summary = "4d";
      } else {
        summary = sameCount + "s " + diffCount + "d";
      }
      mTypeSummaryText.setText(summary);
    } else {
      mTypeSummaryText.setVisibility(GONE);
      mConclusionImage.setVisibility(mShowOverallConclusion ? VISIBLE : INVISIBLE);
      mConclusionImage.setImageResource(valid ? R.drawable.ic_tick : R.drawable.ic_cross);
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
    icon.setVisibility(mShowTicks ? VISIBLE : GONE);
    if (v0 == v1 && v1 == v2) {
      text.setText(R.string.same);
      icon.setImageResource(R.drawable.ic_tick);
      return true;
    } else if (v0 != v1 && v1 != v2 && v0 != v2) {
      text.setText(R.string.diff);
      icon.setImageResource(R.drawable.ic_tick);
      return true;
    } else {
      text.setText(R.string.two_and_one_short);
      icon.setImageResource(R.drawable.ic_cross);
      return false;
    }
  }

  private LinearLayout createRow(Context context) {
    LinearLayout row = new LinearLayout(context);
    row.setClipChildren(false);
    row.setClipToPadding(false);
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setLayoutParams(
        new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    return row;
  }

  private View createEmptyView(Context context) {
    View view = new View(context);
    view.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 1f));
    return view;
  }

  private TextView createHeaderTextView(Context context, int textResId) {
    TextView tv = new TextView(context);
    tv.setText(textResId);
    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
    tv.setGravity(Gravity.CENTER);
    tv.setPadding(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2));
    tv.setLayoutParams(
        new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
    return tv;
  }

  private int dpToPx(float dp) {
    return (int)
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
  }
}
