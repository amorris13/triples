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

  private BaseStatisticsSummaryView mSummaryView;
  private Statistics mCurrentStatistics;

  public static SummaryTabFragment newInstance() {
    return new SummaryTabFragment();
  }

  public void setSummaryView(BaseStatisticsSummaryView summaryView) {
    mSummaryView = summaryView;
    if (mCurrentStatistics != null && mSummaryView != null) {
      mSummaryView.onStatisticsChange(mCurrentStatistics);
    }
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.stats_summary_tab, container, false);
    LinearLayout summaryContainer = view.findViewById(R.id.summary_container);
    if (mSummaryView != null) {
      if (mSummaryView.getParent() != null) {
        ((ViewGroup) mSummaryView.getParent()).removeView(mSummaryView);
      }
      summaryContainer.addView(mSummaryView);
    }
    return view;
  }

  @Override
  public void onStatisticsChange(Statistics statistics) {
    mCurrentStatistics = statistics;
    if (mSummaryView != null) {
      mSummaryView.onStatisticsChange(statistics);
    }
  }
}
