package com.antsapps.triples.stats;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

  interface OnArcadeStyleChangeListener {
    void onArcadeStyleChange(com.antsapps.triples.backend.ArcadeGame.ArcadeStyle style);
  }

  private static final long MS_PER_DAY = TimeUnit.DAYS.toMillis(1);

  private final Map<String, Period> mPeriods = Maps.newLinkedHashMap();
  private ChipGroup mPeriodChipGroup;
  private ChipGroup mHintsChipGroup;
  private ChipGroup mStyleChipGroup;
  private ChipGroup mSummaryChipGroup;
  private Period mCurrentPeriod = Period.ALL_TIME;
  private boolean mIncludeHinted = false;
  private com.antsapps.triples.backend.ArcadeGame.ArcadeStyle mStyle =
      com.antsapps.triples.backend.ArcadeGame.ArcadeStyle.FIXED;

  private OnPeriodChangeListener mOnPeriodChangeListener;
  private OnIncludeHintedChangeListener mOnIncludeHintedChangeListener;
  private OnArcadeStyleChangeListener mOnArcadeStyleChangeListener;

  private LinearLayout mOptionsContainer;
  private ImageView mExpandIcon;

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
    inflater.inflate(R.layout.stats_selector, this);

    mOptionsContainer = findViewById(R.id.filter_options_container);
    mSummaryChipGroup = findViewById(R.id.filter_summary_chips);
    mExpandIcon = findViewById(R.id.filter_expand_icon);

    findViewById(R.id.filter_header)
        .setOnClickListener(
            v -> {
              boolean isExpanded = mOptionsContainer.getVisibility() == View.VISIBLE;
              mOptionsContainer.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
              mSummaryChipGroup.setVisibility(isExpanded ? View.VISIBLE : View.INVISIBLE);
              mExpandIcon.setImageResource(
                  isExpanded ? R.drawable.ic_expand_more : R.drawable.ic_expand_less);
            });

    initChips();
    updateSummary();
  }

  private void initChips() {
    mPeriodChipGroup = findViewById(R.id.period_chip_group);
    initPeriodsMap();
    LayoutInflater inflater = LayoutInflater.from(getContext());
    for (String key : mPeriods.keySet()) {
      Chip chip = (Chip) inflater.inflate(R.layout.stats_period_chip, mPeriodChipGroup, false);
      chip.setText(key);
      chip.setId(View.generateViewId());
      chip.setTag(mPeriods.get(key));
      mPeriodChipGroup.addView(chip);
      if (mPeriods.get(key) == Period.ALL_TIME) {
        mPeriodChipGroup.check(chip.getId());
      }
    }

    mPeriodChipGroup.setOnCheckedStateChangeListener(
        (group, checkedIds) -> {
          if (!checkedIds.isEmpty()) {
            Chip chip = group.findViewById(checkedIds.get(0));
            mCurrentPeriod = (Period) chip.getTag();
            updateSummary();
            if (mOnPeriodChangeListener != null) {
              mOnPeriodChangeListener.onPeriodChange(mCurrentPeriod);
            }
          }
        });

    mHintsChipGroup = findViewById(R.id.hints_chip_group);
    mHintsChipGroup.check(R.id.chip_no_hints);
    mHintsChipGroup.setOnCheckedStateChangeListener(
        (group, checkedIds) -> {
          mIncludeHinted = checkedIds.contains(R.id.chip_incl_hints);
          updateSummary();
          if (mOnIncludeHintedChangeListener != null) {
            mOnIncludeHintedChangeListener.onIncludeHintedChange(mIncludeHinted);
          }
        });

    mStyleChipGroup = findViewById(R.id.style_chip_group);
    mStyleChipGroup.check(R.id.chip_style_fixed);
    mStyleChipGroup.setOnCheckedStateChangeListener(
        (group, checkedIds) -> {
          if (checkedIds.contains(R.id.chip_style_bonus)) {
            mStyle = com.antsapps.triples.backend.ArcadeGame.ArcadeStyle.BONUS;
          } else {
            mStyle = com.antsapps.triples.backend.ArcadeGame.ArcadeStyle.FIXED;
          }
          updateSummary();
          if (mOnArcadeStyleChangeListener != null) {
            mOnArcadeStyleChangeListener.onArcadeStyleChange(mStyle);
          }
        });
  }

  private void initPeriodsMap() {
    mPeriods.put(getContext().getString(R.string.all_time), Period.ALL_TIME);
    mPeriods.put(
        getContext().getString(R.string.past_day), DatePeriod.fromTimePeriod(1 * MS_PER_DAY));
    mPeriods.put(
        getContext().getString(R.string.past_week), DatePeriod.fromTimePeriod(7 * MS_PER_DAY));
    mPeriods.put(
        getContext().getString(R.string.past_month), DatePeriod.fromTimePeriod(30 * MS_PER_DAY));
    mPeriods.put(
        getContext().getString(R.string.past_3_months), DatePeriod.fromTimePeriod(91 * MS_PER_DAY));
    mPeriods.put(
        getContext().getString(R.string.past_6_months),
        DatePeriod.fromTimePeriod(182 * MS_PER_DAY));
    mPeriods.put(
        getContext().getString(R.string.past_year), DatePeriod.fromTimePeriod(365 * MS_PER_DAY));
    mPeriods.put(getContext().getString(R.string.past_10_games), new NumGamesPeriod(10));
    mPeriods.put(getContext().getString(R.string.past_50_games), new NumGamesPeriod(50));
  }

  private void updateSummary() {
    mSummaryChipGroup.removeAllViews();
    LayoutInflater inflater = LayoutInflater.from(getContext());

    // Period Chip
    String periodText = getContext().getString(R.string.all_time);
    for (Map.Entry<String, Period> entry : mPeriods.entrySet()) {
      if (entry.getValue().equals(mCurrentPeriod)) {
        periodText = entry.getKey();
        break;
      }
    }
    addSummaryChip(periodText);

    // Hints Chip
    addSummaryChip(mIncludeHinted ? "Incl. Hints" : "No Hints");

    // Style Chip
    if (mStyleChipGroup.getVisibility() == View.VISIBLE) {
      addSummaryChip(
          mStyle == com.antsapps.triples.backend.ArcadeGame.ArcadeStyle.BONUS ? "Bonus" : "Fixed");
    }
  }

  private void addSummaryChip(String text) {
    Chip chip =
        (Chip)
            LayoutInflater.from(getContext())
                .inflate(R.layout.stats_period_chip, mSummaryChipGroup, false);
    chip.setText(text);
    chip.setCheckable(false);
    chip.setClickable(false);
    chip.setChipIconVisible(false);
    chip.setCheckedIconVisible(false);
    chip.setSelected(true);
    mSummaryChipGroup.addView(chip);
  }

  public void setOnPeriodChangeListener(OnPeriodChangeListener listener) {
    mOnPeriodChangeListener = listener;
  }

  public void setOnIncludeHintedChangeListener(OnIncludeHintedChangeListener listener) {
    mOnIncludeHintedChangeListener = listener;
  }

  public void setOnArcadeStyleChangeListener(OnArcadeStyleChangeListener listener) {
    mOnArcadeStyleChangeListener = listener;
  }

  public void showArcadeStyleSelector(com.antsapps.triples.backend.ArcadeGame.ArcadeStyle style) {
    findViewById(R.id.style_label).setVisibility(View.VISIBLE);
    mStyleChipGroup.setVisibility(View.VISIBLE);
    mStyle = style;
    mStyleChipGroup.check(
        style == com.antsapps.triples.backend.ArcadeGame.ArcadeStyle.BONUS
            ? R.id.chip_style_bonus
            : R.id.chip_style_fixed);
    updateSummary();
  }

  protected void setAccentColor(int accentColor) {
    ((TextView) findViewById(R.id.selector_title)).setTextColor(accentColor);
  }

  public Period getPeriod() {
    return mCurrentPeriod;
  }
}
