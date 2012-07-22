package com.antsapps.triples;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.Game.GameState;
import com.antsapps.triples.backend.Game.OnUpdateGameStateListener;
import com.google.common.collect.Lists;

public abstract class GameCardsView extends CardsView implements OnUpdateGameStateListener {

  private static final String TAG = "GameCardsView";

  private final List<Card> mCurrentlySelected = Lists.newArrayList();
  private Game mGame;
  private GameState mGameState;

  public GameCardsView(Context context) {
    this(context, null);
  }

  public GameCardsView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public GameCardsView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public void setGame(Game game) {
    mGame = game;
    onUpdateGameState(game.getGameState());
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
  public void onUpdateGameState(GameState state) {
    mGameState = state;
    if(mGameState == GameState.STARTING) {
      for(CardDrawable cardDrawable : mCardDrawables.values()) {
        cardDrawable.setShouldSlideIn();
      }
    }
  }
}