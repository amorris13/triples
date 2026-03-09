package com.antsapps.triples;

import android.os.SystemClock;
import android.view.MotionEvent;
import com.antsapps.triples.cardsview.CardsView;

public class TestUtils {

  public static void clickCardAtPosition(CardsView cardsView, int index) {
    int widthOfCard = cardsView.getWidth() / 3;
    // height of card calculation from CardsView
    int heightOfCard = (int) (widthOfCard * ((Math.sqrt(5) - 1) / 2));

    int x = (index % 3) * widthOfCard + widthOfCard / 2;
    int y = (index / 3) * heightOfCard + heightOfCard / 2;

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
