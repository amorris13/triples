package com.antsapps.triples;

import java.util.List;
import java.util.Map;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.CycleInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;

import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.Game.GameState;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;

public class CardDrawable extends Drawable implements Comparable<CardDrawable> {

  private static final Rect EMPTY_RECT = new Rect(0, 0, 0, 0);

  private static final Map<GameState, Float> sAlphasForGameState = Maps
      .newEnumMap(GameState.class);
  static {
    sAlphasForGameState.put(GameState.ACTIVE, 1f);
    sAlphasForGameState.put(GameState.COMPLETED, 0.5f);
    sAlphasForGameState.put(GameState.PAUSED, 0f);
  }

  interface OnAnimationFinishedListener {
    void onAnimationFinished();
  }

  private int mDrawOrder;

  private final Card mCard;

  private final SymbolDrawable mSymbol;
  private final CardBackgroundDrawable mCardBackground;

  private Rect mBounds;

  private boolean mSelected;

  private Animation mAnimation;
  private final Transformation mTransformation = new Transformation();

  private final OnAnimationFinishedListener mListener;

  private float mAlpha;

  public CardDrawable(Card card, OnAnimationFinishedListener listener) {
    mCard = card;
    mListener = listener;

    mSymbol = new SymbolDrawable(mCard);
    mCardBackground = new CardBackgroundDrawable();
  }

  private static List<Rect> getBoundsForNumId(int id, Rect bounds) {
    List<Rect> rects = Lists.newArrayList();

    int halfSideLength = bounds.width() / 10;
    int gap = halfSideLength / 2;
    switch (id) {
      case 0:
        rects.add(squareFromCenterAndRadius(
            bounds.centerX(),
            bounds.centerY(),
            halfSideLength));
        break;
      case 1:
        rects.add(squareFromCenterAndRadius(bounds.centerX() - gap / 2
            - halfSideLength, bounds.centerY(), halfSideLength));
        rects.add(squareFromCenterAndRadius(bounds.centerX() + gap / 2
            + halfSideLength, bounds.centerY(), halfSideLength));
        break;
      case 2:
        rects.add(squareFromCenterAndRadius(bounds.centerX() - gap
            - halfSideLength * 2, bounds.centerY(), halfSideLength));
        rects.add(squareFromCenterAndRadius(
            bounds.centerX(),
            bounds.centerY(),
            halfSideLength));
        rects.add(squareFromCenterAndRadius(bounds.centerX() + gap
            + halfSideLength * 2, bounds.centerY(), halfSideLength));
        break;
    }
    return rects;
  }

  private static Rect squareFromCenterAndRadius(int centerX, int centerY,
      int radius) {
    return new Rect(centerX - radius, centerY - radius, centerX + radius,
        centerY + radius);
  }

  public boolean isAnimating() {
    return mAnimation != null && mAnimation.hasStarted()
        && !mAnimation.hasEnded();
  }

  @Override
  public void draw(Canvas canvas) {
    Rect bounds = mBounds;
    if (bounds == null || bounds == EMPTY_RECT) {
      return;
    }
    int sc = canvas.save();
    Animation anim = mAnimation;
    if (anim != null) {
      anim.getTransformation(
          AnimationUtils.currentAnimationTimeMillis(),
          mTransformation);

      canvas.concat(mTransformation.getMatrix());
      mCardBackground.setAlpha((int) (mTransformation.getAlpha() * 255));
      mSymbol.setAlpha((int) (mTransformation.getAlpha() * 255));

      if (!anim.isInitialized()) {
        anim.initialize(bounds.width(), bounds.height(), 0, 0);
      }
    }
    drawInternal(canvas, bounds);
    canvas.restoreToCount(sc);
  }

  private void drawInternal(Canvas canvas, Rect bounds) {
    mCardBackground.setBounds(bounds);
    mCardBackground.draw(canvas);
    for (Rect rect : getBoundsForNumId(mCard.mNumber, bounds)) {
      mSymbol.setBounds(rect);
      mSymbol.draw(canvas);
    }
  }

  @Override
  public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }

  @Override
  public void setAlpha(int alpha) {
    mAlpha = (float) alpha / 255;
    mSymbol.setAlpha(alpha);
    mCardBackground.setAlpha(alpha);
  }

  @Override
  public void setColorFilter(ColorFilter cf) {
    mSymbol.setColorFilter(cf);
    mCardBackground.setColorFilter(cf);
  }

  public Card getCard() {
    return mCard;
  }

  public void onTap() {
    if (mSelected) {
      setSelected(false);
    } else {
      setSelected(true);
    }
  }

  private void setSelected(boolean selected) {
    mSelected = selected;
    mCardBackground.setSelected(selected);
  }

  public void onIncorrectTriple() {
    // Shake animation
    Animation shakeAnimation = new RotateAnimation(0, 5, mBounds.centerX(),
        mBounds.centerY());
    shakeAnimation.setInterpolator(new CycleInterpolator(4));
    shakeAnimation.setDuration(800);
    shakeAnimation.setStartTime(Animation.START_ON_FIRST_FRAME);
    shakeAnimation.setAnimationListener(new AnimationListener() {

      @Override
      public void onAnimationEnd(Animation animation) {
        setSelected(false);
        if (mAnimation == animation) {
          mAnimation = null;
        }
      }

      @Override
      public void onAnimationRepeat(Animation animation) {
      }

      @Override
      public void onAnimationStart(Animation animation) {
      }

    });
    mAnimation = shakeAnimation;
  }

  @Override
  public void setBounds(Rect bounds) {
    Rect oldBounds = mBounds;
    mBounds = new Rect(bounds);
    Animation transitionAnimation = null;
    if (bounds.equals(oldBounds)) {
      // No change
      return;
    } else if (oldBounds == null) {
      // This CardDrawable is new.
      mBounds = bounds;
      transitionAnimation = new AlphaAnimation(0, 1);
    } else {
      // This CardDrawable is old
      transitionAnimation = new TranslateAnimation(oldBounds.centerX()
          - bounds.centerX(), 0, oldBounds.centerY() - bounds.centerY(), 0);
      mDrawOrder = 1;
    }
    transitionAnimation.setInterpolator(new AccelerateInterpolator());
    transitionAnimation.setDuration(800);
    transitionAnimation.setStartTime(Animation.START_ON_FIRST_FRAME);

    transitionAnimation.setAnimationListener(new AnimationListener() {
      @Override
      public void onAnimationEnd(Animation animation) {
        mListener.onAnimationFinished();
        mDrawOrder = 0;

        if (mAnimation == animation) {
          mAnimation = null;
        }
      }

      @Override
      public void onAnimationRepeat(Animation animation) {
      }

      @Override
      public void onAnimationStart(Animation animation) {
      }

    });
    mAnimation = transitionAnimation;
  }

  public int getDrawOrder() {
    return mDrawOrder;
  }

  @Override
  public int compareTo(CardDrawable another) {
    return Ints.compare(mDrawOrder, another.mDrawOrder);
  }

  public void updateGameState(GameState state, boolean animate) {
    if (animate) {
      float currentAlpha = (mAnimation == null) ? mAlpha : mTransformation.getAlpha();
      Animation stateChangeAnimation = new AlphaAnimation(currentAlpha,
          sAlphasForGameState.get(state));
      stateChangeAnimation.setStartTime(Animation.START_ON_FIRST_FRAME);
      stateChangeAnimation.setDuration(800);
      mAnimation = stateChangeAnimation;
    }
    setAlpha(Math.round(sAlphasForGameState.get(state) * 255));
  }
}
