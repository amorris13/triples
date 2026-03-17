package com.antsapps.triples.stats;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.antsapps.triples.R;
import com.antsapps.triples.backend.DatePeriod;
import com.antsapps.triples.backend.NumGamesPeriod;
import com.antsapps.triples.backend.Period;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.concurrent.TimeUnit;

class StatisticsSelectorView extends FrameLayout {

  interface OnPeriodChangeListener {
    void onPeriodChange(Period period);
  }

  interface OnIncludeHintedChangeListener {
    void onIncludeHintedChange(boolean includeHinted);
  }

  private static final long MS_PER_DAY = TimeUnit.DAYS.toMillis(1);

  private static final Map<String, Period> PERIODS = Maps.newLinkedHashMap();
  private ChipGroup mChipGroup;
  private Period mCurrentPeriod;

  private OnPeriodChangeListener mOnPeriodChangeListener;
  private OnIncludeHintedChangeListener mOnIncludeHintedChangeListener;

  public StatisticsSelectorView(Context context) {
    this(context, null);
  }

  public StatisticsSelectorView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public StatisticsSelectorView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);

    LayoutInflater inflater =
        (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View v = inflater.inflate(R.layout.stats_selector, this);

    initChips();
    initCheckbox();
  }

  private void initCheckbox() {
    com.google.android.material.checkbox.MaterialCheckBox includeHintedCheckbox =
        findViewById(R.id.include_hinted_checkbox);
    includeHintedCheckbox.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          if (mOnIncludeHintedChangeListener != null) {
            mOnIncludeHintedChangeListener.onIncludeHintedChange(isChecked);
          }
        });
  }

  private void initChips() {
    mChipGroup = findViewById(R.id.period_chip_group);
    initPeriodsMap();
    LayoutInflater inflater = LayoutInflater.from(getContext());
    for (String key : PERIODS.keySet()) {
      Chip chip = (Chip) inflater.inflate(R.layout.stats_period_chip, mChipGroup, false);
      chip.setText(key);
      chip.setId(View.generateViewId());
      chip.setTag(PERIODS.get(key));
      mChipGroup.addView(chip);
      if (PERIODS.get(key) == Period.ALL_TIME) {
        mChipGroup.check(chip.getId());
        mCurrentPeriod = Period.ALL_TIME;
      }
    }

    mChipGroup.setOnCheckedStateChangeListener(
        (group, checkedIds) -> {
          if (!checkedIds.isEmpty()) {
            Chip chip = group.findViewById(checkedIds.get(0));
            mCurrentPeriod = (Period) chip.getTag();
            if (mOnPeriodChangeListener != null) {
              mOnPeriodChangeListener.onPeriodChange(mCurrentPeriod);
            }
          }
        });
  }

  private void initPeriodsMap() {
    PERIODS.put(getContext().getString(R.string.all_time), Period.ALL_TIME);
    PERIODS.put(
        getContext().getString(R.string.past_day), DatePeriod.fromTimePeriod(1 * MS_PER_DAY));
    PERIODS.put(
        getContext().getString(R.string.past_week), DatePeriod.fromTimePeriod(7 * MS_PER_DAY));
    PERIODS.put(
        getContext().getString(R.string.past_month), DatePeriod.fromTimePeriod(30 * MS_PER_DAY));
    PERIODS.put(
        getContext().getString(R.string.past_3_months), DatePeriod.fromTimePeriod(91 * MS_PER_DAY));
    PERIODS.put(
        getContext().getString(R.string.past_6_months),
        DatePeriod.fromTimePeriod(182 * MS_PER_DAY));
    PERIODS.put(
        getContext().getString(R.string.past_year), DatePeriod.fromTimePeriod(365 * MS_PER_DAY));
    PERIODS.put(getContext().getString(R.string.past_10_games), new NumGamesPeriod(10));
    PERIODS.put(getContext().getString(R.string.past_50_games), new NumGamesPeriod(50));
  }

  public void setOnPeriodChangeListener(OnPeriodChangeListener listener) {
    mOnPeriodChangeListener = listener;
  }

  public void setOnIncludeHintedChangeListener(OnIncludeHintedChangeListener listener) {
    mOnIncludeHintedChangeListener = listener;
  }

  protected void setAccentColor(int accentColor) {
    ((TextView) findViewById(R.id.selector_title)).setTextColor(accentColor);
  }

  public Period getPeriod() {
    return mCurrentPeriod;
  }
}
