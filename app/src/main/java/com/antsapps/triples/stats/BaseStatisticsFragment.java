package com.antsapps.triples.stats;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import androidx.core.content.ContextCompat;
import com.antsapps.triples.BaseGameListFragment;
import com.antsapps.triples.BaseTriplesActivity;
import com.antsapps.triples.R;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.GameProperty;
import com.antsapps.triples.backend.Statistics;
import com.antsapps.triples.util.CsvExportable;
import com.antsapps.triples.util.ShareUtil;
import java.util.Comparator;

/** Created by anthony on 1/12/13. */
public abstract class BaseStatisticsFragment extends BaseGameListFragment
    implements OnStatisticsChangeListener,
        OnComparatorChangeListener<Game>,
        StatisticsSelectorView.OnPeriodChangeListener,
        CsvExportable {
  private BaseTriplesActivity mGameListActivity;
  private Comparator<Game> mComparator = GameProperty.TIME_ELAPSED.createReversableComparator();
  private StatisticsGamesServicesView mGameServicesView;
  protected StatisticsSelectorView mSelectorView;
  private BaseStatisticsSummaryView mSummaryView;
  private BaseStatisticsListHeaderView mListHeaderView;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    mGameListActivity = (BaseTriplesActivity) activity;
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    ListView listView = (ListView) inflater.inflate(R.layout.stats_fragment, null);

    mGameServicesView = new StatisticsGamesServicesView(getActivity(), getLeaderboardId());
    mGameServicesView.setActivity(mGameListActivity);
    mGameListActivity.setSignInListener(mGameServicesView);
    listView.addHeaderView(mGameServicesView, null, false);

    mSelectorView = new StatisticsSelectorView(getActivity());
    mSelectorView.setOnPeriodChangeListener(this);
    listView.addHeaderView(mSelectorView, null, false);

    int accentColor = getAccentColor();
    mSelectorView.setAccentColor(accentColor);

    mSummaryView = createStatisticsSummaryView();
    mSummaryView.setAccentColor(accentColor);
    listView.addHeaderView(mSummaryView, null, false);

    mListHeaderView = createStatisticsListHeaderView();
    mListHeaderView.setAccentColor(accentColor);
    mListHeaderView.setOnComparatorChangeListener(this);
    listView.addHeaderView(mListHeaderView, null, false);

    return listView;
  }

  protected int getAccentColor() {
    return ContextCompat.getColor(
        getActivity(),
        getGameType().equals("Arcade") ? R.color.arcade_accent : R.color.classic_accent);
  }

  protected abstract String getGameType();

  protected abstract String getLeaderboardId();

  protected abstract BaseStatisticsListHeaderView createStatisticsListHeaderView();

  protected abstract BaseStatisticsSummaryView createStatisticsSummaryView();

  public abstract void exportToCsv();

  protected void shareCsv(String filename, String content) {
    ShareUtil.shareCsv(getActivity(), filename, content);
  }

  @Override
  public void onStatisticsChange(Statistics statistics) {
    mSummaryView.onStatisticsChange(statistics);

    mAdapter.clear();
    for (Game game : statistics.getData()) {
      mAdapter.add(game);
    }
    mAdapter.notifyDataSetChanged();
    mAdapter.sort(mComparator);
  }

  @Override
  public void onComparatorChange(Comparator<Game> comparator) {
    mComparator = comparator;
    if (mAdapter != null) {
      mAdapter.sort(mComparator);
    }
  }
}
