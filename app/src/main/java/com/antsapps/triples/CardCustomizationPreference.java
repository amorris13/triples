package com.antsapps.triples;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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

  private static final String[] SHAPES = {
    "square", "circle", "triangle", "diamond", "hexagon", "star"
  };
  private static final String[] PATTERNS = {"stripes", "dots", "lighter", "crosshatch"};

  private Spinner[] colorSpinners = new Spinner[3];
  private Spinner[] shapeSpinners = new Spinner[3];
  private Spinner patternSpinner;
  private SampleCardView[] sampleCards = new SampleCardView[3];
  private View resetButton;

  private boolean updating = false;

  public static class ColorItemView extends View {
    private int mColor;
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public ColorItemView(@NonNull Context context) {
      super(context);
    }

    public void setColor(int color) {
      mColor = color;
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
    colorSpinners[0] = (Spinner) holder.findViewById(R.id.color_spinner_0);
    colorSpinners[1] = (Spinner) holder.findViewById(R.id.color_spinner_1);
    colorSpinners[2] = (Spinner) holder.findViewById(R.id.color_spinner_2);

    shapeSpinners[0] = (Spinner) holder.findViewById(R.id.shape_spinner_0);
    shapeSpinners[1] = (Spinner) holder.findViewById(R.id.shape_spinner_1);
    shapeSpinners[2] = (Spinner) holder.findViewById(R.id.shape_spinner_2);

    patternSpinner = (Spinner) holder.findViewById(R.id.pattern_spinner);

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

    List<Integer> colorIndices = new ArrayList<>();
    for (int i = 0; i < CardCustomizationUtils.PRESET_COLOR_RES.length; i++) {
      colorIndices.add(i);
    }

    for (int i = 0; i < 3; i++) {
      final int index = i;
      String currentIndexStr =
          prefs.getString(getContext().getString(getColorKey(i)), String.valueOf(i));
      int currentIndex = Integer.parseInt(currentIndexStr);

      final ColorAdapter colorAdapter = new ColorAdapter(getContext(), colorIndices);
      colorSpinners[i].setAdapter(colorAdapter);
      colorSpinners[i].setSelection(currentIndex);
      colorSpinners[i].setOnItemSelectedListener(
          new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
              if (!updating) {
                ensureUniqueColor(index, position);
                updateSampleCards();
              }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
          });

      ArrayAdapter<String> shapeAdapter = new ShapeAdapter(getContext(), Arrays.asList(SHAPES));
      shapeSpinners[i].setAdapter(shapeAdapter);
      String currentShape = prefs.getString(getContext().getString(getShapeKey(i)), SHAPES[i]);
      shapeSpinners[i].setSelection(Arrays.asList(SHAPES).indexOf(currentShape));
      shapeSpinners[i].setOnItemSelectedListener(
          new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
              if (!updating) {
                String selectedShape = SHAPES[position];
                ensureUniqueShape(index, selectedShape);
                updateSampleCards();
              }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
          });
    }

    ArrayAdapter<String> patternAdapter = new PatternAdapter(getContext(), Arrays.asList(PATTERNS));
    patternSpinner.setAdapter(patternAdapter);
    String currentPattern =
        prefs.getString(getContext().getString(R.string.pref_shaded_pattern), PATTERNS[0]);
    patternSpinner.setSelection(Arrays.asList(PATTERNS).indexOf(currentPattern));
    patternSpinner.setOnItemSelectedListener(
        new AdapterView.OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (!updating) {
              prefs
                  .edit()
                  .putString(
                      getContext().getString(R.string.pref_shaded_pattern), PATTERNS[position])
                  .apply();
              updateSampleCards();
            }
          }

          @Override
          public void onNothingSelected(AdapterView<?> parent) {}
        });

    updating = false;
  }

  private void resetToDefaults() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
    SharedPreferences.Editor editor = prefs.edit();
    for (int i = 0; i < 3; i++) {
      editor.putString(getContext().getString(getColorKey(i)), String.valueOf(i));
      editor.putString(getContext().getString(getShapeKey(i)), SHAPES[i]);
    }
    editor.putString(getContext().getString(R.string.pref_shaded_pattern), PATTERNS[0]);
    editor.apply();
    setupSpinners();
    updateSampleCards();
  }

  private void ensureUniqueColor(int index, int selectedIndex) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
    for (int i = 0; i < 3; i++) {
      if (i == index) continue;
      String otherIndexStr =
          prefs.getString(getContext().getString(getColorKey(i)), String.valueOf(i));
      int otherIndex = Integer.parseInt(otherIndexStr);
      if (otherIndex == selectedIndex) {
        String oldIndexStr =
            prefs.getString(getContext().getString(getColorKey(index)), String.valueOf(index));
        prefs.edit().putString(getContext().getString(getColorKey(i)), oldIndexStr).apply();
        updating = true;
        colorSpinners[i].setSelection(Integer.parseInt(oldIndexStr));
        updating = false;
      }
    }
    prefs
        .edit()
        .putString(getContext().getString(getColorKey(index)), String.valueOf(selectedIndex))
        .apply();
    updating = true;
    colorSpinners[index].setSelection(selectedIndex);
    updating = false;
  }

  private void updateSpinnerSelection(Spinner spinner, String value) {
    ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
    int position = adapter.getPosition(value);
    if (position != -1) {
      spinner.setSelection(position);
    }
  }

  private void ensureUniqueShape(int index, String selectedShape) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
    for (int i = 0; i < 3; i++) {
      if (i == index) continue;
      String otherShape = prefs.getString(getContext().getString(getShapeKey(i)), SHAPES[i]);
      if (otherShape.equals(selectedShape)) {
        String oldShape =
            prefs.getString(getContext().getString(getShapeKey(index)), SHAPES[index]);
        prefs.edit().putString(getContext().getString(getShapeKey(i)), oldShape).apply();
        updating = true;
        updateSpinnerSelection(shapeSpinners[i], oldShape);
        updating = false;
      }
    }
    prefs.edit().putString(getContext().getString(getShapeKey(index)), selectedShape).apply();
    updating = true;
    updateSpinnerSelection(shapeSpinners[index], selectedShape);
    updating = false;
  }

  private int getColorKey(int i) {
    switch (i) {
      case 0:
        return R.string.pref_color_0;
      case 1:
        return R.string.pref_color_1;
      case 2:
        return R.string.pref_color_2;
    }
    return 0;
  }

  private int getShapeKey(int i) {
    switch (i) {
      case 0:
        return R.string.pref_shape_0;
      case 1:
        return R.string.pref_shape_1;
      case 2:
        return R.string.pref_shape_2;
    }
    return 0;
  }

  private void updateSampleCards() {
    sampleCards[0].setCard(new Card(1, 0, 0, 0));
    sampleCards[1].setCard(new Card(1, 1, 1, 1));
    sampleCards[2].setCard(new Card(1, 2, 2, 2));
  }

  private class ColorAdapter extends ArrayAdapter<Integer> {
    public ColorAdapter(@NonNull Context context, @NonNull List<Integer> objects) {
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
      int colorIndex = getItem(position);
      ((ColorItemView) convertView)
          .setColor(
              ContextCompat.getColor(
                  getContext(), CardCustomizationUtils.PRESET_COLOR_RES[colorIndex]));
      return convertView;
    }

    @Override
    public View getDropDownView(
        int position, @Nullable View convertView, @NonNull ViewGroup parent) {
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
      siv.setColor(ContextCompat.getColor(getContext(), R.color.color_text_primary));
      return siv;
    }

    @Override
    public View getDropDownView(
        int position, @Nullable View convertView, @NonNull ViewGroup parent) {
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
    public View getDropDownView(
        int position, @Nullable View convertView, @NonNull ViewGroup parent) {
      return getView(position, convertView, parent);
    }
  }
}
