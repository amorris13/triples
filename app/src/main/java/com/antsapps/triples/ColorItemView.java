package com.antsapps.triples;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class ColorItemView extends FrameLayout {

  private final TextView mTextView;

  public ColorItemView(@NonNull Context context) {
    super(context);
    mTextView = new TextView(context);
    mTextView.setGravity(android.view.Gravity.CENTER);
    addView(mTextView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    setMinimumHeight(100);
  }

  public void setColor(String hex) {
    if (hex.equals("Custom...")) {
      mTextView.setText("Custom...");
      setBackgroundColor(Color.TRANSPARENT);
    } else {
      mTextView.setText("");
      setBackgroundColor(Color.parseColor(hex));
    }
  }
}
