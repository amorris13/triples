package com.antsapps.triples.cardsview;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.PathShape;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.Shape;

import com.antsapps.triples.backend.Card;

class SymbolDrawable extends Drawable {

  private final Card mCard;

  private final ShapeDrawable mOutline;
  private final ShapeDrawable mFill;

  SymbolDrawable(Card card) {
    mCard = card;
    mOutline = getOutlineForCard(card);
    mFill = getFillForCard(card);
  }

  private static ShapeDrawable getOutlineForCard(Card card) {
    ShapeDrawable symbol = new ShapeDrawable(getShapeForId(card.mShape));
    symbol.getPaint().setColor(getColorForId(card.mColor));
    symbol.getPaint().setStyle(Style.STROKE);
    symbol.getPaint().setStrokeWidth(5);
    return symbol;
  }

  private static ShapeDrawable getFillForCard(Card card) {
    ShapeDrawable symbol = new ShapeDrawable(getShapeForId(card.mShape));
    symbol.getPaint().setShader(getShaderForPatternId(card.mPattern, card.mColor));
    symbol.getPaint().setStyle(Style.FILL);
    return symbol;
  }

  private static Shader getShaderForPatternId(int patternId, int colorId) {
    int color = getColorForId(colorId);
    int[] pixels;
    Bitmap bm;
    switch (patternId) {
      case 0: // Empty
        pixels = new int[] {0, 0, 0, 0};
        break;
      case 1: // Stripes
        pixels = new int[] {color, color, 0, 0};
        break;
      case 2: // Solid
        pixels = new int[] {color, color, color, color};
        break;
      default:
        return null;
    }
    bm = Bitmap.createBitmap(pixels, 4, 1, Bitmap.Config.ARGB_8888);
    return new BitmapShader(bm, Shader.TileMode.MIRROR, Shader.TileMode.REPEAT);
  }

  private static Shape getShapeForId(int id) {
    switch (id) {
      case 0: // Square
        return new RectShape();
      case 1: // Circle
        return new OvalShape();
      case 2: // Triangle
      default:
        // This is a hack :(
        // TODO: Fix this to work with any screen density.
        int size = 50;
        Path path = new Path();
        path.moveTo(0, size);
        path.lineTo(size / 2, 0);
        path.lineTo(size, size);
        path.close();
        return new PathShape(path, size, size);
    }
  }

  private static int getColorForId(int id) {
    switch (id) {
      case 0:
        return Color.parseColor("#33B5E5"); // Holo Light Blue
      case 1:
        return Color.parseColor("#FFBB33"); // Holo Light Orange
      case 2:
        return Color.parseColor("#FF4444"); // Holo Light Red
    }
    return 0;
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
