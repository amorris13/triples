package com.antsapps.triples.stats;

import android.content.Context;
import android.widget.FrameLayout;

abstract class BaseStatisticsSummaryView extends FrameLayout implements
    OnStatisticsChangeListener {

  public BaseStatisticsSummaryView(Context context) {
    super(context);
  }
}
