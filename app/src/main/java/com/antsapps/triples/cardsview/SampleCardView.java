package com.antsapps.triples.cardsview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.shapes.Shape;
import android.util.AttributeSet;
import android.view.View;

import com.antsapps.triples.backend.Card;

import java.util.List;

public class SampleCardView extends View {

  private final CardBackgroundDrawable mBackground = new CardBackgroundDrawable();
  private SymbolDrawable mSymbol;
  private int mNumber;

  public SampleCardView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void setProperties(Context context, int number, int shapeId, int colorId, int patternId) {
    mNumber = number;
    Shape shape = SymbolDrawable.getShapeForId(context, shapeId);
    int color = SymbolDrawable.getColorForId(context, colorId);
    Shader shader = SymbolDrawable.getShaderForPatternId(context, patternId, colorId);
    mSymbol = new SymbolDrawable(shape, color, shader);
    invalidate();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = MeasureSpec.getSize(widthMeasureSpec);
    // Standard card aspect ratio is 2:3
    int height = width * 3 / 2;
    setMeasuredDimension(width, height);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (mSymbol == null) return;

    Rect bounds = new Rect(0, 0, getWidth(), getHeight());
    mBackground.setBounds(bounds);
    mBackground.draw(canvas);

    List<Rect> symbolBounds = CardDrawable.getBoundsForNumId(mNumber, bounds);
    for (Rect rect : symbolBounds) {
      mSymbol.setBounds(rect);
      mSymbol.draw(canvas);
    }
  }
}
