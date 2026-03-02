package com.antsapps.triples.cardsview;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.Shape;
import androidx.preference.PreferenceManager;

import com.antsapps.triples.R;
import com.antsapps.triples.backend.Card;
import com.google.common.primitives.Ints;

import java.util.Arrays;

class SymbolDrawable extends Drawable {

  private static final int OUTLINE_WIDTH = 4;
  private static final int STRIPE_WIDTH = 3;

  private final Card mCard;

  private final ShapeDrawable mOutline;
  private final ShapeDrawable mFill;

  SymbolDrawable(Context context, Card card) {
    mCard = card;
    mOutline = getOutlineForCard(context, card);
    mFill = getFillForCard(context, card);
  }

  private static ShapeDrawable getOutlineForCard(Context context, Card card) {
    ShapeDrawable symbol = new ShapeDrawable(getShapeForId(context, card.mShape));
    symbol.getPaint().setColor(getColorForId(context, card.mColor));
    symbol.getPaint().setStyle(Style.STROKE);
    symbol.getPaint().setStrokeWidth(OUTLINE_WIDTH);
    return symbol;
  }

  private static ShapeDrawable getFillForCard(Context context, Card card) {
    ShapeDrawable symbol = new ShapeDrawable(getShapeForId(context, card.mShape));
    symbol.getPaint().setShader(getShaderForPatternId(context, card.mPattern, card.mColor));
    symbol.getPaint().setStyle(Style.FILL);
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
    float density = context.getResources().getDisplayMetrics().density;
    int thickness = Math.max(1, Math.round(STRIPE_WIDTH * density));
    Bitmap bm;
    if (pattern.equals("stripes")) {
      int[] pixels = Ints.concat(initIntArray(color, thickness), initIntArray(0, thickness));
      bm = Bitmap.createBitmap(pixels, pixels.length, 1, Bitmap.Config.ARGB_8888);
    } else if (pattern.equals("dots")) {
      bm = Bitmap.createBitmap(thickness * 2, thickness * 2, Bitmap.Config.ARGB_8888);
      for (int i = 0; i < thickness; i++) {
        for (int j = 0; j < thickness; j++) {
          bm.setPixel(i, j, color);
        }
      }
    } else if (pattern.equals("crosshatch")) {
      int size = thickness * 3;
      bm = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
      for (int i = 0; i < size; i++) {
        for (int j = 0; j < size; j++) {
          if ((i + j) % size < thickness || (i - j + size) % size < thickness) {
            bm.setPixel(i, j, color);
          }
        }
      }
    } else if (pattern.equals("lighter")) {
      int lighterColor = Color.argb(128, Color.red(color), Color.green(color), Color.blue(color));
      bm = Bitmap.createBitmap(new int[] {lighterColor}, 1, 1, Bitmap.Config.ARGB_8888);
    } else {
      // default to stripes
      int[] pixels = Ints.concat(initIntArray(color, thickness), initIntArray(0, thickness));
      bm = Bitmap.createBitmap(pixels, pixels.length, 1, Bitmap.Config.ARGB_8888);
    }
    return new BitmapShader(bm, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
  }

  private static int[] initIntArray(int value, int length) {
    int[] arr = new int[length];
    Arrays.fill(arr, value);
    return arr;
  }

  public static Shape getShapeForId(Context context, int id) {
    return com.antsapps.triples.CardCustomizationUtils.getShapeForId(context, id);
  }

  public static int getColorForId(Context context, int id) {
    return com.antsapps.triples.CardCustomizationUtils.getColorForId(context, id);
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
