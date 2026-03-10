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
import android.util.Log;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.CycleInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import androidx.annotation.Nullable;
import com.antsapps.triples.CardCustomizationUtils;
import com.antsapps.triples.R;
import com.antsapps.triples.backend.Card;
import com.google.common.primitives.Ints;

public class CardDrawable extends Drawable implements Comparable<CardDrawable> {

  public static final int DEFAULT_ANIMATION_DURATION_MS = 800;

  private class BaseAnimationListener implements AnimationListener {

    BaseAnimationListener() {}

    @Override
    public void onAnimationStart(Animation animation) {
      if (mAnimationHandler != null) {
        mAnimationHandler.sendMessage(Message.obtain(mAnimationHandler, CardsView.WHAT_INCREMENT));
      }
    }

    @Override
    public void onAnimationEnd(Animation animation) {
      if (mAnimationHandler != null) {
        mAnimationHandler.sendMessage(Message.obtain(mAnimationHandler, CardsView.WHAT_DECREMENT));
      }
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

  private OnAnimationFinishedListener mListener;

  private float mAlpha;

  private boolean mShouldSlideIn;

  private final Context mContext;
  private final Handler mAnimationHandler;

  public CardDrawable(
      Context context, Handler animationHandler, Card card, OnAnimationFinishedListener listener) {
    mContext = context;
    mAnimationHandler = animationHandler;

    mCard = card;
    mListener = listener;
  }

  public void setAnimationFinishedListener(OnAnimationFinishedListener listener) {
    mListener = listener;
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

  public void regenerateCachedDrawable() {
    Log.v(TAG, "regen drawables for mCard = " + mCard);
    Rect bounds = new Rect(mBounds);
    bounds.offsetTo(0, 0);

    // Rescale bounds for density
    float density = mContext.getResources().getDisplayMetrics().density;
    bounds.set(0, 0, (int) (bounds.right / density * 2), (int) (bounds.bottom / density * 2));

    Bitmap bitmap = Bitmap.createBitmap(bounds.width(), bounds.height(), Config.ARGB_8888);
    Canvas tmpCanvas = new Canvas(bitmap);

    CardBackgroundDrawable mCardBackground = new CardBackgroundDrawable(mContext);
    mCardBackground.setBounds(bounds);
    mCardBackground.setSelected(mSelected || mShakeAnimating);
    mCardBackground.setHinted(mHinted);
    mCardBackground.draw(tmpCanvas);

    SymbolDrawable mSymbol = new SymbolDrawable(mContext, mCard);
    for (Rect rect : CardCustomizationUtils.getBoundsForNumId(mCard.mNumber, bounds)) {
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
      if (mBounds != null) {
        regenerateCachedDrawable();
      }
    }
  }

  void setHinted(boolean hinted) {
    if (mHinted != hinted) {
      mHinted = hinted;
      regenerateCachedDrawable();
      if (hinted && mAnimationHandler != null) {
        // Throb animation
        Animation throbAnimation =
            new ScaleAnimation(1.0f, 1.15f, 1.0f, 1.15f, mBounds.centerX(), mBounds.centerY());
        throbAnimation.setInterpolator(new CycleInterpolator(0.5f));
        throbAnimation.setDuration(getAnimationDuration());
        throbAnimation.setStartTime(Animation.START_ON_FIRST_FRAME);
        throbAnimation.setAnimationListener(new BaseAnimationListener());
        updateAnimation(throbAnimation);
      }
    }
  }

  void onIncorrectTriple() {
    onIncorrectTriple(false);
  }

  void onIncorrectTriple(boolean horizontalOnly) {
    mSelected = false;
    mShakeAnimating = true;
    if (mAnimationHandler == null) {
      mShakeAnimating = false;
      regenerateCachedDrawable();
      return;
    }
    // Shake animation
    Animation shakeAnimation;
    if (horizontalOnly) {
      shakeAnimation = new TranslateAnimation(0, 10, 0, 0);
    } else {
      shakeAnimation = new RotateAnimation(0, 5, mBounds.centerX(), mBounds.centerY());
    }
    shakeAnimation.setInterpolator(new CycleInterpolator(4));
    shakeAnimation.setDuration(getAnimationDuration());
    shakeAnimation.setStartTime(Animation.START_ON_FIRST_FRAME);
    shakeAnimation.setAnimationListener(
        new BaseAnimationListener() {
          @Override
          public void onAnimationEnd(Animation animation) {
            super.onAnimationEnd(animation);
            mShakeAnimating = false;
            regenerateCachedDrawable();
          }
        });
    updateAnimation(shakeAnimation);
  }

  public void updateBounds(Rect bounds, boolean animate) {
    Rect oldBounds = mBounds;
    mBounds = new Rect(bounds);
    setBounds(mBounds);
    Log.i(TAG, "mBounds = " + mBounds);
    if (oldBounds == null
        || oldBounds.width() != mBounds.width()
        || oldBounds.height() != mBounds.height()) {
      regenerateCachedDrawable();
    }
    if (mBounds.equals(oldBounds)) {
      // No change
      return;
    }
    if (!animate) {
      return;
    }
    if (mAnimationHandler == null) {
      if (mListener != null) {
        mListener.onAnimationFinished();
      }
      return;
    }
    Animation transitionAnimation = null;
    if (oldBounds == null) {
      // This CardDrawable is new.
      if (mShouldSlideIn) {
        transitionAnimation =
            new TranslateAnimation(0 - bounds.centerX(), 0, 0 - bounds.centerY(), 0);
        mShouldSlideIn = false;
      } else {
        transitionAnimation = new AlphaAnimation(mAlpha, 1);
      }
    } else {
      // This CardDrawable is an existing drawable
      AnimationSet set = new AnimationSet(true);
      set.addAnimation(
          new ScaleAnimation(
              (float) oldBounds.width() / mBounds.width(),
              1.0f,
              (float) oldBounds.height() / mBounds.height(),
              1.0f,
              Animation.ABSOLUTE,
              mBounds.centerX(),
              Animation.ABSOLUTE,
              mBounds.centerY()));
      set.addAnimation(
          new TranslateAnimation(
              oldBounds.centerX() - mBounds.centerX(),
              0,
              oldBounds.centerY() - mBounds.centerY(),
              0));

      transitionAnimation = set;
      mDrawOrder = 1;
    }
    transitionAnimation.setInterpolator(new AccelerateInterpolator());

    transitionAnimation.setDuration(getAnimationDuration());

    transitionAnimation.setStartTime(Animation.START_ON_FIRST_FRAME);

    transitionAnimation.setAnimationListener(
        new BaseAnimationListener() {
          @Override
          public void onAnimationEnd(Animation animation) {
            super.onAnimationEnd(animation);
            if (mListener != null) {
              mListener.onAnimationFinished();
            }
            mDrawOrder = 0;
          }
        });
    updateAnimation(transitionAnimation);
  }

  public void updateAnimation(Animation animation) {
    if (mAnimation != null) {
      mAnimation.cancel();
    }
    mAnimation = animation;
  }

  private int getAnimationDuration() {
    return PreferenceManager.getDefaultSharedPreferences(mContext)
        .getInt(mContext.getString(R.string.pref_animation_speed), DEFAULT_ANIMATION_DURATION_MS);
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
