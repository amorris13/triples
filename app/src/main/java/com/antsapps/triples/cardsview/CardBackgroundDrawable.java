package com.antsapps.triples.cardsview;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;

import java.util.Arrays;

class CardBackgroundDrawable extends Drawable {

  private static final int SHADOW_OFFSET_VERTICAL = 4;

  private static final int SHADOW_OFFSET_HORIZONTAL = 4;

  private static final int CORNER_RADIUS = 0;

  private static final int INSET_PX = 15;

  private final RoundRectShape mCardShape;

  private final ShapeDrawable mBackground;

  private final ShapeDrawable mShadow;

  private final ShapeDrawable mOutline;
  private final ShapeDrawable mHintOutline;

  private boolean mSelected;
  private boolean mHinted;

  CardBackgroundDrawable() {
    float[] outerR = new float[8];
    Arrays.fill(outerR, CORNER_RADIUS);
    mCardShape = new RoundRectShape(outerR, null, null);

    mBackground = new ShapeDrawable(mCardShape);
    mBackground.getPaint().setColor(Color.WHITE);

    mShadow = new ShapeDrawable(mCardShape);
    mShadow.getPaint().setColor(0x20202020);

    mOutline = new ShapeDrawable(mCardShape);
    mOutline.getPaint().setStyle(Paint.Style.STROKE);
    mOutline.getPaint().setColor(Color.BLUE);
    mOutline.getPaint().setStrokeWidth(5);

    mHintOutline = new ShapeDrawable(mCardShape);
    mHintOutline.getPaint().setStyle(Paint.Style.STROKE);
    mHintOutline.getPaint().setColor((0x400000FF));
    mHintOutline.getPaint().setStrokeWidth(15);
    mHintOutline.getPaint().setPathEffect(new DashPathEffect(new float[] {20, 10}, 0));
  }

  @Override
  public void draw(Canvas canvas) {
    if (mHinted) {
      mHintOutline.draw(canvas);
    }
    mShadow.draw(canvas);
    mBackground.draw(canvas);
    if (mSelected) {
      mOutline.draw(canvas);
    }
  }

  @Override
  public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }

  @Override
  public void setAlpha(int alpha) {
    mOutline.setAlpha(alpha);
    mHintOutline.setAlpha(alpha);
    mBackground.setAlpha(alpha);
    mShadow.setAlpha(alpha);
  }

  @Override
  public void setColorFilter(ColorFilter cf) {
    mOutline.setColorFilter(cf);
    mHintOutline.setColorFilter(cf);
    mBackground.setColorFilter(cf);
    mShadow.setColorFilter(cf);
  }

  void setSelected(boolean selected) {
    mSelected = selected;
  }

  public void setHinted(boolean hinted) {
    mHinted = hinted;
  }

  @Override
  public void setBounds(Rect bounds) {
    Rect cardBounds = new Rect(bounds);
    cardBounds.inset(INSET_PX, INSET_PX);
    mBackground.setBounds(cardBounds);
    mOutline.setBounds(cardBounds);
    mHintOutline.setBounds(cardBounds);

    cardBounds.offset(SHADOW_OFFSET_HORIZONTAL, SHADOW_OFFSET_VERTICAL);
    mShadow.setBounds(cardBounds);
  }
}
