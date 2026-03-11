package com.antsapps.triples;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Color;
import android.graphics.Shader;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.Shape;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import com.antsapps.triples.cardsview.DiamondShape;
import com.antsapps.triples.cardsview.HeartShape;
import com.antsapps.triples.cardsview.HexagonShape;
import com.antsapps.triples.cardsview.StarShape;
import com.antsapps.triples.cardsview.TriangleShape;
import com.google.common.primitives.Ints;
import java.util.Arrays;

public class CardCustomizationUtils {

  public static final int[] PRESET_COLOR_RES = {
    R.color.preset_color_0, R.color.preset_color_1, R.color.preset_color_2,
    R.color.preset_color_3, R.color.preset_color_4, R.color.preset_color_5,
    R.color.preset_color_6, R.color.preset_color_7, R.color.preset_color_8,
    R.color.preset_color_9, R.color.preset_color_10, R.color.preset_color_11
  };

  private static final float STRIPE_WIDTH = 1.5f;
  public static final int ICON_MARGIN_DP = 8;

  public static int getColorForId(Context context, int id) {
    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    String key;
    int defaultIndex;
    switch (id) {
      case 0:
        key = context.getString(R.string.pref_color_0);
        defaultIndex = 0;
        break;
      case 1:
        key = context.getString(R.string.pref_color_1);
        defaultIndex = 1;
        break;
      case 2:
        key = context.getString(R.string.pref_color_2);
        defaultIndex = 2;
        break;
      default:
        return 0;
    }
    String indexStr = sharedPref.getString(key, String.valueOf(defaultIndex));
    int index;
    try {
      index = Integer.parseInt(indexStr);
    } catch (NumberFormatException e) {
      index = defaultIndex;
    }
    if (index < 0 || index >= PRESET_COLOR_RES.length) {
      index = defaultIndex;
    }
    return ContextCompat.getColor(context, PRESET_COLOR_RES[index]);
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
    if (shape.equals("heart")) return new HeartShape();
    return new TriangleShape();
  }

  public static Shader getCustomShadedShader(Context context, int color, String pattern) {
    float density = context.getResources().getDisplayMetrics().density;
    int thickness = Math.max(1, Math.round(STRIPE_WIDTH * density));
    Bitmap bm;

    switch (pattern) {
      case "dots":
        // Make dots denser: 2x2 tile with 1x1 dot, starting with whitespace
        bm = Bitmap.createBitmap(thickness * 2, thickness * 2, Bitmap.Config.ARGB_8888);
        for (int i = 0; i < thickness; i++) {
          for (int j = 0; j < thickness; j++) {
            bm.setPixel(i + thickness, j + thickness, color);
          }
        }
        break;
      case "crosshatch":
        int size = thickness * 3;
        bm = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        for (int i = 0; i < size; i++) {
          for (int j = 0; j < size; j++) {
            if ((i + j) % size < thickness || (i - j + size) % size < thickness) {
              bm.setPixel(i, j, color);
            }
          }
        }
        break;
      case "lighter":
        int lighterColor = Color.argb(128, Color.red(color), Color.green(color), Color.blue(color));
        bm = Bitmap.createBitmap(new int[] {lighterColor}, 1, 1, Bitmap.Config.ARGB_8888);
        break;
      case "stripes":
      default:
        // Start with transparent/empty bit
        int[] pixels = Ints.concat(initIntArray(0, thickness), initIntArray(color, thickness));
        bm = Bitmap.createBitmap(pixels, pixels.length, 1, Bitmap.Config.ARGB_8888);
        break;
    }
    return new BitmapShader(bm, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
  }

  public static int[] initIntArray(int value, int length) {
    int[] arr = new int[length];
    Arrays.fill(arr, value);
    return arr;
  }
}
