package com.antsapps.triples.stats;

import java.util.Comparator;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.antsapps.triples.BaseGameListActivity;
import com.antsapps.triples.BaseGameListFragment;
import com.antsapps.triples.R;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.GameProperty;
import com.antsapps.triples.backend.Statistics;

/**
 * Created by anthony on 1/12/13.
 */
public abstract class BaseStatisticsFragment extends BaseGameListFragment
    implements OnStatisticsChangeListener, OnComparatorChangeListener<Game>,
    StatisticsSelectorView.OnPeriodChangeListener {
  private BaseGameListActivity mGameListActivity;
  private Comparator<Game> mComparator = GameProperty.TIME_ELAPSED
      .createReversableComparator();
  private StatisticsGamesServicesView mGameServicesView;
  protected StatisticsSelectorView mSelectorView;
  private BaseStatisticsSummaryView mSummaryView;
  private BaseStatisticsListHeaderView mListHeaderView;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    mGameListActivity = (BaseGameListActivity) activity;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    ListView listView = (ListView) inflater.inflate(
        R.layout.stats_fragment,
        null);

    mGameServicesView = new StatisticsGamesServicesView(getActivity(), getLeaderboardId());
    mGameServicesView.setGameHelper(mGameListActivity.getGameHelper());
    mGameListActivity.setGameHelperListener(mGameServicesView);
    listView.addHeaderView(mGameServicesView, null, false);

    mSelectorView = new StatisticsSelectorView(getActivity());
    mSelectorView.setOnPeriodChangeListener(this);
    listView.addHeaderView(mSelectorView, null, false);

    mSummaryView = createStatisticsSummaryView();
    listView.addHeaderView(mSummaryView, null, false);

    mListHeaderView = createStatisticsListHeaderView();
    mListHeaderView.setOnComparatorChangeListener(this);
    listView.addHeaderView(mListHeaderView, null, false);

    return listView;
  }

  protected abstract String getLeaderboardId();

  protected abstract BaseStatisticsListHeaderView createStatisticsListHeaderView();

  protected abstract BaseStatisticsSummaryView createStatisticsSummaryView();

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
