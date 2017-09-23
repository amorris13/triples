package com.antsapps.triples.cardsview;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.antsapps.triples.cardsview.CardDrawable.OnAnimationFinishedListener;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.Game.OnUpdateCardsInPlayListener;
import com.antsapps.triples.backend.OnValidTripleSelectedListener;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

public abstract class CardsView extends View implements
    OnUpdateCardsInPlayListener {

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
  private final Map<Card, CardDrawable> mCardDrawables = Maps
      .newConcurrentMap();
  private final List<Card> mCurrentlySelected = Lists.newArrayList();
  private OnValidTripleSelectedListener mOnValidTripleSelectedListener;
  protected Rect mOffScreenLocation = new Rect();
  private final Handler mHandler;
  private volatile int mNumAnimating;

  /**
   * This is a value from 0 to 1, where 0 means the view is completely
   * transparent and 1 means the view is completely opaque.
   */
  private float mAlpha = 1;

  public CardsView(Context context) {
    this(context, null);
  }

  public CardsView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public CardsView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    mHandler = new Handler() {
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
  protected void onMeasure(final int widthMeasureSpec,
      final int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    updateMeasuredDimensions(widthMeasureSpec, heightMeasureSpec);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    long start = System.currentTimeMillis();
    for (CardDrawable dr : Ordering.natural().sortedCopy(
        mCardDrawables.values())) {
      dr.draw(canvas);
    }
    if (mAlpha != 1) {
      canvas.drawColor(Color.argb((int) ((1 - mAlpha) * 255), 0xF3, 0xF3, 0xF3));
    }
    boolean invalidated = false;
    if (mNumAnimating > 0) {
      invalidate();
      invalidated = true;
    }
    long end = System.currentTimeMillis();
    Log.v(TAG, "draw took " + (end - start) + ", cards drawn: "
        + mCardDrawables.size() + ", invalidated = " + invalidated);
  }

  private void updateCards(ImmutableList<Card> newCards,
      ImmutableList<Card> oldCards, int numRemaining) {
    long start = System.currentTimeMillis();
    for (Card oldCard : mCards) {
      if (!newCards.contains(oldCard)) {
        mCardDrawables.get(oldCard).updateBounds(mOffScreenLocation, mHandler);
      }
    }

    mCards = newCards;
    for (int i = 0; i < mCards.size(); i++) {
      Card card = mCards.get(i);
      CardDrawable cardDrawable = mCardDrawables.get(card);
      if (cardDrawable == null) {
        cardDrawable = new CardDrawable(getContext(), card,
            new CardRemovalListener(card));
        mCardDrawables.put(card, cardDrawable);
      }
      if (!calcBounds(i).equals(EMPTY_RECT)) {
        cardDrawable.updateBounds(calcBounds(i), mHandler);
      }
    }
    updateMeasuredDimensions(0, 0);
    invalidate();
    logValidTriple();
    long end = System.currentTimeMillis();
    Log.i(TAG, "updateCards took " + (end - start));
  }

  protected abstract void logValidTriple();

  void updateBounds() {
    long start = System.currentTimeMillis();
    for (int i = 0; i < mCards.size(); i++) {
      Card card = mCards.get(i);
      CardDrawable cardDrawable = mCardDrawables.get(card);
      cardDrawable.updateBounds(calcBounds(i), mHandler);
    }
    invalidate();
    long end = System.currentTimeMillis();
    Log.i(TAG, "updateBounds took " + (end - start));
  }

  protected abstract void updateMeasuredDimensions(final int widthMeasureSpec,
      final int heightMeasureSpec);

  protected abstract Rect calcBounds(int i);

  @Override
  public void onUpdateCardsInPlay(ImmutableList<Card> newCards,
      ImmutableList<Card> oldCards, int numRemaining, int numTriplesFound) {
    updateCards(newCards, oldCards, numRemaining);
  }

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
    mAlpha = opacity;
    invalidate();
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (!isEnabled()) {
      return false;
    }
    if (event.getAction() == MotionEvent.ACTION_DOWN) {
      Card tappedCard = getCardForPosition(
          (int) event.getX(),
          (int) event.getY());
      if (tappedCard == null) {
        return true;
      }
      CardDrawable tappedCardDrawable = mCardDrawables.get(tappedCard);
      if (tappedCardDrawable.onTap()) {
        mCurrentlySelected.add(tappedCard);
      } else {
        mCurrentlySelected.remove(tappedCard);
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
          mCardDrawables.get(card).onIncorrectTriple(mHandler);
        }
      }
      mCurrentlySelected.clear();
    }
  }

  public void shouldSlideIn() {
    for(CardDrawable cardDrawable : mCardDrawables.values()) {
      cardDrawable.setShouldSlideIn();
    }
  }

  public void setOnValidTripleSelectedListener(OnValidTripleSelectedListener listener) {
    mOnValidTripleSelectedListener = listener;
  }
}