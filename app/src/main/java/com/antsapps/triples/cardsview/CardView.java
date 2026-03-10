package com.antsapps.triples.cardsview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.CycleInterpolator;
import android.view.animation.Interpolator;
import androidx.annotation.Nullable;
import com.antsapps.triples.CardCustomizationUtils;
import com.antsapps.triples.R;
import com.antsapps.triples.backend.Card;

public class CardView extends View {

  public static final int DEFAULT_ANIMATION_DURATION_MS = 800;
  public static final float HEIGHT_OVER_WIDTH = (float) ((Math.sqrt(5) - 1) / 2);

  private Card mCard;
  private boolean mSelected = false;
  private boolean mHinted = false;
  private boolean mShakeAnimating = false;
  private Bitmap mCachedBitmap;
  private CardBackgroundDrawable mCardBackground;
  private SymbolDrawable mSymbol;
  private boolean mShouldSlideIn = false;

  public CardView(Context context) {
    super(context);
    init(context);
  }

  public CardView(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public CardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  public CardView(Context context, Card card) {
    super(context);
    mCard = card;
    init(context);
  }

  private void init(Context context) {
    mCardBackground = new CardBackgroundDrawable(context);
    if (mCard != null) {
      mSymbol = new SymbolDrawable(context, mCard);
    }
    setClickable(true);
    setFocusable(true);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      TypedValue outValue = new TypedValue();
      getContext()
          .getTheme()
          .resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
      setForeground(getContext().getDrawable(outValue.resourceId));
    }
  }

  public void setCard(Card card) {
    mCard = card;
    mSymbol = new SymbolDrawable(getContext(), mCard);
    regenerateCache();
    invalidate();
  }

  public Card getCard() {
    return mCard;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = MeasureSpec.getSize(widthMeasureSpec);
    int height = (int) (width * HEIGHT_OVER_WIDTH);
    setMeasuredDimension(width, height);
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    if (w > 0 && h > 0) {
      regenerateCache();
    }
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (mCachedBitmap != null) {
      canvas.drawBitmap(mCachedBitmap, 0, 0, null);
    }
  }

  /** Draws the card content directly into the provided canvas within the given bounds. */
  public void drawCardContent(Canvas canvas, Rect bounds) {
    mCardBackground.setBounds(bounds);
    mCardBackground.setSelected(mSelected || mShakeAnimating);
    mCardBackground.setHinted(mHinted);
    mCardBackground.draw(canvas);

    if (mSymbol != null) {
      for (Rect rect : CardCustomizationUtils.getBoundsForNumId(mCard.mNumber, bounds)) {
        mSymbol.setBounds(rect);
        mSymbol.draw(canvas);
      }
    }
  }

  private void regenerateCache() {
    int width = getWidth();
    int height = getHeight();
    if (width <= 0 || height <= 0 || mCard == null) {
      return;
    }

    Bitmap oldBitmap = mCachedBitmap;
    mCachedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(mCachedBitmap);

    drawCardContent(canvas, new Rect(0, 0, width, height));
    if (oldBitmap != null) {
      oldBitmap.recycle();
    }
  }

  public void setSelected(boolean selected) {
    if (mSelected != selected) {
      mSelected = selected;
      regenerateCache();
      invalidate();
    }
  }

  public void setHinted(boolean hinted) {
    if (mHinted != hinted) {
      mHinted = hinted;
      regenerateCache();
      invalidate();
      if (hinted) {
        animate()
            .scaleX(1.15f)
            .scaleY(1.15f)
            .setDuration(getAnimationDuration())
            .setInterpolator(new CycleInterpolator(0.5f))
            .start();
      }
    }
  }

  public void onIncorrectTriple() {
    onIncorrectTriple(false);
  }

  public void onIncorrectTriple(boolean horizontalOnly) {
    mSelected = false;
    mShakeAnimating = true;
    regenerateCache();
    invalidate();

    if (horizontalOnly) {
      animate()
          .translationXBy(10)
          .setDuration(getAnimationDuration())
          .setInterpolator(new CycleInterpolator(4))
          .setListener(
              new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                  mShakeAnimating = false;
                  regenerateCache();
                  invalidate();
                }
              })
          .start();
    } else {
      animate()
          .rotation(5)
          .setDuration(getAnimationDuration())
          .setInterpolator(new CycleInterpolator(4))
          .setListener(
              new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                  mShakeAnimating = false;
                  regenerateCache();
                  invalidate();
                }
              })
          .start();
    }
  }

  public void animateFoundCard(Rect target, Interpolator interpolator, Runnable onFinished) {
    setPivotX(0);
    setPivotY(0);
    animate()
        .x(target.left)
        .y(target.top)
        .scaleX((float) target.width() / getWidth())
        .scaleY((float) target.height() / getHeight())
        .setInterpolator(interpolator)
        .setDuration(getAnimationDuration())
        .setListener(
            new AnimatorListenerAdapter() {
              @Override
              public void onAnimationEnd(Animator animation) {
                if (onFinished != null) {
                  onFinished.run();
                }
              }
            })
        .start();
  }

  private int getAnimationDuration() {
    return PreferenceManager.getDefaultSharedPreferences(getContext())
        .getInt(
            getContext().getString(R.string.pref_animation_speed), DEFAULT_ANIMATION_DURATION_MS);
  }

  public void setShouldSlideIn() {
    mShouldSlideIn = true;
  }

  public boolean shouldSlideIn() {
    return mShouldSlideIn;
  }

  public void resetSlideIn() {
    mShouldSlideIn = false;
  }
}
