package com.antsapps.triples;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.antsapps.triples.CardDrawable.OnAnimationFinishedListener;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.Game.OnUpdateGameStateListener;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

public abstract class CardsView extends View implements
    OnUpdateGameStateListener {

  private class CardRemovalListener implements OnAnimationFinishedListener {
    Card mCard;

    public CardRemovalListener(Card card) {
      mCard = card;
    }

    @Override
    public void onAnimationFinished(boolean stillVisible) {
      if (!stillVisible && !mCards.contains(mCard)) {
        mCardDrawables.remove(mCard);
      }
    }

  }

  protected ImmutableList<Card> mCards = ImmutableList.of();
  protected final Map<Card, CardDrawable> mCardDrawables = Maps.newHashMap();
  protected final List<Card> mCurrentlySelected = Lists.newArrayList();
  protected Game mGame;
  protected boolean mActive;

  public CardsView(Context context) {
    super(context);
  }

  public CardsView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public CardsView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public void setGame(Game game) {
    mGame = game;
    game.addOnUpdateGameStateListener(this);
    mActive = true;
  }

  @Override
  protected void onMeasure(final int widthMeasureSpec,
      final int heightMeasureSpec) {
    Log.i("CV", "oM: wMS = " + widthMeasureSpec + ", hMS = "
        + heightMeasureSpec);
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    updateMeasuredDimensions(widthMeasureSpec, heightMeasureSpec);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    long start = System.currentTimeMillis();
    canvas.drawColor(0xFFE0E0E0);
    if (mActive) {
      for (CardDrawable dr : Ordering.natural().sortedCopy(
          mCardDrawables.values())) {
        dr.draw(canvas);
      }
      invalidate();
    }
    long end = System.currentTimeMillis();
    Log.v("CardsView", "draw took " + (end - start) + ", mActive = " + mActive
        + ", cards drawn: " + mCardDrawables.size());
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (mActive) {
      Log.i("CardsView", "onTouchEvent");
      if (event.getAction() == MotionEvent.ACTION_DOWN) {
        Card tappedCard = getCardForPosition(
            (int) event.getX(),
            (int) event.getY());
        if (tappedCard == null) {
          return true;
        }
        CardDrawable tappedCardDrawable = mCardDrawables.get(tappedCard);
        tappedCardDrawable.onTap();
        if (mCurrentlySelected.contains(tappedCard)) {
          mCurrentlySelected.remove(tappedCard);
        } else {
          mCurrentlySelected.add(tappedCard);
        }

        checkSelectedCards();
      }
    }

    return true;
  }

  protected abstract Card getCardForPosition(int x, int y);

  protected void updateCards(ImmutableList<Card> newCards, ImmutableList<Card> oldCards, int numRemaining) {
    for (Card oldCard : mCards) {
      if (!newCards.contains(oldCard)) {
        mCardDrawables.get(oldCard).setNewPosition(
            null,
            new CardRemovalListener(oldCard));
      }
    }

    mCards = newCards;
    for (int i = 0; i < mCards.size(); i++) {
      Card card = mCards.get(i);
      CardDrawable cardDrawable = mCardDrawables.get(card);
      if (cardDrawable == null) {
        cardDrawable = new CardDrawable(card);
        mCardDrawables.put(card, cardDrawable);
      }
      cardDrawable.setNewPosition(calcBounds(i), new CardRemovalListener(card));
    }
    Log.i("CV", "updateCards()");
    updateMeasuredDimensions(0, 0);
  }

  protected void updateBounds() {
    for (int i = 0; i < mCards.size(); i++) {
      Card card = mCards.get(i);
      CardDrawable cardDrawable = mCardDrawables.get(card);
      cardDrawable.setBounds(calcBounds(i));
    }
  }

  protected abstract void updateMeasuredDimensions(final int widthMeasureSpec,
      final int heightMeasureSpec);

  protected abstract Rect calcBounds(int i);

  void checkSelectedCards() {
    if (mCurrentlySelected.size() == 3) {
      if (Game.isValidTriple(mCurrentlySelected)) {
        mGame.commitTriple(mCurrentlySelected);
      } else {
        for (Card card : mCurrentlySelected) {
          mCardDrawables.get(card).onIncorrectTriple();
        }
      }
      mCurrentlySelected.clear();
      invalidate();
    }
  }

  public void pause() {
    mActive = false;
    invalidate();
  }

  public void resume() {
    mActive = true;
    invalidate();
  }

  @Override
  public void onUpdateCardsInPlay(ImmutableList<Card> newCards,
      ImmutableList<Card> oldCards, int numRemaining) {
    updateCards(newCards, oldCards, numRemaining);
  }

  @Override
  public void onFinish() {

  }

}