package com.antsapps.triples.cardsview;

import static com.antsapps.triples.cardsview.CardDrawable.DEFAULT_ANIMATION_DURATION_MS;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import com.antsapps.triples.R;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.OnValidTripleSelectedListener;
import com.antsapps.triples.cardsview.CardDrawable.OnAnimationFinishedListener;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class CardsView extends View implements Game.GameRenderer {

  private static final String TAG = "CardsView";

  class CardRemovalListener implements OnAnimationFinishedListener {
    Card mCard;

    public CardRemovalListener(Card card) {
      mCard = card;
    }

    @Override
    public void onAnimationFinished() {
      if (!mCards.contains(mCard)) {
        mCardDrawables.remove(mCard);
      }
    }
  }

  private static final Rect EMPTY_RECT = new Rect(0, 0, 0, 0);
  static final int WHAT_INCREMENT = 0;
  static final int WHAT_DECREMENT = 1;
  protected ImmutableList<Card> mCards = ImmutableList.of();
  private final Map<Card, CardDrawable> mCardDrawables = Maps.newConcurrentMap();
  private final List<CardDrawable> mAnimatingCopies = Lists.newArrayList();
  private final Set<Card> mCurrentlySelected = Sets.newHashSet();
  private final Set<Card> mCurrentlyHinted = Sets.newHashSet();
  private OnValidTripleSelectedListener mOnValidTripleSelectedListener;
  protected Rect mOffScreenLocation = new Rect();
  private final Handler mHandler;
  private volatile int mNumAnimating;

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
    mHandler =
        new Handler() {
          @Override
          public void handleMessage(Message m) {
            switch (m.what) {
              case WHAT_INCREMENT:
                incrementNumAnimating();
                break;
              case WHAT_DECREMENT:
                decrementNumAnimating();
                break;
            }
          }
        };
  }

  @Override
  protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    updateMeasuredDimensions(widthMeasureSpec, heightMeasureSpec);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    long start = System.currentTimeMillis();
    for (CardDrawable dr : Ordering.natural().sortedCopy(mCardDrawables.values())) {
      dr.draw(canvas);
    }
    // Defensive copy here as entries may be deleted from the list.
    for (CardDrawable dr : Lists.newArrayList(mAnimatingCopies)) {
      dr.draw(canvas);
    }
    if (mDimAlpha != 1) {
      canvas.drawColor(Color.argb((int) ((1 - mDimAlpha) * 255), 0xF3, 0xF3, 0xF3));
    }
    boolean invalidated = false;
    if (mNumAnimating > 0) {
      invalidate();
      invalidated = true;
    }
    long end = System.currentTimeMillis();
    Log.v(
        TAG,
        "draw took "
            + (end - start)
            + ", cards drawn: "
            + mCardDrawables.size()
            + ", invalidated = "
            + invalidated);
  }

  public void updateCardsInPlay(ImmutableList<Card> newCards) {
    long start = System.currentTimeMillis();
    for (Card oldCard : mCards) {
      if (!newCards.contains(oldCard)) {
        CardDrawable cardDrawable = mCardDrawables.get(oldCard);
        if (cardDrawable != null) {
          cardDrawable.updateBounds(mOffScreenLocation, true);
        }
        mCurrentlySelected.remove(oldCard);
      }
    }

    mCards = newCards;
    updateMeasuredDimensions(0, 0);
    for (int i = 0; i < mCards.size(); i++) {
      Card card = mCards.get(i);
      CardDrawable cardDrawable = mCardDrawables.get(card);
      if (cardDrawable == null) {
        cardDrawable =
            new CardDrawable(getContext(), mHandler, card, new CardRemovalListener(card));
        mCardDrawables.put(card, cardDrawable);
      }
      if (!calcBounds(i).equals(EMPTY_RECT)) {
        cardDrawable.updateBounds(calcBounds(i), true);
      }
    }
    requestLayout();
    invalidate();
    logValidTriple();
    long end = System.currentTimeMillis();
    Log.i(TAG, "updateCards took " + (end - start));
  }

  protected abstract void logValidTriple();

  public void updateBounds() {
    if (mOffScreenLocation.isEmpty() && getWidth() > 0 && getHeight() > 0) {
      updateMeasuredDimensions(0, 0); // Trigger dimension calculation if needed
    }
    long start = System.currentTimeMillis();
    for (int i = 0; i < mCards.size(); i++) {
      Card card = mCards.get(i);
      CardDrawable cardDrawable = mCardDrawables.get(card);
      cardDrawable.updateBounds(calcBounds(i), true);
    }
    invalidate();
    long end = System.currentTimeMillis();
    Log.i(TAG, "updateBounds took " + (end - start));
  }

  protected abstract void updateMeasuredDimensions(
      final int widthMeasureSpec, final int heightMeasureSpec);

  public abstract Rect calcBounds(int i);

  private void incrementNumAnimating() {
    mNumAnimating++;
    Log.i(TAG, "increment with mNumAnimating = " + mNumAnimating);
    if (mNumAnimating > 0) {
      invalidate();
    }
  }

  private void decrementNumAnimating() {
    Log.i(TAG, "decrement with mNumAnimating = " + mNumAnimating);
    mNumAnimating--;
  }

  @Override
  public void setAlpha(float opacity) {
    super.setAlpha(opacity);
    mDimAlpha = opacity;
    invalidate();
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (!isEnabled()) {
      return false;
    }
    int action = event.getActionMasked();
    if (action == MotionEvent.ACTION_POINTER_DOWN || action == MotionEvent.ACTION_DOWN) {
      // Get the index of the pointer associated with the action.
      int index = event.getActionIndex();
      int xPos = (int) event.getX(index);
      int yPos = (int) event.getY(index);

      Card tappedCard = getCardForPosition(xPos, yPos);
      if (tappedCard == null) {
        return true;
      }
      CardDrawable tappedCardDrawable = mCardDrawables.get(tappedCard);
      if (mCurrentlySelected.contains(tappedCard)) {
        mCurrentlySelected.remove(tappedCard);
        tappedCardDrawable.setSelected(false);
      } else {
        mCurrentlySelected.add(tappedCard);
        tappedCardDrawable.setSelected(true);
      }

      checkSelectedCards();
      invalidate();
    }
    return true;
  }

  protected abstract Card getCardForPosition(int x, int y);

  private void checkSelectedCards() {
    if (mCurrentlySelected.size() == 3) {
      if (Game.isValidTriple(mCurrentlySelected)) {
        mOnValidTripleSelectedListener.onValidTripleSelected(mCurrentlySelected);
      } else {
        for (Card card : mCurrentlySelected) {
          mCardDrawables.get(card).onIncorrectTriple();
        }
      }
      mCurrentlySelected.clear();
    }
  }

  public void shouldSlideIn() {
    for (CardDrawable cardDrawable : mCardDrawables.values()) {
      cardDrawable.setShouldSlideIn();
    }
  }

  public void refreshDrawables() {
    for (CardDrawable cardDrawable : mCardDrawables.values()) {
      cardDrawable.regenerateCachedDrawable();
    }
    invalidate();
  }

  public void setOnValidTripleSelectedListener(OnValidTripleSelectedListener listener) {
    mOnValidTripleSelectedListener = listener;
  }

  public OnValidTripleSelectedListener getOnValidTripleSelectedListener() {
    return mOnValidTripleSelectedListener;
  }

  @Override
  public Set<Card> getSelectedCards() {
    return mCurrentlySelected;
  }

  @Override
  public void addHint(Card card) {
    CardDrawable cardDrawable = mCardDrawables.get(card);
    if (cardDrawable != null) {
      cardDrawable.setHinted(true);
      mCurrentlyHinted.add(card);
    }

    // Remove incorrectly selected cards
    Iterator<Card> iter = mCurrentlySelected.iterator();
    while (iter.hasNext()) {
      Card selectedCard = iter.next();
      if (!mCurrentlyHinted.contains(selectedCard)) {
        mCardDrawables.get(selectedCard).setSelected(false);
        iter.remove();
      }
    }

    invalidate();
  }

  @Override
  public void clearHintedCards() {
    for (CardDrawable cardDrawable : mCardDrawables.values()) {
      if (cardDrawable != null) {
        cardDrawable.setHinted(false);
      }
    }
    mCurrentlyHinted.clear();
  }

  @Override
  public void clearSelectedCards() {
    for (Card card : mCurrentlySelected) {
      CardDrawable cardDrawable = mCardDrawables.get(card);
      if (cardDrawable != null) {
        cardDrawable.setSelected(false);
      }
    }
    mCurrentlySelected.clear();
    invalidate();
  }

  public void onAlreadyFoundTriple(Set<Card> triple) {
    for (Card card : triple) {
      CardDrawable cd = mCardDrawables.get(card);
      if (cd != null) {
        cd.onIncorrectTriple(true);
      }
    }
  }

  public void animateTripleFound(final Map<Card, Rect> triple, final Runnable onAnimationFinished) {
    // Translate window coordinates to CardsView coordinates
    int[] cardsViewLoc = new int[2];
    getLocationInWindow(cardsViewLoc);

    int i = 0;
    for (Map.Entry<Card, Rect> entry : triple.entrySet()) {
      Card c = entry.getKey();
      Rect targetBoundsInWindow = entry.getValue();
      Rect targetBoundsInCardsView = new Rect(targetBoundsInWindow);
      targetBoundsInCardsView.offset(-cardsViewLoc[0], -cardsViewLoc[1]);

      CardDrawable cd = mCardDrawables.get(c);
      if (cd != null) {
        final boolean isLast = (i == triple.size() - 1);
        final CardDrawable copy = new CardDrawable(getContext(), mHandler, c, null);
        copy.setAnimationFinishedListener(
            () -> {
              mAnimatingCopies.remove(copy);
              if (isLast && onAnimationFinished != null) {
                onAnimationFinished.run();
              }
            });
        copy.updateBounds(cd.getBounds(), false);
        copy.setSelected(true);
        mAnimatingCopies.add(copy);
        copy.updateBounds(targetBoundsInCardsView, true);

        // Board card disappears immediately and then fades back in
        cd.setSelected(false);
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(
            PreferenceManager.getDefaultSharedPreferences(getContext())
                .getInt(
                    getContext().getString(R.string.pref_animation_speed),
                    DEFAULT_ANIMATION_DURATION_MS));
        fadeIn.setFillBefore(true);
        cd.updateAnimation(fadeIn);
      }
      i++;
    }

    mCurrentlySelected.clear();
  }
}
