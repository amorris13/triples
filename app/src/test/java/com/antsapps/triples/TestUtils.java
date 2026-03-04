package com.antsapps.triples;

import android.os.SystemClock;
import android.view.MotionEvent;
import com.antsapps.triples.cardsview.VerticalCardsView;

public class TestUtils {

  public static void clickCardAtPosition(VerticalCardsView cardsView, int index) {
    int widthOfCard = cardsView.getWidth() / VerticalCardsView.COLUMNS;
    // height of card calculation from VerticalCardsView
    int heightOfCard = (int) (widthOfCard * ((Math.sqrt(5) - 1) / 2));

    int x = (index % VerticalCardsView.COLUMNS) * widthOfCard + widthOfCard / 2;
    int y = (index / VerticalCardsView.COLUMNS) * heightOfCard + heightOfCard / 2;

    long downTime = SystemClock.uptimeMillis();
    long eventTime = SystemClock.uptimeMillis();
    MotionEvent event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, x, y, 0);
    cardsView.dispatchTouchEvent(event);
    event.recycle();

    event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, x, y, 0);
    cardsView.dispatchTouchEvent(event);
    event.recycle();
  }
}
