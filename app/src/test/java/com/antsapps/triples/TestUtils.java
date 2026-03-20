package com.antsapps.triples;

import android.view.View;
import com.antsapps.triples.cardsview.CardsView;

public class TestUtils {

  public static void clickCardAtPosition(CardsView cardsView, int index) {
    int widthOfCard = cardsView.getWidth() / CardsView.COLUMNS;
    // height of card calculation from CardsView
    int heightOfCard = (int) (widthOfCard * ((Math.sqrt(5) - 1) / 2));

    int x = (index % CardsView.COLUMNS) * widthOfCard + widthOfCard / 2;
    int y = (index / CardsView.COLUMNS) * heightOfCard + heightOfCard / 2;

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
