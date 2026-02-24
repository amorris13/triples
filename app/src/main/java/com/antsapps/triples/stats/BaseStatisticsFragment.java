package com.antsapps.triples.stats;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.ListFragment;

import com.antsapps.triples.BaseTriplesActivity;
import com.antsapps.triples.R;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.GameProperty;
import com.antsapps.triples.backend.OnStateChangedListener;
import com.antsapps.triples.backend.Statistics;

import java.util.Comparator;

/** Created by anthony on 1/12/13. */
public abstract class BaseStatisticsFragment extends ListFragment
    implements OnStateChangedListener,
        OnStatisticsChangeListener,
        OnComparatorChangeListener<Game>,
        StatisticsSelectorView.OnPeriodChangeListener {
  private BaseTriplesActivity mGameListActivity;
  private Comparator<Game> mComparator = GameProperty.TIME_ELAPSED.createReversableComparator();
  private StatisticsGamesServicesView mGameServicesView;
  protected StatisticsSelectorView mSelectorView;
  private BaseStatisticsSummaryView mSummaryView;
  private BaseStatisticsListHeaderView mListHeaderView;
  protected Application mApplication;
  protected ArrayAdapter<Game> mAdapter;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    mGameListActivity = (BaseTriplesActivity) activity;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mApplication = Application.getInstance(getActivity());

    mAdapter = createArrayAdapter();
    setListAdapter(mAdapter);
  }

  @Override
  public void onStart() {
    super.onStart();
    mApplication.addOnStateChangedListener(this);
    updateDataSet();
  }

  @Override
  public void onStop() {
    super.onStop();
    mApplication.removeOnStateChangedListener(this);
  }

  protected abstract ArrayAdapter<Game> createArrayAdapter();

  @Override
  public void onStateChanged() {
    updateDataSet();
  }

  protected abstract void updateDataSet();

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
