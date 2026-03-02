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

  public ColorItemView(@NonNull Context context) {
    super(context);
    setMinimumHeight(100);
  }

  public void setColor(String hex) {
    setBackgroundColor(Color.parseColor(hex));
  }
}
