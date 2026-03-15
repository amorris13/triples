package com.antsapps.triples.cardsview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.OnValidTripleSelectedListener;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;

public abstract class CardsView extends ViewGroup implements Game.GameRenderer {

  private static final String TAG = "CardsView";

  public interface OnSelectionChangedListener {
    void onSelectionChanged(Set<Card> selectedCards);
  }

  private static final Rect EMPTY_RECT = new Rect(0, 0, 0, 0);
  protected ImmutableList<Card> mCards = ImmutableList.of();
  protected final Map<Card, CardView> mCardViews = Maps.newHashMap();
  private final Set<Card> mCurrentlyHinted = Sets.newHashSet();
  private OnValidTripleSelectedListener mOnValidTripleSelectedListener;
  private OnSelectionChangedListener mOnSelectionChangedListener;
  protected Rect mOffScreenLocation = new Rect();

  /**
   * This is a value from 0 to 1, where 0 means the view is completely transparent and 1 means the
   * view is completely opaque.
   */
  private float mDimAlpha = 1;

  public CardsView(Context context) {
    this(context, null);
  }

  public CardsView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public CardsView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
    updateMeasuredDimensions(widthMeasureSpec, heightMeasureSpec);
    int cardWidth = cardWidth();
    int cardHeight = cardHeight();

    if (cardWidth > 0 && cardHeight > 0) {
      for (int i = 0; i < getChildCount(); i++) {
        View child = getChildAt(i);
        child.measure(
            MeasureSpec.makeMeasureSpec(cardWidth, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(cardHeight, MeasureSpec.EXACTLY));
      }
    }
  }

  @Override
  protected void dispatchDraw(Canvas canvas) {
    super.dispatchDraw(canvas);
    if (mDimAlpha != 1) {
      canvas.drawColor(Color.argb((int) ((1 - mDimAlpha) * 255), 0xF3, 0xF3, 0xF3));
    }
  }

  public void updateCardsInPlay(ImmutableList<Card> newCards) {
    long start = System.currentTimeMillis();
    for (Card oldCard : mCards) {
      if (!newCards.contains(oldCard)) {
        if (mCardViews.containsKey(oldCard)) {
          // Card was removed but not by finding it (e.g. game reset)
          CardView cardView = mCardViews.remove(oldCard);
          removeView(cardView);
        }
      }
    }

    mCards = newCards;
    updateMeasuredDimensions(0, 0);
    for (int i = 0; i < mCards.size(); i++) {
      Card card = mCards.get(i);
      CardView cardView = mCardViews.get(card);
      if (cardView == null) {
        cardView = createCardView(card);
        mCardViews.put(card, cardView);
        addView(cardView);
        if (cardView.shouldSlideIn()) {
          cardView.setTranslationX(0 - cardWidth());
          cardView.setTranslationY(0 - cardHeight());
          cardView.resetSlideIn();
        } else {
          cardView.setAlpha(0);
        }
      }
    }
    requestLayout();
    logValidTriple();
    long end = System.currentTimeMillis();
    Log.i(TAG, "updateCards took " + (end - start));
  }

  private CardView createCardView(Card card) {
    CardView cardView = new CardView(getContext(), card);
    cardView.setOnClickListener(
        v -> {
          if (!isEnabled()) return;
          v.setSelected(!v.isSelected());
          if (mOnSelectionChangedListener != null) {
            mOnSelectionChangedListener.onSelectionChanged(getSelectedCards());
          }
          checkSelectedCards();
        });
    return cardView;
  }

  protected abstract int cardWidth();

  protected abstract int cardHeight();

  protected abstract void logValidTriple();

  public void updateBounds() {
    requestLayout();
  }

  protected abstract void updateMeasuredDimensions(
      final int widthMeasureSpec, final int heightMeasureSpec);

  public abstract Rect calcBounds(int i);

  @Override
  public void setAlpha(float opacity) {
    super.setAlpha(opacity);
    mDimAlpha = opacity;
    invalidate();
  }

  private void checkSelectedCards() {
    Set<Card> selectedCards = getSelectedCards();
    if (selectedCards.size() == 3) {
      if (Game.isValidTriple(selectedCards)) {
        mOnValidTripleSelectedListener.onValidTripleSelected(selectedCards);
      } else {
        for (Card card : selectedCards) {
          CardView cardView = mCardViews.get(card);
          if (cardView != null) {
            cardView.onIncorrectTriple();
          }
        }
      }
      clearSelectedCards();
    }
  }

  public void shouldSlideIn() {
    for (CardView cardView : mCardViews.values()) {
      cardView.setShouldSlideIn();
    }
  }

  public void refreshDrawables() {
    // Invalidate and refresh all card views
    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildAt(i);
      if (child instanceof CardView) {
        ((CardView) child).refresh();
      }
    }
  }

  public void setOnValidTripleSelectedListener(OnValidTripleSelectedListener listener) {
    mOnValidTripleSelectedListener = listener;
  }

  public OnValidTripleSelectedListener getOnValidTripleSelectedListener() {
    return mOnValidTripleSelectedListener;
  }

  public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
    mOnSelectionChangedListener = listener;
  }

  @Override
  public Set<Card> getSelectedCards() {
    Set<Card> selectedCards = Sets.newHashSet();
    for (CardView cardView : mCardViews.values()) {
      if (cardView.isSelected()) {
        selectedCards.add(cardView.getCard());
      }
    }
    return selectedCards;
  }

  @Override
  public void addHint(Card card) {
    CardView cardView = mCardViews.get(card);
    if (cardView != null) {
      cardView.setHinted(true);
      mCurrentlyHinted.add(card);
    }

    // Remove incorrectly selected cards
    for (CardView cv : mCardViews.values()) {
      if (cv.isSelected() && !mCurrentlyHinted.contains(cv.getCard())) {
        cv.setSelected(false);
      }
    }
  }

  @Override
  public void clearHintedCards() {
    for (CardView cardView : mCardViews.values()) {
      if (cardView != null) {
        cardView.setHinted(false);
      }
    }
    mCurrentlyHinted.clear();
  }

  @Override
  public void clearSelectedCards() {
    for (CardView cardView : mCardViews.values()) {
      cardView.setSelected(false);
    }
  }

  public void onAlreadyFoundTriple(Set<Card> triple) {
    for (Card card : triple) {
      CardView cv = mCardViews.get(card);
      if (cv != null) {
        cv.onIncorrectTriple(true);
      }
    }
  }

  public void animateTripleFoundToOffscreen(Set<Card> triple) {
    animateTripleFoundInternal(
        Maps.toMap(triple, card -> mOffScreenLocation), new AccelerateInterpolator(), null);
  }

  public void animateTripleFound(
      final Map<Card, Rect> triple, Interpolator interpolator, final Runnable onAnimationFinished) {
    // Translate window coordinates to CardsView coordinates
    int[] cardsViewLoc = new int[2];
    getLocationInWindow(cardsViewLoc);

    animateTripleFoundInternal(
        Maps.transformValues(
            triple,
            windowRect -> {
              Rect targetBoundsInCardsView = new Rect(windowRect);
              targetBoundsInCardsView.offset(-cardsViewLoc[0], -cardsViewLoc[1]);
              return targetBoundsInCardsView;
            }),
        interpolator,
        onAnimationFinished);
  }

  private void animateTripleFoundInternal(
      final Map<Card, Rect> triple, Interpolator interpolator, final Runnable onAnimationFinished) {
    int i = 0;
    for (Map.Entry<Card, Rect> entry : triple.entrySet()) {
      Card c = entry.getKey();
      Rect targetBoundsInCardsView = entry.getValue();
      CardView cv = mCardViews.remove(c);
      if (cv != null) {
        final boolean isLast = (i == triple.size() - 1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          cv.setTranslationZ(100f);
        } else {
          cv.bringToFront();
        }
        cv.animateFoundCard(
            targetBoundsInCardsView,
            interpolator,
            () -> {
              removeView(cv);
              if (isLast && onAnimationFinished != null) {
                onAnimationFinished.run();
              }
            });
      }
      i++;
    }

    clearSelectedCards();
  }
}
