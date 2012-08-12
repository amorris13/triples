package com.antsapps.triples;

import java.util.Map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.antsapps.triples.CardDrawable.OnAnimationFinishedListener;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.Game.OnUpdateCardsInPlayListener;
import com.google.common.collect.ImmutableList;
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
  protected static final int WHAT_INCREMENT = 0;
  protected static final int WHAT_DECREMENT = 1;
  protected ImmutableList<Card> mCards = ImmutableList.of();
  protected final Map<Card, CardDrawable> mCardDrawables = Maps
      .newConcurrentMap();
  protected Rect mOffScreenLocation = new Rect();
  protected final Handler mHandler;
  protected int mNumAnimating;

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
    canvas.saveLayerAlpha(
        0,
        0,
        canvas.getWidth(),
        canvas.getHeight(),
        Math.round(mAlpha * 255),
        Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
    for (CardDrawable dr : Ordering.natural().sortedCopy(
        mCardDrawables.values())) {
      dr.draw(canvas);
    }
    if (mNumAnimating > 0) {
      invalidate();
    }
    long end = System.currentTimeMillis();
    Log.v(TAG, "draw took " + (end - start) + ", cards drawn: "
        + mCardDrawables.size());
  }

  protected void updateCards(ImmutableList<Card> newCards,
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
    long end = System.currentTimeMillis();
    Log.i(TAG, "updateCards took " + (end - start));
  }

  protected void updateBounds() {
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
      ImmutableList<Card> oldCards, int numRemaining) {
    updateCards(newCards, oldCards, numRemaining);
  }

  synchronized void incrementNumAnimating() {
    mNumAnimating++;
    Log.i(TAG, "increment with mNumAnimating = " + mNumAnimating);
    if (mNumAnimating > 0) {
      invalidate();
    }
  }

  synchronized void decrementNumAnimating() {
    mNumAnimating--;
  }

  @Override
  public void setAlpha(float opacity) {
    mAlpha = opacity;
    invalidate();
  }
}