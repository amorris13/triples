package com.antsapps.triples.cardsview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.shapes.Shape;
import android.util.AttributeSet;
import android.view.View;

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
    Shader shader = SymbolDrawable.getShaderForPatternId(context, patternId, color);
    mSymbol = new SymbolDrawable(shape, color, shader);
    invalidate();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = MeasureSpec.getSize(widthMeasureSpec);
    // Landscape card aspect ratio is 3:2
    int height = width * 2 / 3;
    setMeasuredDimension(width, height);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (mSymbol == null) return;

    Rect bounds = new Rect(0, 0, getWidth(), getHeight());
    // We draw to a bitmap at 2x size and scale down to match CardDrawable's behavior
    // and ensure correct line/pattern thickness.
    Bitmap bitmap = Bitmap.createBitmap(bounds.width() * 2, bounds.height() * 2, Bitmap.Config.ARGB_8888);
    Canvas tmpCanvas = new Canvas(bitmap);
    Rect tmpBounds = new Rect(0, 0, bounds.width() * 2, bounds.height() * 2);

    mBackground.setBounds(tmpBounds);
    mBackground.draw(tmpCanvas);

    List<Rect> symbolBounds = CardDrawable.getBoundsForNumId(mNumber, tmpBounds);
    for (Rect rect : symbolBounds) {
      mSymbol.setBounds(rect);
      mSymbol.draw(tmpCanvas);
    }

    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bounds.width(), bounds.height(), true);
    canvas.drawBitmap(scaledBitmap, 0, 0, null);
  }
}
