package com.antsapps.triples;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import androidx.preference.PreferenceManager;
import com.antsapps.triples.cardsview.DiamondShape;
import com.antsapps.triples.cardsview.HexagonShape;
import com.antsapps.triples.cardsview.StarShape;
import com.antsapps.triples.cardsview.TriangleShape;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.Shape;

public class CardCustomizationUtils {

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
}
