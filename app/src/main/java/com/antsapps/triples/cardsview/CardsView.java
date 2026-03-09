package com.antsapps.triples.cardsview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.OnValidTripleSelectedListener;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;

/**
 * A ViewGroup that manages a grid of CardViews.
 */
public class CardsView extends ViewGroup implements Game.GameRenderer {

  private static final String TAG = "CardsView";
  private static final int COLUMNS = 3;
  private static final float HEIGHT_OVER_WIDTH = (float) ((Math.sqrt(5) - 1) / 2);

  private ImmutableList<Card> mCards = ImmutableList.of();
  private final Map<Card, CardView> mCardViews = Maps.newHashMap();
  private final Set<Card> mCurrentlySelected = Sets.newHashSet();
  private final Set<Card> mCurrentlyHinted = Sets.newHashSet();
  private OnValidTripleSelectedListener mOnValidTripleSelectedListener;

  private int mWidthOfCard;
  private int mHeightOfCard;

  public CardsView(Context context) {
    this(context, null);
  }

  public CardsView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public CardsView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = MeasureSpec.getSize(widthMeasureSpec);
    mWidthOfCard = width / COLUMNS;
    mHeightOfCard = (int) (mWidthOfCard * HEIGHT_OVER_WIDTH);

    int rows = (int) Math.ceil((double) mCards.size() / COLUMNS);
    int height = mHeightOfCard * rows;

    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildAt(i);
      child.measure(
          MeasureSpec.makeMeasureSpec(mWidthOfCard, MeasureSpec.EXACTLY),
          MeasureSpec.makeMeasureSpec(mHeightOfCard, MeasureSpec.EXACTLY));
    }

    setMeasuredDimension(width, height);
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    for (int i = 0; i < mCards.size(); i++) {
      Card card = mCards.get(i);
      CardView cardView = mCardViews.get(card);
      if (cardView != null) {
        int left = (i % COLUMNS) * mWidthOfCard;
        int top = (i / COLUMNS) * mHeightOfCard;
        cardView.layout(left, top, left + mWidthOfCard, top + mHeightOfCard);
      }
    }
  }

  @Override
  public void updateCardsInPlay(ImmutableList<Card> newCards) {
    Set<Card> cardsToRemove = Sets.newHashSet(mCards);
    cardsToRemove.removeAll(newCards);

    for (Card card : cardsToRemove) {
      CardView cardView = mCardViews.remove(card);
      if (cardView != null) {
        if (cardView.getAnimation() != null || cardView.getScaleX() < 1.0f) {
          // It's probably animating away, let it finish and remove itself
          cardView
              .animate()
              .withEndAction(
                  () -> {
                    removeView(cardView);
                  });
        } else {
          removeView(cardView);
        }
      }
      mCurrentlySelected.remove(card);
      mCurrentlyHinted.remove(card);
    }

    mCards = newCards;

    for (int i = 0; i < mCards.size(); i++) {
      Card card = mCards.get(i);
      CardView cardView = mCardViews.get(card);
      if (cardView == null) {
        cardView = createCardView(card);
        mCardViews.put(card, cardView);
        addView(cardView);
        if (mShouldSlideInNextUpdate) {
          cardView.setAlpha(0);
          cardView.setTranslationX(-getWidth());
          cardView.animate().alpha(1).translationX(0).setDuration(500).start();
        }
      }
    }
    mShouldSlideInNextUpdate = false;

    requestLayout();
  }

  private CardView createCardView(Card card) {
    CardView cardView = new CardView(getContext());
    cardView.setCard(card);
    cardView.setOnClickListener(v -> handleCardClick((CardView) v));
    return cardView;
  }

  private void handleCardClick(CardView cardView) {
    if (!isEnabled()) return;

    Card card = cardView.getCard();
    if (mCurrentlySelected.contains(card)) {
      mCurrentlySelected.remove(card);
      cardView.setSelected(false);
    } else {
      mCurrentlySelected.add(card);
      cardView.setSelected(true);
    }

    checkSelectedCards();
  }

  private void checkSelectedCards() {
    if (mCurrentlySelected.size() == 3) {
      if (Game.isValidTriple(mCurrentlySelected)) {
        if (mOnValidTripleSelectedListener != null) {
          mOnValidTripleSelectedListener.onValidTripleSelected(mCurrentlySelected);
        }
      } else {
        for (Card card : mCurrentlySelected) {
          CardView cv = mCardViews.get(card);
          if (cv != null) {
            cv.animateIncorrect();
          }
        }
      }
      clearSelectedCards();
    }
  }

  @Override
  public void addHint(Card card) {
    CardView cardView = mCardViews.get(card);
    if (cardView != null) {
      cardView.setHinted(true);
      mCurrentlyHinted.add(card);
    }

    // Remove incorrectly selected cards
    Set<Card> toRemove = Sets.newHashSet();
    for (Card selected : mCurrentlySelected) {
      if (!mCurrentlyHinted.contains(selected)) {
        toRemove.add(selected);
        CardView cv = mCardViews.get(selected);
        if (cv != null) cv.setSelected(false);
      }
    }
    mCurrentlySelected.removeAll(toRemove);
  }

  @Override
  public void clearHintedCards() {
    for (CardView cardView : mCardViews.values()) {
      cardView.setHinted(false);
    }
    mCurrentlyHinted.clear();
  }

  @Override
  public void clearSelectedCards() {
    for (Card card : mCurrentlySelected) {
      CardView cardView = mCardViews.get(card);
      if (cardView != null) {
        cardView.setSelected(false);
      }
    }
    mCurrentlySelected.clear();
  }

  @Override
  public Set<Card> getSelectedCards() {
    return mCurrentlySelected;
  }

  public void setOnValidTripleSelectedListener(OnValidTripleSelectedListener listener) {
    mOnValidTripleSelectedListener = listener;
  }

  public void animateTripleFound(Set<Card> triple) {
    for (Card card : triple) {
      CardView cardView = mCardViews.get(card);
      if (cardView != null) {
        cardView.animate()
            .scaleX(0f)
            .scaleY(0f)
            .translationY(getHeight())
            .setDuration(500)
            .withEndAction(() -> {
              // This will be handled by updateCardsInPlay when the backend updates
            })
            .start();
      }
    }
  }

  private boolean mShouldSlideInNextUpdate;

  public void shouldSlideIn() {
    mShouldSlideInNextUpdate = true;
  }

  public void refreshDrawables() {
    for (CardView cardView : mCardViews.values()) {
      cardView.invalidate();
    }
  }

  public void updateBounds() {
    requestLayout();
  }
}
