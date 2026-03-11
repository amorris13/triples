package com.antsapps.triples;

import android.view.View;
import com.antsapps.triples.cardsview.VerticalCardsView;

public class TestUtils {

  public static void clickCardAtPosition(VerticalCardsView cardsView, int index) {
    int widthOfCard = cardsView.getWidth() / VerticalCardsView.COLUMNS;
    // height of card calculation from VerticalCardsView
    int heightOfCard = (int) (widthOfCard * ((Math.sqrt(5) - 1) / 2));

    int x = (index % VerticalCardsView.COLUMNS) * widthOfCard + widthOfCard / 2;
    int y = (index / VerticalCardsView.COLUMNS) * heightOfCard + heightOfCard / 2;

    // With the move to CardViews, we need to click the child View at that position
    for (int i = 0; i < cardsView.getChildCount(); i++) {
      View child = cardsView.getChildAt(i);
      if (child.getLeft() <= x
          && x <= child.getRight()
          && child.getTop() <= y
          && y <= child.getBottom()) {
        child.performClick();
        return;
      }
    }
  }
}
