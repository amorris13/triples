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

public class SymbolDrawable extends Drawable {

  private static final int OUTLINE_WIDTH = 4;
  private static final int STRIPE_WIDTH = 3;

  private final Card mCard;

  private final ShapeDrawable mOutline;
  private final ShapeDrawable mFill;

  public SymbolDrawable(Context context, Card card) {
    this(context, card, null);
  }

  public SymbolDrawable(Context context, Card card, String overriddenPattern) {
    mCard = card;
    mOutline = getOutlineForCard(context, card);
    mFill = getFillForCard(context, card, overriddenPattern);
  }

  public SymbolDrawable(Shape shape, int color, Shader shader) {
    this(null, shape, color, shader);
  }

  private SymbolDrawable(Card card, Shape shape, int color, Shader shader) {
    mCard = card;
    mOutline = new ShapeDrawable(shape);
    mOutline.getPaint().setColor(color);
    mOutline.getPaint().setStyle(Style.STROKE);
    mOutline.getPaint().setStrokeWidth(OUTLINE_WIDTH);

    mFill = new ShapeDrawable(shape);
    mFill.getPaint().setShader(shader);
    mFill.getPaint().setStyle(Style.FILL);
  }

  private static ShapeDrawable getOutlineForCard(Context context, Card card) {
    ShapeDrawable symbol = new ShapeDrawable(getShapeForId(context, card.mShape));
    symbol.getPaint().setColor(getColorForId(context, card.mColor));
    symbol.getPaint().setStyle(Style.STROKE);
    symbol.getPaint().setStrokeWidth(OUTLINE_WIDTH);
    return symbol;
  }

  private static ShapeDrawable getFillForCard(Context context, Card card, String overriddenPattern) {
    ShapeDrawable symbol = new ShapeDrawable(getShapeForId(context, card.mShape));
    symbol.getPaint().setShader(getShaderForPatternId(context, card.mPattern, card.mColor, overriddenPattern));
    symbol.getPaint().setStyle(Style.FILL);
    return symbol;
  }

  public static Shader getShaderForPatternId(Context context, int patternId, int colorId) {
    return getShaderForPatternId(context, patternId, colorId, null);
  }

  public static Shader getShaderForPatternId(
      Context context, int patternId, int colorId, String overriddenPattern) {
    int color = getColorForId(context, colorId);
    switch (patternId) {
      case 0: // Empty
        return new BitmapShader(
            Bitmap.createBitmap(new int[] {0}, 1, 1, Bitmap.Config.ARGB_8888),
            Shader.TileMode.REPEAT,
            Shader.TileMode.REPEAT);
      case 1: // Customizable Shaded
        return getCustomShadedShader(context, color, overriddenPattern);
      case 2: // Solid
        return new BitmapShader(
            Bitmap.createBitmap(new int[] {color}, 1, 1, Bitmap.Config.ARGB_8888),
            Shader.TileMode.REPEAT,
            Shader.TileMode.REPEAT);
      default:
        return null;
    }
  }

  public static Shader getCustomShadedShader(Context context, int color, String overriddenPattern) {
    String pattern = overriddenPattern;
    if (pattern == null) {
      SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
      pattern = sharedPref.getString(context.getString(R.string.pref_shaded_pattern), "stripes");
    }
    int thickness = STRIPE_WIDTH;
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
    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    String key;
    String defaultShape;
    switch (id) {
      case 0:
        key = context.getString(R.string.pref_shape_0);
        defaultShape = "square";
        break;
      case 1:
        key = context.getString(R.string.pref_shape_1);
        defaultShape = "circle";
        break;
      case 2:
        key = context.getString(R.string.pref_shape_2);
        defaultShape = "triangle";
        break;
      default:
        return new TriangleShape();
    }
    String shape = sharedPref.getString(key, defaultShape);
    if (shape == null || shape.isEmpty()) {
      shape = defaultShape;
    }
    if (shape.equals("square")) return new RectShape();
    if (shape.equals("circle")) return new OvalShape();
    if (shape.equals("triangle")) return new TriangleShape();
    if (shape.equals("diamond")) return new DiamondShape();
    if (shape.equals("hexagon")) return new HexagonShape();
    if (shape.equals("star")) return new StarShape();
    return new TriangleShape();
  }

  public static int getColorForId(Context context, int id) {
    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    String key;
    String defaultHex;
    switch (id) {
      case 0:
        key = context.getString(R.string.pref_color_0);
        defaultHex = "#33B5E5";
        break;
      case 1:
        key = context.getString(R.string.pref_color_1);
        defaultHex = "#FFBB33";
        break;
      case 2:
        key = context.getString(R.string.pref_color_2);
        defaultHex = "#FF4444";
        break;
      default:
        return 0;
    }
    String hex = sharedPref.getString(key, defaultHex);
    if (hex == null || hex.isEmpty()) {
      hex = defaultHex;
    }
    return Color.parseColor(hex);
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