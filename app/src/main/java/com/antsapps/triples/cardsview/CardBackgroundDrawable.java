package com.antsapps.triples.cardsview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import androidx.core.content.ContextCompat;
import com.antsapps.triples.R;
import java.util.Arrays;

public class CardBackgroundDrawable extends Drawable {

  private static final int SHADOW_OFFSET_VERTICAL = 4;

  private static final int SHADOW_OFFSET_HORIZONTAL = 4;

  private static final int CORNER_RADIUS = 8;

  public static final int INSET_DP = 8;

  private final int mInsetPx;

  private final RoundRectShape mCardShape;

  private final ShapeDrawable mBackground;

  private final ShapeDrawable mShadow;

  private final ShapeDrawable mOutline;
  private final ShapeDrawable mHintOutline;
  private final ShapeDrawable mBorder;

  private boolean mSelected;
  private boolean mHinted;

  public CardBackgroundDrawable(Context context) {
    mInsetPx = (int) context.getResources().getDisplayMetrics().density * INSET_DP;

    float[] outerR = new float[8];
    Arrays.fill(outerR, CORNER_RADIUS);
    mCardShape = new RoundRectShape(outerR, null, null);

    mBackground = new ShapeDrawable(mCardShape);
    mBackground.getPaint().setColor(ContextCompat.getColor(context, R.color.card_background));

    mShadow = new ShapeDrawable(mCardShape);
    mShadow.getPaint().setColor(ContextCompat.getColor(context, R.color.card_shadow));

    mOutline = new ShapeDrawable(mCardShape);
    mOutline.getPaint().setStyle(Paint.Style.STROKE);
    mOutline.getPaint().setColor(ContextCompat.getColor(context, R.color.card_selected_outline));
    mOutline.getPaint().setStrokeWidth(5);

    mHintOutline = new ShapeDrawable(mCardShape);
    mHintOutline.getPaint().setStyle(Paint.Style.STROKE);
    mHintOutline.getPaint().setColor(ContextCompat.getColor(context, R.color.card_hint_outline));
    mHintOutline.getPaint().setStrokeWidth(15);
    mHintOutline.getPaint().setPathEffect(new DashPathEffect(new float[] {20, 10}, 0));

    mBorder = new ShapeDrawable(mCardShape);
    mBorder.getPaint().setStyle(Paint.Style.STROKE);
    mBorder.getPaint().setColor(ContextCompat.getColor(context, R.color.colorOutlineVariant));
    mBorder.getPaint().setStrokeWidth(0);
  }

  @Override
  public void draw(Canvas canvas) {
    if (mHinted) {
      mHintOutline.draw(canvas);
    }
    mShadow.draw(canvas);
    mBackground.draw(canvas);
    mBorder.draw(canvas);
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
    mBorder.setAlpha(alpha);
  }

  @Override
  public void setColorFilter(ColorFilter cf) {
    mOutline.setColorFilter(cf);
    mHintOutline.setColorFilter(cf);
    mBackground.setColorFilter(cf);
    mShadow.setColorFilter(cf);
    mBorder.setColorFilter(cf);
  }

  void setSelected(boolean selected) {
    mSelected = selected;
  }

  public void setHinted(boolean hinted) {
    mHinted = hinted;
  }

  public Drawable getCardMask() {
    ShapeDrawable mask = new ShapeDrawable(mCardShape);
    return new InsetDrawable(mask, mInsetPx, mInsetPx, mInsetPx, mInsetPx);
  }

  @Override
  public void setBounds(Rect bounds) {
    Rect cardBounds = new Rect(bounds);
    cardBounds.inset(mInsetPx, mInsetPx);
    mBackground.setBounds(cardBounds);
    mOutline.setBounds(cardBounds);
    mHintOutline.setBounds(cardBounds);
    mBorder.setBounds(cardBounds);

    cardBounds.offset(SHADOW_OFFSET_HORIZONTAL, SHADOW_OFFSET_VERTICAL);
    mShadow.setBounds(cardBounds);
  }
}
