package com.antsapps.triples;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;

public class CardDrawable extends Drawable implements Comparable<CardDrawable> {

  private class BaseAnimationListener implements AnimationListener {

    private final Handler mHandler;

    BaseAnimationListener(Handler handler) {
      mHandler = handler;
    }

    @Override
    public void onAnimationStart(Animation animation) {
      mHandler.sendMessage(Message.obtain(mHandler, CardsView.WHAT_INCREMENT));
    }

    @Override
    public void onAnimationEnd(Animation animation) {
      mHandler.sendMessage(Message.obtain(mHandler, CardsView.WHAT_DECREMENT));
      if (mAnimation == animation) {
        mAnimation = null;
      }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
    }
  }

  enum CardState {
    SELECTED,
    NORMAL;
  }

  private final Map<CardState, BitmapDrawable> mDrawableForCardState = Maps
      .newEnumMap(CardState.class);

  interface OnAnimationFinishedListener {
    void onAnimationFinished();
  }

  private static final String TAG = "CardDrawable";

  private int mDrawOrder;

  private final Card mCard;

  private final SymbolDrawable mSymbol;
  private final CardBackgroundDrawable mCardBackground;

  private Rect mBounds;

  private CardState mState;

  private Animation mAnimation;
  private final Transformation mTransformation = new Transformation();

  private final OnAnimationFinishedListener mListener;

  private float mAlpha;

  private boolean mShouldSlideIn;

  private final Context mContext;

  public CardDrawable(Context context,
      Card card,
      OnAnimationFinishedListener listener) {
    mContext = context;
    mState = CardState.NORMAL;

    mCard = card;
    mListener = listener;
    mAlpha = 1;

    mSymbol = new SymbolDrawable(mCard);
    mCardBackground = new CardBackgroundDrawable();
  }

  private static List<Rect> getBoundsForNumId(int id, Rect bounds) {
    List<Rect> rects = Lists.newArrayList();

    int width = bounds.width();
    int height = bounds.height();
    int halfSideLength = width / 10;
    int gap = halfSideLength / 2;
    switch (id) {
      case 0:
        rects.add(squareFromCenterAndRadius(
            width / 2,
            height / 2,
            halfSideLength));
        break;
      case 1:
        rects.add(squareFromCenterAndRadius(width / 2 - gap / 2
            - halfSideLength, height / 2, halfSideLength));
        rects.add(squareFromCenterAndRadius(width / 2 + gap / 2
            + halfSideLength, height / 2, halfSideLength));
        break;
      case 2:
        rects.add(squareFromCenterAndRadius(width / 2 - gap - halfSideLength
            * 2, height / 2, halfSideLength));
        rects.add(squareFromCenterAndRadius(
            width / 2,
            height / 2,
            halfSideLength));
        rects.add(squareFromCenterAndRadius(width / 2 + gap + halfSideLength
            * 2, height / 2, halfSideLength));
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
    if (bounds == null) {
      return;
    }
    int sc = canvas.save();
    Animation anim = mAnimation;
    if (anim != null) {
      anim.getTransformation(
          AnimationUtils.currentAnimationTimeMillis(),
          mTransformation);

      canvas.concat(mTransformation.getMatrix());
      setAlpha((int) (mTransformation.getAlpha() * 255));

      if (!anim.isInitialized()) {
        anim.initialize(bounds.width(), bounds.height(), 0, 0);
      }
    }
    drawInternal(canvas, bounds);
    canvas.restoreToCount(sc);
  }

  private void drawInternal(Canvas canvas, Rect bounds) {
    CardState state = mState;
    BitmapDrawable drawable = mDrawableForCardState.get(state);
    if (drawable == null) {
      return;
    }
    drawable.setBounds(bounds);
    drawable.draw(canvas);
  }

  private void regenerateBitmapDrawables() {
    Rect normBounds = new Rect(mBounds);
    normBounds.offsetTo(0, 0);
    for (CardState state : CardState.values()) {
      Bitmap bitmap = Bitmap.createBitmap(
          normBounds.width(),
          normBounds.height(),
          Config.ARGB_8888);
      Canvas tmpCanvas = new Canvas(bitmap);
      mCardBackground.setBounds(normBounds);
      mCardBackground.setCardState(state);
      mCardBackground.draw(tmpCanvas);
      for (Rect rect : getBoundsForNumId(
          mCard.mNumber,
          normBounds)) {
        mSymbol.setBounds(rect);
        mSymbol.draw(tmpCanvas);
      }
      BitmapDrawable drawable = new BitmapDrawable(mContext.getResources(), bitmap);
      mDrawableForCardState.put(state, drawable);
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

  /** Returns true if the card is now selected, false otherwise. */
  public boolean onTap() {
    if (mState == CardState.SELECTED) {
      mState = CardState.NORMAL;
    } else {
      mState = CardState.SELECTED;
    }
    return mState == CardState.SELECTED;
  }

  public void onIncorrectTriple(final Handler handler) {
    // Shake animation
    Animation shakeAnimation = new RotateAnimation(0, 5, mBounds.centerX(),
        mBounds.centerY());
    shakeAnimation.setInterpolator(new CycleInterpolator(4));
    shakeAnimation.setDuration(800);
    shakeAnimation.setStartTime(Animation.START_ON_FIRST_FRAME);
    shakeAnimation.setAnimationListener(new BaseAnimationListener(handler) {
      @Override
      public void onAnimationEnd(Animation animation) {
        super.onAnimationEnd(animation);
        mState = CardState.NORMAL;
      }
    });
    updateAnimation(handler, shakeAnimation);
  }

  public void updateBounds(Rect bounds, final Handler handler) {
    Rect oldBounds = mBounds;
    mBounds = new Rect(bounds);
    Log.i(TAG, "mBounds = " + mBounds);
    if (oldBounds == null || oldBounds.width() != mBounds.width()
        || oldBounds.height() != mBounds.height()) {
      regenerateBitmapDrawables();
    }
    Animation transitionAnimation = null;
    if (bounds.equals(oldBounds)) {
      // No change
      return;
    } else if (oldBounds == null) {
      // This CardDrawable is new.
      if (mShouldSlideIn) {
        transitionAnimation = new TranslateAnimation(0 - bounds.centerX(), 0,
            0 - bounds.centerY(), 0);
        mShouldSlideIn = false;
      } else {
        transitionAnimation = new AlphaAnimation(0, mAlpha);
      }
    } else {
      // This CardDrawable is old
      transitionAnimation = new TranslateAnimation(oldBounds.centerX()
          - bounds.centerX(), 0, oldBounds.centerY() - bounds.centerY(), 0);
      mDrawOrder = 1;
    }
    transitionAnimation.setInterpolator(new AccelerateInterpolator());
    transitionAnimation.setDuration(800);
    transitionAnimation.setStartTime(Animation.START_ON_FIRST_FRAME);

    transitionAnimation
        .setAnimationListener(new BaseAnimationListener(handler) {
          @Override
          public void onAnimationEnd(Animation animation) {
            super.onAnimationEnd(animation);
            mListener.onAnimationFinished();
            mDrawOrder = 0;
          }
        });
    updateAnimation(handler, transitionAnimation);
  }

  private void updateAnimation(final Handler handler, Animation animation) {
    if (mAnimation != null) {
      mAnimation.cancel();
    }
    mAnimation = animation;
  }

  public int getDrawOrder() {
    return mDrawOrder;
  }

  @Override
  public int compareTo(CardDrawable another) {
    return Ints.compare(mDrawOrder, another.mDrawOrder);
  }

  public void setShouldSlideIn() {
    mShouldSlideIn = true;
  }

  public static Bitmap drawableToBitmap(Drawable drawable) {
    if (drawable instanceof BitmapDrawable) {
      return ((BitmapDrawable) drawable).getBitmap();
    }

    Bitmap bitmap = Bitmap.createBitmap(
        drawable.getIntrinsicWidth(),
        drawable.getIntrinsicHeight(),
        Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    drawable.draw(canvas);

    return bitmap;
  }
}
