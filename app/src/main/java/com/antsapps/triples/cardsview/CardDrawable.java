package com.antsapps.triples.cardsview;

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
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
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

import com.antsapps.triples.R;
import com.antsapps.triples.backend.Card;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

import java.util.List;

class CardDrawable extends Drawable implements Comparable<CardDrawable> {

  private static final int INCORRECT_ANIMATION_DURATION_MS = 800;
  private static final int TRANSITION_DURATION_MS = 800;
  private static final int TRANSITION_DURATION_MS_FAST = 200;

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
    public void onAnimationRepeat(Animation animation) {}
  }

  interface OnAnimationFinishedListener {
    void onAnimationFinished();
  }

  private static final String TAG = "CardDrawable";

  private int mDrawOrder;

  private final Card mCard;

  private BitmapDrawable mCachedDrawable;

  private Rect mBounds;

  private boolean mSelected = false;
  private boolean mShakeAnimating = false;
  private boolean mHinted = false;

  private Animation mAnimation;
  private final Transformation mTransformation = new Transformation();

  private final OnAnimationFinishedListener mListener;

  private float mAlpha;

  private boolean mShouldSlideIn;

  private int mTransitionDurationMillis = TRANSITION_DURATION_MS;

  private final Context mContext;

  CardDrawable(Context context, Card card, OnAnimationFinishedListener listener) {
    mContext = context;

    mCard = card;
    mListener = listener;

    mTransitionDurationMillis =
        PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.pref_fast_animations), false)
            ? TRANSITION_DURATION_MS_FAST
            : TRANSITION_DURATION_MS;
  }

  private static List<Rect> getBoundsForNumId(int id, Rect bounds) {
    List<Rect> rects = Lists.newArrayList();

    int width = bounds.width();
    int height = bounds.height();
    int halfSideLength = width / 10;
    int gap = halfSideLength / 2;
    switch (id) {
      case 0:
        rects.add(squareFromCenterAndRadius(width / 2, height / 2, halfSideLength));
        break;
      case 1:
        rects.add(
            squareFromCenterAndRadius(
                width / 2 - gap / 2 - halfSideLength, height / 2, halfSideLength));
        rects.add(
            squareFromCenterAndRadius(
                width / 2 + gap / 2 + halfSideLength, height / 2, halfSideLength));
        break;
      case 2:
        rects.add(
            squareFromCenterAndRadius(
                width / 2 - gap - halfSideLength * 2, height / 2, halfSideLength));
        rects.add(squareFromCenterAndRadius(width / 2, height / 2, halfSideLength));
        rects.add(
            squareFromCenterAndRadius(
                width / 2 + gap + halfSideLength * 2, height / 2, halfSideLength));
        break;
    }
    return rects;
  }

  private static Rect squareFromCenterAndRadius(int centerX, int centerY, int radius) {
    return new Rect(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
  }

  boolean isAnimating() {
    return mAnimation != null && mAnimation.hasStarted() && !mAnimation.hasEnded();
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
      if (!anim.isInitialized()) {
        anim.initialize(bounds.width(), bounds.height(), 0, 0);
      }

      anim.getTransformation(AnimationUtils.currentAnimationTimeMillis(), mTransformation);

      canvas.concat(mTransformation.getMatrix());
      setAlpha((int) (mTransformation.getAlpha() * 255));
    }
    drawInternal(canvas, bounds);
    canvas.restoreToCount(sc);
  }

  private void drawInternal(Canvas canvas, Rect bounds) {
    if (mCachedDrawable == null) {
      regenerateCachedDrawable();
    }
    BitmapDrawable drawable = mCachedDrawable;
    if (drawable == null) {
      return;
    }
    drawable.setBounds(bounds);
    drawable.draw(canvas);
  }

  private void regenerateCachedDrawable() {
    Log.v(TAG, "regen drawables for mCard = " + mCard);
    Rect bounds = new Rect(mBounds);
    bounds.offsetTo(0, 0);

    // Rescale bounds for density
    float density = mContext.getResources().getDisplayMetrics().density;
    bounds.set(0, 0, (int) (bounds.right / density * 2), (int) (bounds.bottom / density * 2));

    Bitmap bitmap = Bitmap.createBitmap(bounds.width(), bounds.height(), Config.ARGB_8888);
    Canvas tmpCanvas = new Canvas(bitmap);

    CardBackgroundDrawable mCardBackground = new CardBackgroundDrawable();
    mCardBackground.setBounds(bounds);
    mCardBackground.setSelected(mSelected || mShakeAnimating);
    mCardBackground.setHinted(mHinted);
    mCardBackground.draw(tmpCanvas);

    SymbolDrawable mSymbol = new SymbolDrawable(mCard);
    for (Rect rect : getBoundsForNumId(mCard.mNumber, bounds)) {
      mSymbol.setBounds(rect);
      mSymbol.draw(tmpCanvas);
    }

    mCachedDrawable = new BitmapDrawable(mContext.getResources(), bitmap);
  }

  @Override
  public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }

  @Override
  public void setAlpha(int alpha) {
    mAlpha = (float) alpha / 255;
    if (mCachedDrawable != null) {
      mCachedDrawable.setAlpha(alpha);
    }
  }

  @Override
  public void setColorFilter(@Nullable ColorFilter colorFilter) {
    if (mCachedDrawable != null) {
      mCachedDrawable.setColorFilter(colorFilter);
    }
  }

  void setSelected(boolean selected) {
    if (mSelected != selected) {
      mSelected = selected;
      regenerateCachedDrawable();
    }
  }

  void setHinted(boolean hinted) {
    if (mHinted != hinted) {
      mHinted = hinted;
      regenerateCachedDrawable();
    }
  }

  void onIncorrectTriple(final Handler handler) {
    mSelected = false;
    mShakeAnimating = true;
    // Shake animation
    Animation shakeAnimation = new RotateAnimation(0, 5, mBounds.centerX(), mBounds.centerY());
    shakeAnimation.setInterpolator(new CycleInterpolator(4));
    shakeAnimation.setDuration(INCORRECT_ANIMATION_DURATION_MS);
    shakeAnimation.setStartTime(Animation.START_ON_FIRST_FRAME);
    shakeAnimation.setAnimationListener(
        new BaseAnimationListener(handler) {
          @Override
          public void onAnimationEnd(Animation animation) {
            super.onAnimationEnd(animation);
            mShakeAnimating = false;
            regenerateCachedDrawable();
          }
        });
    updateAnimation(shakeAnimation);
  }

  void updateBounds(Rect bounds, final Handler handler) {
    Rect oldBounds = mBounds;
    mBounds = new Rect(bounds);
    Log.i(TAG, "mBounds = " + mBounds);
    if (oldBounds == null
        || oldBounds.width() != mBounds.width()
        || oldBounds.height() != mBounds.height()) {
      regenerateCachedDrawable();
    }
    Animation transitionAnimation = null;
    if (bounds.equals(oldBounds)) {
      // No change
      return;
    } else if (oldBounds == null) {
      // This CardDrawable is new.
      if (mShouldSlideIn) {
        transitionAnimation =
            new TranslateAnimation(0 - bounds.centerX(), 0, 0 - bounds.centerY(), 0);
        mShouldSlideIn = false;
      } else {
        transitionAnimation = new AlphaAnimation(mAlpha, 1);
      }
    } else {
      // This CardDrawable is old
      transitionAnimation =
          new TranslateAnimation(
              oldBounds.centerX() - bounds.centerX(), 0, oldBounds.centerY() - bounds.centerY(), 0);
      mDrawOrder = 1;
    }
    transitionAnimation.setInterpolator(new AccelerateInterpolator());
    transitionAnimation.setDuration(mTransitionDurationMillis);
    transitionAnimation.setStartTime(Animation.START_ON_FIRST_FRAME);

    transitionAnimation.setAnimationListener(
        new BaseAnimationListener(handler) {
          @Override
          public void onAnimationEnd(Animation animation) {
            super.onAnimationEnd(animation);
            mListener.onAnimationFinished();
            mDrawOrder = 0;
          }
        });
    updateAnimation(transitionAnimation);
  }

  private void updateAnimation(Animation animation) {
    if (mAnimation != null) {
      mAnimation.cancel();
    }
    mAnimation = animation;
  }

  int getDrawOrder() {
    return mDrawOrder;
  }

  @Override
  public int compareTo(CardDrawable another) {
    return Ints.compare(mDrawOrder, another.mDrawOrder);
  }

  void setShouldSlideIn() {
    mShouldSlideIn = true;
  }

  private static Bitmap drawableToBitmap(Drawable drawable) {
    if (drawable instanceof BitmapDrawable) {
      return ((BitmapDrawable) drawable).getBitmap();
    }

    Bitmap bitmap =
        Bitmap.createBitmap(
            drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    drawable.draw(canvas);

    return bitmap;
  }
}
