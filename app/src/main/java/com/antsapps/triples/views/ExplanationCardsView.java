package com.antsapps.triples.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.cardsview.CardView;
import com.antsapps.triples.cardsview.CardsView;
import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExplanationCardsView extends View {

  private List<Card> mCards = ImmutableList.of();
  private CardsView mCardsView;
  private final Map<Card, CardView> mCardViewCache = new HashMap<>();

  public ExplanationCardsView(Context context) {
    this(context, null);
  }

  public ExplanationCardsView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public void setCards(List<Card> cards) {
    mCards = ImmutableList.copyOf(cards);
    invalidate();
  }

  public void setCardsView(CardsView cardsView) {
    mCardsView = cardsView;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = MeasureSpec.getSize(widthMeasureSpec);
    if (width <= 0) {
      setMeasuredDimension(0, 0);
      return;
    }

    int cardWidth = width / 3;
    int cardHeight = (int) (cardWidth * CardView.HEIGHT_OVER_WIDTH);
    setMeasuredDimension(width, cardHeight);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (mCards.isEmpty() || mCardsView == null) {
      return;
    }

    Rect naturalBounds = mCardsView.calcBounds(0);
    int naturalWidth = naturalBounds.width();
    int naturalHeight = naturalBounds.height();

    float cardWidth = getWidth() / 3f;
    float scale = cardWidth / naturalWidth;

    Rect drawBounds = new Rect(0, 0, naturalWidth, naturalHeight);

    for (int i = 0; i < mCards.size(); i++) {
      Card card = mCards.get(i);
      CardView cardView = mCardViewCache.get(card);
      if (cardView == null) {
        cardView = new CardView(getContext(), card);
        mCardViewCache.put(card, cardView);
      }

      canvas.save();
      canvas.translate(i * cardWidth, 0);
      canvas.scale(scale, scale);
      cardView.drawCardContent(canvas, drawBounds);
      canvas.restore();
    }
  }
}
