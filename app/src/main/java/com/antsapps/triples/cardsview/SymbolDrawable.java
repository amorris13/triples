package com.antsapps.triples.cardsview;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import androidx.preference.PreferenceManager;
import com.antsapps.triples.CardCustomizationUtils;
import com.antsapps.triples.R;
import com.antsapps.triples.backend.Card;

public class SymbolDrawable extends Drawable {

  public static final int OUTLINE_WIDTH = 2;

  private final Card mCard;

  private final ShapeDrawable mOutline;
  private final ShapeDrawable mFill;

  public SymbolDrawable(Context context, Card card) {
    mCard = card;
    mOutline = getOutlineForCard(context, card);
    mFill = getFillForCard(context, card);
  }

  private static ShapeDrawable getOutlineForCard(Context context, Card card) {
    ShapeDrawable symbol = new ShapeDrawable(getShapeForId(context, card.mShape));
    symbol.getPaint().setColor(getColorForId(context, card.mColor));
    symbol.getPaint().setStyle(Paint.Style.STROKE);
    float density = context.getResources().getDisplayMetrics().density;
    symbol.getPaint().setStrokeWidth(OUTLINE_WIDTH * density);
    return symbol;
  }

  private static ShapeDrawable getFillForCard(Context context, Card card) {
    ShapeDrawable symbol = new ShapeDrawable(getShapeForId(context, card.mShape));
    symbol.getPaint().setShader(getShaderForPatternId(context, card.mPattern, card.mColor));
    symbol.getPaint().setStyle(Paint.Style.FILL);
    return symbol;
  }

  private static Shader getShaderForPatternId(Context context, int patternId, int colorId) {
    int color = getColorForId(context, colorId);
    switch (patternId) {
      case 0: // Empty
        return new BitmapShader(
            Bitmap.createBitmap(new int[] {0}, 1, 1, Bitmap.Config.ARGB_8888),
            Shader.TileMode.REPEAT,
            Shader.TileMode.REPEAT);
      case 1: // Customizable Shaded
        return getCustomShadedShader(context, color);
      case 2: // Solid
        return new BitmapShader(
            Bitmap.createBitmap(new int[] {color}, 1, 1, Bitmap.Config.ARGB_8888),
            Shader.TileMode.REPEAT,
            Shader.TileMode.REPEAT);
      default:
        return null;
    }
  }

  private static Shader getCustomShadedShader(Context context, int color) {
    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    String pattern =
        sharedPref.getString(context.getString(R.string.pref_shaded_pattern), "stripes");
    return CardCustomizationUtils.getCustomShadedShader(context, color, pattern);
  }

  public static Shape getShapeForId(Context context, int id) {
    return CardCustomizationUtils.getShapeForId(context, id);
  }

  public static int getColorForId(Context context, int id) {
    return CardCustomizationUtils.getColorForId(context, id);
  }

  @Override
  public void draw(Canvas canvas) {
    mFill.draw(canvas);
    mOutline.draw(canvas);
  }

  @Override
  public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }

  @Override
  public void setAlpha(int alpha) {
    mFill.setAlpha(alpha);
    mOutline.setAlpha(alpha);
  }

  @Override
  public void setColorFilter(ColorFilter cf) {
    mFill.setColorFilter(cf);
    mOutline.setColorFilter(cf);
  }

  @Override
  public void setBounds(Rect bounds) {
    mFill.setBounds(bounds);
    mOutline.setBounds(bounds);
  }
}
