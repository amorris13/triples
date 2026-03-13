package com.antsapps.triples;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import com.google.android.material.slider.Slider;

public class MaterialSliderPreference extends Preference {

  private float valueFrom = 0f;
  private float valueTo = 100f;
  private float stepSize = 1f;
  private int value;

  public MaterialSliderPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    setLayoutResource(R.layout.preference_material_slider);

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MaterialSliderPreference);
    valueFrom = a.getFloat(R.styleable.MaterialSliderPreference_valueFrom, 0f);
    valueTo = a.getFloat(R.styleable.MaterialSliderPreference_valueTo, 100f);
    stepSize = a.getFloat(R.styleable.MaterialSliderPreference_stepSize, 1f);
    a.recycle();
  }

  @Override
  public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
    super.onBindViewHolder(holder);
    Slider slider = (Slider) holder.findViewById(R.id.slider);
    slider.clearOnChangeListeners();
    slider.setValueFrom(valueFrom);
    slider.setValueTo(valueTo);
    slider.setStepSize(stepSize);
    slider.setValue(value);
    slider.setEnabled(isEnabled());
    slider.addOnChangeListener(
        (s, v, fromUser) -> {
          if (fromUser) {
            int newValue = (int) v;
            if (callChangeListener(newValue)) {
              setValue(newValue);
            }
          }
        });
  }

  @Override
  protected Object onGetDefaultValue(TypedArray a, int index) {
    return a.getInt(index, 0);
  }

  @Override
  protected void onSetInitialValue(Object defaultValue) {
    int initialValue;
    if (defaultValue instanceof Integer) {
      initialValue = (Integer) defaultValue;
    } else {
      initialValue = getPersistedInt(0);
    }
    setValue(roundToStep(initialValue));
  }

  public void setValue(int newValue) {
    int roundedValue = roundToStep(newValue);
    if (value != roundedValue) {
      value = roundedValue;
      persistInt(roundedValue);
      notifyChanged();
    }
  }

  public int getValue() {
    return value;
  }

  private int roundToStep(int val) {
    if (stepSize == 0) return val;
    float stepped = Math.round((val - valueFrom) / stepSize) * stepSize + valueFrom;
    return (int) Math.max(valueFrom, Math.min(valueTo, stepped));
  }
}
