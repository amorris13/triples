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

  private float mValueFrom = 0f;
  private float mValueTo = 100f;
  private float mStepSize = 0f;
  private float mValue = 0f;

  public MaterialSliderPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    setLayoutResource(R.layout.preference_material_slider);

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MaterialSliderPreference);
    mValueFrom = a.getFloat(R.styleable.MaterialSliderPreference_valueFrom, 0f);
    mValueTo = a.getFloat(R.styleable.MaterialSliderPreference_valueTo, 100f);
    mStepSize = a.getFloat(R.styleable.MaterialSliderPreference_stepSize, 0f);
    a.recycle();
  }

  @Override
  public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
    super.onBindViewHolder(holder);
    Slider slider = (Slider) holder.findViewById(R.id.slider);
    slider.setEnabled(isEnabled());
    slider.setValueFrom(mValueFrom);
    slider.setValueTo(mValueTo);
    if (mStepSize > 0) {
      slider.setStepSize(mStepSize);
    }
    slider.setValue(mValue);

    slider.clearOnChangeListeners();
    slider.addOnChangeListener(
        (s, value, fromUser) -> {
          if (fromUser) {
            if (callChangeListener((int) value)) {
              setValue((int) value);
            } else {
              s.setValue(mValue);
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
    if (defaultValue == null) {
      mValue = roundToStep(getPersistedInt((int) mValue));
    } else {
      mValue = roundToStep((int) defaultValue);
      persistInt((int) mValue);
    }
  }

  public void setValue(int value) {
    float roundedValue = roundToStep(value);
    if (mValue != roundedValue) {
      mValue = roundedValue;
      persistInt((int) roundedValue);
      notifyChanged();
    }
  }

  private float roundToStep(float value) {
    if (mStepSize <= 0) {
      return Math.max(mValueFrom, Math.min(mValueTo, value));
    }
    int steps = Math.round((value - mValueFrom) / mStepSize);
    float rounded = mValueFrom + steps * mStepSize;
    return Math.max(mValueFrom, Math.min(mValueTo, rounded));
  }

  public int getValue() {
    return (int) mValue;
  }
}
