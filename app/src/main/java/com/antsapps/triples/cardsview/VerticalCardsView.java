package com.antsapps.triples.cardsview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import com.antsapps.triples.SettingsFragment;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.Game;
import java.util.List;

public class VerticalCardsView extends CardsView {

  public static final int COLUMNS = 3;

  private int mWidthOfCard;

  private int mHeightOfCard;

  public VerticalCardsView(Context context) {
    this(context, null);
  }

  public VerticalCardsView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void logValidTriple() {
    Log.v("ValidTriples", "valid positions:");
    List<Integer> validPositions = Game.getValidTriplePositions(mCards);
    for (int r = 0; r < mCards.size() / COLUMNS; r++) {
      StringBuilder sb = new StringBuilder();
      for (int c = 0; c < COLUMNS; c++) {
        sb.append(validPositions.contains(r * COLUMNS + c) ? "X" : ".");
        sb.append(" ");
      }
      Log.v("ValidTriples", sb.toString());
    }
  }

  @Override
  protected void updateMeasuredDimensions(final int widthMeasureSpec, final int heightMeasureSpec) {
    int widthOfCards = getDefaultSize(getMeasuredWidth(), widthMeasureSpec);
    if (widthOfCards == 0) {
      if (getWidth() > 0) {
        widthOfCards = getWidth();
      } else {
        widthOfCards = getResources().getDisplayMetrics().widthPixels;
      }
    }
    mWidthOfCard = widthOfCards / COLUMNS;
    mHeightOfCard = (int) (mWidthOfCard * CardView.HEIGHT_OVER_WIDTH);

    int rows = (int) Math.ceil((double) mCards.size() / COLUMNS);
    int heightOfCards = mHeightOfCard * rows;
    if (mCards.isEmpty()) {
      heightOfCards = 0;
    }

    if (widthOfCards > 0 && heightOfCards > 0) {
      mOffScreenLocation.set(
          widthOfCards, heightOfCards, widthOfCards + mWidthOfCard, heightOfCards + mHeightOfCard);
    }

    setMeasuredDimension(widthOfCards, heightOfCards);
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    mWidthOfCard = (right - left) / COLUMNS;
    mHeightOfCard = (int) (mWidthOfCard * CardView.HEIGHT_OVER_WIDTH);
    mOffScreenLocation.set(right, bottom, right + mWidthOfCard, bottom + mHeightOfCard);
    Log.i("VCV", "oL: mHOC = " + mHeightOfCard + ", mWOC = " + mWidthOfCard);

    for (int i = 0; i < mCards.size(); i++) {
      Card card = mCards.get(i);
      CardView child = mCardViews.get(card);
      if (child != null) {
        int oldLeft = child.getLeft();
        int oldTop = child.getTop();
        boolean wasLaidOut = oldLeft != 0 || oldTop != 0 || child.getWidth() != 0;

        Rect bounds = calcBounds(i);
        child.layout(bounds.left, bounds.top, bounds.right, bounds.bottom);

        if (!wasLaidOut && mCardsForReverseAnimation.contains(card)) {
          // New card that should fly in from the off-screen location
          mCardsForReverseAnimation.remove(card);
          child.setTranslationX(mOffScreenLocation.left - bounds.left);
          child.setTranslationY(mOffScreenLocation.top - bounds.top);
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            child.setTranslationZ(50f);
          }
          child
              .animate()
              .translationX(0)
              .translationY(0)
              .setDuration(SettingsFragment.getAnimationDuration(getContext()))
              .setInterpolator(new AccelerateDecelerateInterpolator())
              .setListener(
                  new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        child.setTranslationZ(0);
                      }
                    }
                  })
              .start();
        } else if (wasLaidOut && (oldLeft != bounds.left || oldTop != bounds.top)) {
          // Position changed, animate from delta back to 0
          child.setTranslationX(oldLeft - bounds.left);
          child.setTranslationY(oldTop - bounds.top);
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            child.setTranslationZ(50f);
          }

          child
              .animate()
              .translationX(0)
              .translationY(0)
              .setDuration(SettingsFragment.getAnimationDuration(getContext()))
              .setInterpolator(new AccelerateDecelerateInterpolator())
              .setListener(
                  new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        child.setTranslationZ(0);
                      }
                    }
                  })
              .start();
        }

        if (child.getAlpha() == 0) {
          child
              .animate()
              .alpha(1)
              .setDuration(SettingsFragment.getAnimationDuration(getContext()))
              .setInterpolator(new AccelerateDecelerateInterpolator())
              .start();
        }
      }
    }
  }

  @Override
  public Rect calcBounds(int i) {  // public for use in BoardHistoryActivity
    return new Rect(
        i % COLUMNS * mWidthOfCard,
        i / COLUMNS * mHeightOfCard,
        (i % COLUMNS + 1) * mWidthOfCard,
        (i / COLUMNS + 1) * mHeightOfCard);
  }

  @Override
  public int cardWidth() {
    return mWidthOfCard;
  }

  @Override
  public int cardHeight() {
    return mHeightOfCard;
  }
}
