package com.antsapps.triples.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.antsapps.triples.R;
import com.antsapps.triples.backend.Statistics;

public class AnalysisTabFragment extends Fragment implements OnStatisticsChangeListener {

  private Statistics mCurrentStatistics;

  public static AnalysisTabFragment newInstance() {
    return new AnalysisTabFragment();
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.stats_analysis_tab, container, false);

    if (getParentFragment() instanceof BaseStatisticsFragment parent) {
      if (mCurrentStatistics != null) {
        updateAnalysis(view, mCurrentStatistics);
      } else if (parent.mLatestStatistics != null) {
        updateAnalysis(view, parent.mLatestStatistics);
      }
    }

    View root = view.findViewById(R.id.analysis_tab_root);
    ViewCompat.setOnApplyWindowInsetsListener(
        root,
        (v, insets) -> {
          int bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
          v.setPadding(0, 0, 0, bottom);
          return insets;
        });

    return view;
  }

  @Override
  public void onStatisticsChange(Statistics statistics) {
    mCurrentStatistics = statistics;
    View view = getView();
    if (view != null) {
      updateAnalysis(view, statistics);
    }
  }

  private void updateAnalysis(View view, Statistics statistics) {
    TripleAnalysisSummaryView summaryView = view.findViewById(R.id.analysis_summary);
    if (summaryView != null) {
      summaryView.setAnalysis(statistics.getAnalysis());
    }

    TextView warningView = view.findViewById(R.id.analysis_warning);
    if (warningView != null) {
      int numGames = statistics.getNumGames();
      int numWithAnalysis = statistics.getNumGamesWithAnalysis();
      if (numGames > 0 && numWithAnalysis < numGames) {
        warningView.setVisibility(View.VISIBLE);
        warningView.setText(
            getString(R.string.analysis_partial_data_warning, numWithAnalysis, numGames));
      } else {
        warningView.setVisibility(View.GONE);
      }
    }
  }
}
