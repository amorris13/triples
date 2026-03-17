package com.antsapps.triples.stats;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.antsapps.triples.BaseTriplesActivity;
import com.antsapps.triples.R;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.OnStateChangedListener;
import com.antsapps.triples.backend.Statistics;
import com.antsapps.triples.util.CsvExportable;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.Comparator;

/** Created by anthony on 1/12/13. */
public abstract class BaseStatisticsFragment extends Fragment
    implements OnStatisticsChangeListener,
        OnComparatorChangeListener<Game>,
        StatisticsSelectorView.OnPeriodChangeListener,
        StatisticsSelectorView.OnIncludeHintedChangeListener,
        OnStateChangedListener,
        CsvExportable {
  protected Application mApplication;
  protected ArrayAdapter<Game> mAdapter;
  private BaseTriplesActivity mGameListActivity;
  protected StatisticsSelectorView mSelectorView;

  protected SummaryTabFragment mSummaryTabFragment;
  protected ListTabFragment mListTabFragment;

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    mGameListActivity = (BaseTriplesActivity) context;
    mApplication = Application.getInstance(context);
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mAdapter = createArrayAdapter();
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.stats_tabbed_fragment, container, false);

    initViewModel();

    StatisticsGamesServicesView gameServicesView = view.findViewById(R.id.game_services_view_container);
    gameServicesView.setLeaderboardId(getLeaderboardId());
    gameServicesView.setActivity(mGameListActivity);
    mGameListActivity.setSignInListener(gameServicesView);

    mSelectorView = view.findViewById(R.id.selector_view);
    mSelectorView.setOnPeriodChangeListener(this);
    mSelectorView.setOnIncludeHintedChangeListener(this);
    mSelectorView.setAccentColor(getAccentColor());

    mSummaryTabFragment = SummaryTabFragment.newInstance();
    mSummaryTabFragment.setSummaryView(createStatisticsSummaryView());

    mListTabFragment = ListTabFragment.newInstance();
    mListTabFragment.setHeaderView(createStatisticsListHeaderView());
    mListTabFragment.setAdapter(mAdapter);
    mListTabFragment.setOnComparatorChangeListener(this);

    ViewPager2 viewPager = view.findViewById(R.id.view_pager);
    TabLayout tabLayout = view.findViewById(R.id.tabs);

    viewPager.setAdapter(new StatsPagerAdapter(this));
    new TabLayoutMediator(
            tabLayout,
            viewPager,
            (tab, position) -> {
              tab.setText(position == 0 ? "Summary" : "Games");
            })
        .attach();

    return view;
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

  @Override
  public void onStateChanged() {
    updateDataSet();
  }

  protected abstract void updateDataSet();

  private class StatsPagerAdapter extends FragmentStateAdapter {
    public StatsPagerAdapter(@NonNull Fragment fragment) {
      super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
      if (position == 0) {
        return mSummaryTabFragment;
      } else {
        return mListTabFragment;
      }
    }

    @Override
    public int getItemCount() {
      return 2;
    }
  }

  protected abstract BaseStatisticsSummaryView createStatisticsSummaryView();

  protected abstract BaseStatisticsListHeaderView createStatisticsListHeaderView();

  protected abstract int getAccentColor();

  protected abstract String getGameType();

  protected abstract String getLeaderboardId();

  protected abstract void initViewModel();

  protected abstract ArrayAdapter<Game> createArrayAdapter();

  public abstract void exportToCsv();

  protected void shareCsv(String filename, String content) {
    com.antsapps.triples.util.ShareUtil.shareCsv(getActivity(), filename, content);
  }

  @Override
  public void onStatisticsChange(Statistics statistics) {
    if (mAdapter != null) {
      mAdapter.clear();
      for (Game game : statistics.getData()) {
        mAdapter.add(game);
      }
      mAdapter.notifyDataSetChanged();
    }
    if (mSummaryTabFragment != null) {
      mSummaryTabFragment.onStatisticsChange(statistics);
    }
  }

  @Override
  public void onComparatorChange(Comparator<Game> comparator) {
    if (mAdapter != null) {
      mAdapter.sort(comparator);
    }
  }
}
