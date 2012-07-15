package com.antsapps.triples;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.antsapps.triples.CardDrawable.OnAnimationFinishedListener;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.Game.GameState;
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
    public void onAnimationFinished() {
      if (!mCards.contains(mCard)) {
        mCardDrawables.remove(mCard);
      }
    }
  }

  static final int WHAT_INCREMENT = 0;
  static final int WHAT_DECREMENT = 1;

  private static final String TAG = "CardsView";

  private static final Rect EMPTY_RECT = new Rect(0, 0, 0, 0);

  protected ImmutableList<Card> mCards = ImmutableList.of();
  private final Map<Card, CardDrawable> mCardDrawables = Maps.newHashMap();
  private final List<Card> mCurrentlySelected = Lists.newArrayList();
  private Game mGame;
  private GameState mGameState;
  private final Vibrator mVibrator;

  protected Rect mOffScreenLocation = new Rect();

  private final Handler mHandler;
  private int mNumAnimating;

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
    mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
  }

  public void setGame(Game game) {
    mGame = game;
    onUpdateGameState(game.getGameState());
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
    canvas.drawColor(0xFFE0E0E0);
    for (CardDrawable dr : Ordering.natural().sortedCopy(
        mCardDrawables.values())) {
      dr.draw(canvas);
    }
    if (mNumAnimating > 0) {
      invalidate();
    }
    long end = System.currentTimeMillis();
    Log.v("CardsView", "draw took " + (end - start) + ", cards drawn: "
        + mCardDrawables.size());
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (mGameState == GameState.ACTIVE) {
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
    }

    return true;
  }

  protected abstract Card getCardForPosition(int x, int y);

  protected void updateCards(ImmutableList<Card> newCards,
      ImmutableList<Card> oldCards, int numRemaining) {
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
        cardDrawable = new CardDrawable(card, new CardRemovalListener(card));
        mCardDrawables.put(card, cardDrawable);
      }
      if (!calcBounds(i).equals(EMPTY_RECT)) {
        cardDrawable.updateBounds(calcBounds(i), mHandler);
      }
    }
    updateMeasuredDimensions(0, 0);
    invalidate();
  }

  protected void updateBounds() {
    for (int i = 0; i < mCards.size(); i++) {
      Card card = mCards.get(i);
      CardDrawable cardDrawable = mCardDrawables.get(card);
      cardDrawable.updateBounds(calcBounds(i), mHandler);
    }
    invalidate();
  }

  protected abstract void updateMeasuredDimensions(final int widthMeasureSpec,
      final int heightMeasureSpec);

  protected abstract Rect calcBounds(int i);

  private void checkSelectedCards() {
    if (mCurrentlySelected.size() == 3) {
      if (Game.isValidTriple(mCurrentlySelected)) {
        mGame.commitTriple(mCurrentlySelected);
      } else {
        for (Card card : mCurrentlySelected) {
          mCardDrawables.get(card).onIncorrectTriple(mHandler);
        }
      }
      mCurrentlySelected.clear();
    }
  }

  @Override
  public void onUpdateCardsInPlay(ImmutableList<Card> newCards,
      ImmutableList<Card> oldCards, int numRemaining) {
    updateCards(newCards, oldCards, numRemaining);
  }

  @Override
  public void onUpdateGameState(GameState state) {
    mGameState = state;
    dispatchGameStateUpdate();
  }

  private void dispatchGameStateUpdate() {
    for (CardDrawable drawable : mCardDrawables.values()) {
      drawable.updateGameState(mGameState, mHandler);
    }
    invalidate();
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
}