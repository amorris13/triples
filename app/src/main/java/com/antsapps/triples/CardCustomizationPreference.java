package com.antsapps.triples;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.google.android.material.textfield.TextInputLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceViewHolder;

import com.antsapps.triples.backend.Card;
import com.antsapps.triples.cardsview.PatternIconView;
import com.antsapps.triples.cardsview.SampleCardView;
import com.antsapps.triples.cardsview.ShapeIconView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CardCustomizationPreference extends Preference {

  private static final String[] SHAPES = {"square", "circle", "triangle", "diamond", "hexagon", "star"};
  private static final String[] PRESET_COLORS = {
    "#2196F3", "#FF9800", "#F44336", "#4CAF50", "#9C27B0", "#00BCD4",
    "#E91E63", "#FF5722", "#FFC107", "#3F51B5", "#009688", "#000000"
  };
  private static final String[] PATTERNS = {"stripes", "dots", "lighter", "crosshatch"};

  private AutoCompleteTextView[] colorSpinners = new AutoCompleteTextView[3];
  private TextInputLayout[] colorLayouts = new TextInputLayout[3];
  private AutoCompleteTextView[] shapeSpinners = new AutoCompleteTextView[3];
  private TextInputLayout[] shapeLayouts = new TextInputLayout[3];
  private AutoCompleteTextView patternSpinner;
  private TextInputLayout patternLayout;
  private SampleCardView[] sampleCards = new SampleCardView[3];
  private View resetButton;

  private boolean updating = false;

  public static class ViewDrawable extends Drawable {
    private final View mView;

    public ViewDrawable(View view) {
      mView = view;
      int size = view.getContext().getResources().getDimensionPixelSize(android.R.dimen.app_icon_size);
      setBounds(0, 0, size, size);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
      mView.measure(
          View.MeasureSpec.makeMeasureSpec(getBounds().width(), View.MeasureSpec.EXACTLY),
          View.MeasureSpec.makeMeasureSpec(getBounds().height(), View.MeasureSpec.EXACTLY));
      mView.layout(0, 0, getBounds().width(), getBounds().height());
      mView.draw(canvas);
    }

    @Override
    public void setAlpha(int alpha) {}

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {}

    @Override
    public int getOpacity() {
      return PixelFormat.TRANSLUCENT;
    }

    @Override
    public int getIntrinsicWidth() {
      return getBounds().width();
    }

    @Override
    public int getIntrinsicHeight() {
      return getBounds().height();
    }
  }

  public static class ColorItemView extends View {
    private int mColor;
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public ColorItemView(@NonNull Context context) {
      super(context);
    }

    public void setColor(String hex) {
      mColor = Color.parseColor(hex);
      invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
      super.onDraw(canvas);
      mPaint.setColor(mColor);
      mPaint.setStyle(Paint.Style.FILL);
      float density = getResources().getDisplayMetrics().density;
      int margin = (int) (CardCustomizationUtils.ICON_MARGIN_DP * density);
      canvas.drawRect(margin, margin, getWidth() - margin, getHeight() - margin, mPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
      int width = MeasureSpec.getSize(widthMeasureSpec);
      if (width == 0) width = 100;
      setMeasuredDimension(width, width);
    }
  }

  public CardCustomizationPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    setLayoutResource(R.layout.preference_card_customization);
    setSelectable(false);
  }

  @Override
  public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
    super.onBindViewHolder(holder);
    colorSpinners[0] = (AutoCompleteTextView) holder.findViewById(R.id.color_spinner_0);
    colorSpinners[1] = (AutoCompleteTextView) holder.findViewById(R.id.color_spinner_1);
    colorSpinners[2] = (AutoCompleteTextView) holder.findViewById(R.id.color_spinner_2);
    colorLayouts[0] = (TextInputLayout) holder.findViewById(R.id.color_layout_0);
    colorLayouts[1] = (TextInputLayout) holder.findViewById(R.id.color_layout_1);
    colorLayouts[2] = (TextInputLayout) holder.findViewById(R.id.color_layout_2);

    shapeSpinners[0] = (AutoCompleteTextView) holder.findViewById(R.id.shape_spinner_0);
    shapeSpinners[1] = (AutoCompleteTextView) holder.findViewById(R.id.shape_spinner_1);
    shapeSpinners[2] = (AutoCompleteTextView) holder.findViewById(R.id.shape_spinner_2);
    shapeLayouts[0] = (TextInputLayout) holder.findViewById(R.id.shape_layout_0);
    shapeLayouts[1] = (TextInputLayout) holder.findViewById(R.id.shape_layout_1);
    shapeLayouts[2] = (TextInputLayout) holder.findViewById(R.id.shape_layout_2);

    patternSpinner = (AutoCompleteTextView) holder.findViewById(R.id.pattern_spinner);
    patternLayout = (TextInputLayout) holder.findViewById(R.id.pattern_layout);

    sampleCards[0] = (SampleCardView) holder.findViewById(R.id.sample_card_0);
    sampleCards[1] = (SampleCardView) holder.findViewById(R.id.sample_card_1);
    sampleCards[2] = (SampleCardView) holder.findViewById(R.id.sample_card_2);

    resetButton = holder.findViewById(R.id.reset_button);
    resetButton.setOnClickListener(v -> resetToDefaults());

    setupSpinners();
    updateSampleCards();
  }

  private void setupSpinners() {
    updating = true;
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

    for (int i = 0; i < 3; i++) {
      final int index = i;
      String currentColor = prefs.getString(getContext().getString(getColorKey(i)), PRESET_COLORS[i]);
      List<String> colors = new ArrayList<>(Arrays.asList(PRESET_COLORS));

      final ColorAdapter colorAdapter = new ColorAdapter(getContext(), colors);
      colorSpinners[i].setAdapter(colorAdapter);
      colorSpinners[i].setText(currentColor, false);
      updateColorIcon(i, currentColor);
      colorSpinners[i].setOnItemClickListener((parent, view, position, id) -> {
        if (!updating) {
          String selectedColor = (String) parent.getItemAtPosition(position);
          ensureUniqueColor(index, selectedColor);
          updateSampleCards();
        }
      });

      ArrayAdapter<String> shapeAdapter = new ShapeAdapter(getContext(), Arrays.asList(SHAPES));
      shapeSpinners[i].setAdapter(shapeAdapter);
      String currentShape = prefs.getString(getContext().getString(getShapeKey(i)), SHAPES[i]);
      shapeSpinners[i].setText(currentShape, false);
      updateShapeIcon(i, currentShape);
      shapeSpinners[i].setOnItemClickListener((parent, view, position, id) -> {
        if (!updating) {
          String selectedShape = SHAPES[position];
          ensureUniqueShape(index, selectedShape);
          updateSampleCards();
        }
      });
    }

    ArrayAdapter<String> patternAdapter = new PatternAdapter(getContext(), Arrays.asList(PATTERNS));
    patternSpinner.setAdapter(patternAdapter);
    String currentPattern = prefs.getString(getContext().getString(R.string.pref_shaded_pattern), PATTERNS[0]);
    patternSpinner.setText(currentPattern, false);
    updatePatternIcon(currentPattern);
    patternSpinner.setOnItemClickListener((parent, view, position, id) -> {
      if (!updating) {
        String selectedPattern = (String) parent.getItemAtPosition(position);
        prefs.edit().putString(getContext().getString(R.string.pref_shaded_pattern), selectedPattern).apply();
        updatePatternIcon(selectedPattern);
        updateSampleCards();
      }
    });

    updating = false;
  }

  private void updateColorIcon(int index, String color) {
    ColorItemView icon = new ColorItemView(getContext());
    int size = getContext().getResources().getDimensionPixelSize(android.R.dimen.app_icon_size);
    icon.setLayoutParams(new ViewGroup.LayoutParams(size, size));
    icon.setColor(color);
    colorLayouts[index].setStartIconDrawable(new ViewDrawable(icon));
  }

  private void updateShapeIcon(int index, String shape) {
    ShapeIconView icon = new ShapeIconView(getContext());
    int size = getContext().getResources().getDimensionPixelSize(android.R.dimen.app_icon_size);
    icon.setLayoutParams(new ViewGroup.LayoutParams(size, size));
    icon.setShape(shape);
    icon.setColor(Color.BLACK);
    shapeLayouts[index].setStartIconDrawable(new ViewDrawable(icon));
  }

  private void updatePatternIcon(String pattern) {
    PatternIconView icon = new PatternIconView(getContext());
    int size = getContext().getResources().getDimensionPixelSize(android.R.dimen.app_icon_size);
    icon.setLayoutParams(new ViewGroup.LayoutParams(size, size));
    icon.setPattern(pattern);
    patternLayout.setStartIconDrawable(new ViewDrawable(icon));
  }

  private void resetToDefaults() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
    SharedPreferences.Editor editor = prefs.edit();
    for (int i = 0; i < 3; i++) {
      editor.putString(getContext().getString(getColorKey(i)), PRESET_COLORS[i]);
      editor.putString(getContext().getString(getShapeKey(i)), SHAPES[i]);
    }
    editor.putString(getContext().getString(R.string.pref_shaded_pattern), PATTERNS[0]);
    editor.apply();
    setupSpinners();
    updateSampleCards();
  }

  private void ensureUniqueColor(int index, String selectedColor) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
    for (int i = 0; i < 3; i++) {
      if (i == index) continue;
      String otherColor = prefs.getString(getContext().getString(getColorKey(i)), PRESET_COLORS[i]);
      if (otherColor.equalsIgnoreCase(selectedColor)) {
        String oldColor = prefs.getString(getContext().getString(getColorKey(index)), PRESET_COLORS[index]);
        prefs.edit().putString(getContext().getString(getColorKey(i)), oldColor).apply();
        updating = true;
        colorSpinners[i].setText(oldColor, false);
        updateColorIcon(i, oldColor);
        updating = false;
      }
    }
    prefs.edit().putString(getContext().getString(getColorKey(index)), selectedColor).apply();
    updating = true;
    colorSpinners[index].setText(selectedColor, false);
    updateColorIcon(index, selectedColor);
    updating = false;
  }

  private void ensureUniqueShape(int index, String selectedShape) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
    for (int i = 0; i < 3; i++) {
      if (i == index) continue;
      String otherShape = prefs.getString(getContext().getString(getShapeKey(i)), SHAPES[i]);
      if (otherShape.equals(selectedShape)) {
        String oldShape = prefs.getString(getContext().getString(getShapeKey(index)), SHAPES[index]);
        prefs.edit().putString(getContext().getString(getShapeKey(i)), oldShape).apply();
        updating = true;
        shapeSpinners[i].setText(oldShape, false);
        updateShapeIcon(i, oldShape);
        updating = false;
      }
    }
    prefs.edit().putString(getContext().getString(getShapeKey(index)), selectedShape).apply();
    updating = true;
    shapeSpinners[index].setText(selectedShape, false);
    updateShapeIcon(index, selectedShape);
    updating = false;
  }

  private int getColorKey(int i) {
    switch (i) {
      case 0: return R.string.pref_color_0;
      case 1: return R.string.pref_color_1;
      case 2: return R.string.pref_color_2;
    }
    return 0;
  }

  private int getShapeKey(int i) {
    switch (i) {
      case 0: return R.string.pref_shape_0;
      case 1: return R.string.pref_shape_1;
      case 2: return R.string.pref_shape_2;
    }
    return 0;
  }

  private void updateSampleCards() {
    sampleCards[0].setCard(new Card(1, 0, 0, 0));
    sampleCards[1].setCard(new Card(1, 1, 1, 1));
    sampleCards[2].setCard(new Card(1, 2, 2, 2));
  }

  private class ColorAdapter extends ArrayAdapter<String> {
    public ColorAdapter(@NonNull Context context, @NonNull List<String> objects) {
      super(context, 0, objects);
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
      if (convertView == null) {
        convertView = new ColorItemView(getContext());
        int size = getContext().getResources().getDimensionPixelSize(android.R.dimen.app_icon_size);
        convertView.setLayoutParams(new ViewGroup.LayoutParams(size, size));
      }
      ((ColorItemView) convertView).setColor(getItem(position));
      return convertView;
    }
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
      return getView(position, convertView, parent);
    }
  }

  private class ShapeAdapter extends ArrayAdapter<String> {
    public ShapeAdapter(@NonNull Context context, @NonNull List<String> objects) {
      super(context, 0, objects);
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
      if (convertView == null) {
          convertView = new ShapeIconView(getContext());
          int size = getContext().getResources().getDimensionPixelSize(android.R.dimen.app_icon_size);
          convertView.setLayoutParams(new ViewGroup.LayoutParams(size, size));
      }
      ShapeIconView siv = (ShapeIconView) convertView;
      siv.setShape(getItem(position));
      siv.setColor(Color.BLACK);
      return siv;
    }
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
      return getView(position, convertView, parent);
    }
  }

  private class PatternAdapter extends ArrayAdapter<String> {
    public PatternAdapter(@NonNull Context context, @NonNull List<String> objects) {
      super(context, 0, objects);
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
      if (convertView == null) {
          convertView = new PatternIconView(getContext());
          int size = getContext().getResources().getDimensionPixelSize(android.R.dimen.app_icon_size);
          convertView.setLayoutParams(new ViewGroup.LayoutParams(size, size));
      }
      PatternIconView piv = (PatternIconView) convertView;
      piv.setPattern(getItem(position));
      return piv;
    }
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
      return getView(position, convertView, parent);
    }
  }
}
