package com.antsapps.triples.cardsview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import com.antsapps.triples.SettingsFragment;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.OnValidTripleSelectedListener;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class CardsView extends ViewGroup implements Game.GameRenderer, CardDimensionsProvider {

  private static final String TAG = "CardsView";

  public interface OnSelectionChangedListener {
    void onSelectionChanged(Set<Card> selectedCards);
  }

  public interface OnIncorrectTripleSelectedListener {
    void onIncorrectTripleSelected();
  }

  private static final Rect EMPTY_RECT = new Rect(0, 0, 0, 0);
  protected ImmutableList<Card> mCards = ImmutableList.of();
  protected final Map<Card, CardView> mCardViews = Maps.newHashMap();
  private final Set<Card> mCurrentlyHinted = Sets.newHashSet();
  private OnValidTripleSelectedListener mOnValidTripleSelectedListener;
  private OnSelectionChangedListener mOnSelectionChangedListener;
  private OnIncorrectTripleSelectedListener mOnIncorrectTripleSelectedListener;
  protected Rect mOffScreenLocation = new Rect();

  /**
   * Cards in this set will animate in from the off-screen location rather than fading in when added
   * via the next updateCardsInPlay call. Used for backward step navigation.
   */
  protected final Set<Card> mCardsForReverseAnimation = new HashSet<>();

  /**
   * This is a value from 0 to 1, where 0 means the view is completely transparent and 1 means the
   * view is completely opaque.
   */
  private float mDimAlpha = 1;

  public static final int COLUMNS = 3;

  private int mWidthOfCard;

  private int mHeightOfCard;

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
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    mWidthOfCard = (right - left) / COLUMNS;
    mHeightOfCard = (int) (mWidthOfCard * CardView.HEIGHT_OVER_WIDTH);
    mOffScreenLocation.set(right, bottom, right + mWidthOfCard, bottom + mHeightOfCard);

    for (int i = 0; i < mCards.size(); i++) {
      Card card = mCards.get(i);
      CardView child = mCardViews.get(card);
      if (child != null) {
        int oldLeft = child.getLeft();
        int oldTop = child.getTop();
        boolean wasLaidOut = oldLeft != 0 || oldTop != 0 || child.getWidth() != 0;

        Rect bounds = calcBounds(i);
        child.layout(bounds.left, bounds.top, bounds.right, bounds.bottom);

        if (!wasLaidOut && mCardsForReverseAnimation.contains(card)) {
          // New card that should fly in from the off-screen location
          mCardsForReverseAnimation.remove(card);
          animateTranslation(
              child, mOffScreenLocation.left - bounds.left, mOffScreenLocation.top - bounds.top);
        } else if (wasLaidOut && (oldLeft != bounds.left || oldTop != bounds.top)) {
          // Position changed, animate from delta back to 0
          animateTranslation(child, oldLeft - bounds.left, oldTop - bounds.top);
        }

        if (child.getAlpha() == 0) {
          child
              .animate()
              .alpha(1)
              .setDuration(SettingsFragment.getAnimationDuration(getContext()))
              .setInterpolator(new AccelerateDecelerateInterpolator())
              .start();
        }
      }
    }
  }

  private void animateTranslation(final CardView child, int deltaX, int deltaY) {
    child.setTranslationX(deltaX);
    child.setTranslationY(deltaY);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      child.setTranslationZ(50f);
    }
    child
        .animate()
        .translationX(0)
        .translationY(0)
        .setDuration(SettingsFragment.getAnimationDuration(getContext()))
        .setInterpolator(new AccelerateDecelerateInterpolator())
        .setListener(
            new AnimatorListenerAdapter() {
              @Override
              public void onAnimationEnd(Animator animation) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                  child.setTranslationZ(0);
                }
              }
            })
        .start();
  }

  @Override
  protected void dispatchDraw(Canvas canvas) {
    super.dispatchDraw(canvas);
    if (mDimAlpha != 1) {
      canvas.drawColor(Color.argb((int) ((1 - mDimAlpha) * 255), 0xF3, 0xF3, 0xF3));
    }
  }

  public void updateCardsInPlay(ImmutableList<Card> newCards) {
    long start = com.antsapps.triples.backend.Application.getTimeProvider().currentTimeMillis();
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
    long end = com.antsapps.triples.backend.Application.getTimeProvider().currentTimeMillis();
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

  protected void logValidTriple() {
    Log.v(TAG, "valid positions: " + Game.getValidTriplePositions(mCards));
  }

  public void updateBounds() {
    requestLayout();
  }

  protected void updateMeasuredDimensions(final int widthMeasureSpec, final int heightMeasureSpec) {
    int widthOfCards = getDefaultSize(getMeasuredWidth(), widthMeasureSpec);
    if (widthOfCards == 0) {
      if (getWidth() > 0) {
        widthOfCards = getWidth();
      } else {
        widthOfCards = getResources().getDisplayMetrics().widthPixels;
      }
    }
    mWidthOfCard = widthOfCards / COLUMNS;
    mHeightOfCard = (int) (mWidthOfCard * CardView.HEIGHT_OVER_WIDTH);

    int rows = (int) Math.ceil((double) mCards.size() / COLUMNS);
    int heightOfCards = mHeightOfCard * rows;
    if (mCards.isEmpty()) {
      heightOfCards = 0;
    }

    if (widthOfCards > 0 && heightOfCards > 0) {
      mOffScreenLocation.set(
          widthOfCards, heightOfCards, widthOfCards + mWidthOfCard, heightOfCards + mHeightOfCard);
    }

    setMeasuredDimension(widthOfCards, heightOfCards);
  }

  public Rect calcBounds(int i) { // public for use in BoardHistoryActivity
    return new Rect(
        i % COLUMNS * mWidthOfCard,
        i / COLUMNS * mHeightOfCard,
        (i % COLUMNS + 1) * mWidthOfCard,
        (i / COLUMNS + 1) * mHeightOfCard);
  }

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
        if (mOnIncorrectTripleSelectedListener != null) {
          mOnIncorrectTripleSelectedListener.onIncorrectTripleSelected();
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

  public void setOnIncorrectTripleSelectedListener(OnIncorrectTripleSelectedListener listener) {
    mOnIncorrectTripleSelectedListener = listener;
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

  public void setSelected(Card card, boolean selected) {
    CardView cardView = mCardViews.get(card);
    if (cardView != null) {
      cardView.setSelected(selected);
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

  public void animateTripleFoundToOffscreen(Set<Card> triple, Runnable onFinished) {
    animateTripleFoundInternal(
        Maps.toMap(triple, card -> mOffScreenLocation), new AccelerateInterpolator(), onFinished);
  }

  /**
   * Marks the given cards to start their entry animation from the off-screen location (rather than
   * fading in) when they are added in the next {@link #updateCardsInPlay} call. Used to animate
   * found-triple cards flying back onto the board during backward step navigation.
   */
  public void markCardsForReverseAnimation(Set<Card> cards) {
    mCardsForReverseAnimation.addAll(cards);
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

  /**
   * Immediately removes the given cards from {@code mCardViews} (so they are no longer tracked) and
   * starts a fade-out animation on their views, removing them from the view hierarchy when the
   * animation ends. Returns without waiting — the caller can update board state immediately. Used
   * to fade out replaced cards during backward step navigation.
   */
  public void fadeOutAndRemoveCards(Set<Card> cards) {
    long dur = SettingsFragment.getAnimationDuration(getContext());
    for (Card card : cards) {
      CardView cv = mCardViews.remove(card);
      if (cv == null) continue;
      cv.animate()
          .alpha(0)
          .setDuration(dur)
          .setListener(
              new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                  removeView(cv);
                }
              })
          .start();
    }
  }

  /**
   * Animates the given cards to alpha 0, then calls {@code onFinished}. If none of the cards exist
   * in this view, {@code onFinished} is called immediately. Used to fade out cards before a
   * backward step navigation so they disappear gracefully before the board state changes.
   */
  public void fadeOutCardsAndThen(Set<Card> cards, Runnable onFinished) {
    if (cards.isEmpty()) {
      onFinished.run();
      return;
    }
    long dur = SettingsFragment.getAnimationDuration(getContext());
    AtomicInteger pending = new AtomicInteger(cards.size());
    for (Card card : cards) {
      CardView cv = mCardViews.get(card);
      if (cv == null) {
        if (pending.decrementAndGet() == 0) onFinished.run();
        continue;
      }
      cv.animate()
          .alpha(0)
          .setDuration(dur)
          .setListener(
              new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                  if (pending.decrementAndGet() == 0) onFinished.run();
                }
              })
          .start();
    }
  }

  @Override
  public int cardWidth() {
    return mWidthOfCard;
  }

  @Override
  public int cardHeight() {
    return mHeightOfCard;
  }
}
