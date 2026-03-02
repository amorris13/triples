package com.antsapps.triples;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Color;
import android.graphics.Shader;
import androidx.preference.PreferenceManager;
import com.antsapps.triples.cardsview.DiamondShape;
import com.antsapps.triples.cardsview.HexagonShape;
import com.antsapps.triples.cardsview.StarShape;
import com.antsapps.triples.cardsview.TriangleShape;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.Shape;
import com.google.common.primitives.Ints;
import java.util.Arrays;

public class CardCustomizationUtils {

  private static final int STRIPE_WIDTH = 3;

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
    try {
      return Color.parseColor(hex);
    } catch (IllegalArgumentException e) {
      return Color.parseColor(defaultHex);
    }
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
    if (shape.equals("square")) return new RectShape();
    if (shape.equals("circle")) return new OvalShape();
    if (shape.equals("triangle")) return new TriangleShape();
    if (shape.equals("diamond")) return new DiamondShape();
    if (shape.equals("hexagon")) return new HexagonShape();
    if (shape.equals("star")) return new StarShape();
    return new TriangleShape();
  }

  public static Shader getCustomShadedShader(Context context, int color, String pattern) {
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
}
