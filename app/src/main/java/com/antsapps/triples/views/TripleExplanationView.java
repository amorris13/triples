package com.antsapps.triples.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.antsapps.triples.R;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.cardsview.CardsView;
import java.util.List;

public class TripleExplanationView extends FrameLayout {

  private static class PropertyRow {
    PropertyIllustrationView[] icons = new PropertyIllustrationView[3];
    TextView conclusion;
    ImageView tickCross;
    PropertyIllustrationView.PropertyType type;

    PropertyRow(PropertyIllustrationView.PropertyType type) {
      this.type = type;
    }
  }

  private SingleScaledCardView[] mCardViews = new SingleScaledCardView[3];
  private PropertyRow[] mPropertyRows =
      new PropertyRow[] {
        new PropertyRow(PropertyIllustrationView.PropertyType.NUMBER),
        new PropertyRow(PropertyIllustrationView.PropertyType.SHAPE),
        new PropertyRow(PropertyIllustrationView.PropertyType.PATTERN),
        new PropertyRow(PropertyIllustrationView.PropertyType.COLOR)
      };

  private CardsView mCardsView;

  public TripleExplanationView(@NonNull Context context) {
    this(context, null);
  }

  public TripleExplanationView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    LayoutInflater.from(context).inflate(R.layout.triple_explanation, this, true);

    TableLayout table = findViewById(R.id.table_layout);
    mCardViews[0] = findViewById(R.id.card_0);
    mCardViews[1] = findViewById(R.id.card_1);
    mCardViews[2] = findViewById(R.id.card_2);

    for (PropertyRow row : mPropertyRows) {
      table.addView(createRow(context, row));
    }
  }

  private TableRow createRow(Context context, PropertyRow propertyRow) {
    TableRow row = new TableRow(context);
    row.setLayoutParams(
        new TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT,
            (int) (48 * context.getResources().getDisplayMetrics().density)));
    row.setGravity(Gravity.CENTER_VERTICAL);

    // Label
    TextView label = new TextView(context);
    label.setLayoutParams(
        new TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
    label.setPadding(0, 0, (int) (8 * context.getResources().getDisplayMetrics().density), 0);
    label.setTextSize(14);
    switch (propertyRow.type) {
      case NUMBER:
        label.setText(R.string.number);
        break;
      case SHAPE:
        label.setText(R.string.shape);
        break;
      case PATTERN:
        label.setText(R.string.pattern);
        break;
      case COLOR:
        label.setText(R.string.colour);
        break;
    }
    row.addView(label);

    // Illustrations
    for (int i = 0; i < 3; i++) {
      PropertyIllustrationView illustration = new PropertyIllustrationView(context);
      TableRow.LayoutParams lp =
          new TableRow.LayoutParams(
              0, (int) (40 * context.getResources().getDisplayMetrics().density));
      illustration.setLayoutParams(lp);
      propertyRow.icons[i] = illustration;
      row.addView(illustration);
    }

    // Conclusion Container
    LinearLayout conclusionContainer = new LinearLayout(context);
    conclusionContainer.setLayoutParams(
        new TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
    conclusionContainer.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
    conclusionContainer.setOrientation(LinearLayout.HORIZONTAL);

    propertyRow.conclusion = new TextView(context);
    propertyRow.conclusion.setLayoutParams(
        new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    propertyRow.conclusion.setPadding(
        (int) (4 * context.getResources().getDisplayMetrics().density), 0, 0, 0);
    propertyRow.conclusion.setTextSize(12);
    conclusionContainer.addView(propertyRow.conclusion);

    propertyRow.tickCross = new ImageView(context);
    propertyRow.tickCross.setLayoutParams(
        new LinearLayout.LayoutParams(
            (int) (20 * context.getResources().getDisplayMetrics().density),
            (int) (20 * context.getResources().getDisplayMetrics().density)));
    propertyRow.tickCross.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    conclusionContainer.addView(propertyRow.tickCross);

    row.addView(conclusionContainer);

    return row;
  }

  public void setCardsView(CardsView cardsView) {
    mCardsView = cardsView;
    for (SingleScaledCardView cardView : mCardViews) {
      cardView.setCardsView(cardsView);
    }
    for (PropertyRow row : mPropertyRows) {
      for (PropertyIllustrationView icon : row.icons) {
        icon.setCardsView(cardsView);
      }
    }
  }

  public void setCards(List<Card> cards) {
    for (int i = 0; i < 3; i++) {
      boolean hasCard = i < cards.size();
      Card card = hasCard ? cards.get(i) : null;

      mCardViews[i].setVisibility(hasCard ? VISIBLE : INVISIBLE);
      mCardViews[i].setCard(card);

      for (PropertyRow row : mPropertyRows) {
        row.icons[i].setVisibility(hasCard ? VISIBLE : INVISIBLE);
        if (hasCard) {
          int value = 0;
          switch (row.type) {
            case NUMBER:
              value = card.mNumber;
              break;
            case SHAPE:
              value = card.mShape;
              break;
            case PATTERN:
              value = card.mPattern;
              break;
            case COLOR:
              value = card.mColor;
              break;
          }
          row.icons[i].setProperty(row.type, value);
        }
      }
    }

    updateConclusions(cards);
  }

  private void updateConclusions(List<Card> cards) {
    if (cards.size() < 3) {
      for (PropertyRow row : mPropertyRows) {
        row.conclusion.setText("");
        row.tickCross.setImageDrawable(null);
      }
      return;
    }

    for (PropertyRow row : mPropertyRows) {
      int v0 = 0, v1 = 0, v2 = 0;
      switch (row.type) {
        case NUMBER:
          v0 = cards.get(0).mNumber;
          v1 = cards.get(1).mNumber;
          v2 = cards.get(2).mNumber;
          break;
        case SHAPE:
          v0 = cards.get(0).mShape;
          v1 = cards.get(1).mShape;
          v2 = cards.get(2).mShape;
          break;
        case PATTERN:
          v0 = cards.get(0).mPattern;
          v1 = cards.get(1).mPattern;
          v2 = cards.get(2).mPattern;
          break;
        case COLOR:
          v0 = cards.get(0).mColor;
          v1 = cards.get(1).mColor;
          v2 = cards.get(2).mColor;
          break;
      }
      updateConclusion(row.conclusion, row.tickCross, v0, v1, v2);
    }
  }

  private void updateConclusion(TextView text, ImageView icon, int v0, int v1, int v2) {
    if (v0 == v1 && v1 == v2) {
      text.setText(R.string.same);
      icon.setImageResource(R.drawable.ic_tick);
    } else if (v0 != v1 && v1 != v2 && v0 != v2) {
      text.setText(R.string.diff);
      icon.setImageResource(R.drawable.ic_tick);
    } else {
      text.setText(R.string.two_and_one_short);
      icon.setImageResource(R.drawable.ic_cross);
    }
  }
}
