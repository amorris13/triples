package com.antsapps.triples.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.antsapps.triples.R;
import com.antsapps.triples.backend.Statistics;

public class SummaryTabFragment extends Fragment implements OnStatisticsChangeListener {

  private Statistics mCurrentStatistics;

  public static SummaryTabFragment newInstance() {
    return new SummaryTabFragment();
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.stats_summary_tab, container, false);
    LinearLayout summaryContainer = view.findViewById(R.id.summary_container);

    if (getParentFragment() instanceof BaseStatisticsFragment parent) {
      BaseStatisticsSummaryView summaryView = parent.createStatisticsSummaryView();
      summaryView.setAccentColor(parent.getAccentColor());
      summaryContainer.addView(summaryView);
      if (mCurrentStatistics != null) {
        summaryView.onStatisticsChange(mCurrentStatistics);
      }
    }

    return view;
  }

  @Override
  public void onStatisticsChange(Statistics statistics) {
    mCurrentStatistics = statistics;
    View view = getView();
    if (view != null) {
      LinearLayout summaryContainer = view.findViewById(R.id.summary_container);
      if (summaryContainer != null && summaryContainer.getChildCount() > 0) {
        View child = summaryContainer.getChildAt(0);
        if (child instanceof BaseStatisticsSummaryView summaryView) {
          summaryView.onStatisticsChange(statistics);
        }
      }
    }
  }
}
