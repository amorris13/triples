package com.antsapps.triples.stats;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.antsapps.triples.R;
import com.antsapps.triples.backend.GameProperty;

/** Created by anthony on 1/12/13. */
public class ClassicStatisticsListHeaderView extends BaseStatisticsListHeaderView {
  public ClassicStatisticsListHeaderView(Context context) {
    super(context);
  }

  protected TextView initHeaders(View v) {
    TextView dateHeader = (TextView) v.findViewById(R.id.date_header);
    initHeader(dateHeader, GameProperty.DATE, RIGHT);

    TextView resultHeader = (TextView) v.findViewById(R.id.result_header);
    resultHeader.setText("TIME ELAPSED");
    initHeader(resultHeader, GameProperty.TIME_ELAPSED, LEFT);
    return resultHeader;
  }
}
