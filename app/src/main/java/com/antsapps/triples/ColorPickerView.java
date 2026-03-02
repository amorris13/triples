package com.antsapps.triples;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ColorPickerView extends View {

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int[] mColors;
    private OnColorSelectedListener mListener;

    public interface OnColorSelectedListener {
        void onColorSelected(int color);
    }

    public ColorPickerView(Context context) {
        super(context);
        init();
    }

    public ColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mColors = new int[] {
                0xFF33B5E5, 0xFFFFBB33, 0xFFFF4444, 0xFF99CC00, 0xFFAA66CC, 0xFF0099CC,
                0xFFFF8800, 0xFFCC0000, 0xFF669900, 0xFF9933CC, 0xFF000000, 0xFF888888,
                0xFFFFFFFF, 0xFFE5E5E5, 0xFFCCCCCC, 0xFFBBBBBB, 0xFFAAAAAA, 0xFF666666,
                0xFF333333, 0xFF222222, 0xFF111111, 0xFF000000
        };
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        mListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        int columns = 6;
        int rows = (mColors.length + columns - 1) / columns;
        int cellWidth = width / columns;
        int cellHeight = height / rows;

        for (int i = 0; i < mColors.length; i++) {
            int row = i / columns;
            int col = i % columns;
            mPaint.setColor(mColors[i]);
            canvas.drawRect(col * cellWidth, row * cellHeight, (col + 1) * cellWidth, (row + 1) * cellHeight, mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(android.view.MotionEvent event) {
        if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
            int width = getWidth();
            int height = getHeight();
            int columns = 6;
            int rows = (mColors.length + columns - 1) / columns;
            int cellWidth = width / columns;
            int cellHeight = height / rows;

            int col = (int) (event.getX() / cellWidth);
            int row = (int) (event.getY() / cellHeight);
            int index = row * columns + col;
            if (index >= 0 && index < mColors.length) {
                if (mListener != null) {
                    mListener.onColorSelected(mColors[index]);
                }
            }
            return true;
        }
        return super.onTouchEvent(event);
    }
}
