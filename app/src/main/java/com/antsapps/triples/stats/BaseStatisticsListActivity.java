package com.antsapps.triples.stats;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import com.antsapps.triples.BaseStatisticsActivity;
import com.antsapps.triples.R;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.GameProperty;
import com.antsapps.triples.backend.OnStateChangedListener;
import com.antsapps.triples.backend.Statistics;
import com.antsapps.triples.util.ShareUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.Comparator;

public abstract class BaseStatisticsListActivity extends BaseStatisticsActivity
    implements OnStatisticsChangeListener,
        OnComparatorChangeListener<Game>,
        StatisticsSelectorView.OnPeriodChangeListener,
        OnStateChangedListener {

  protected Application mApplication;
  protected ArrayAdapter<Game> mAdapter;
  private Comparator<Game> mComparator = GameProperty.TIME_ELAPSED.createReversableComparator();
  private StatisticsGamesServicesView mGameServicesView;
  protected StatisticsSelectorView mSelectorView;
  private BaseStatisticsSummaryView mSummaryView;
  private BaseStatisticsListHeaderView mListHeaderView;
  protected ListView mListView;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_statistics_list);

    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    mApplication = Application.getInstance(this);

    mListView = findViewById(android.R.id.list);

    mGameServicesView = new StatisticsGamesServicesView(this, getLeaderboardId());
    mGameServicesView.setActivity(this);
    setSignInListener(mGameServicesView);
    mListView.addHeaderView(mGameServicesView, null, false);

    mSelectorView = new StatisticsSelectorView(this);
    mSelectorView.setOnPeriodChangeListener(this);
    mListView.addHeaderView(mSelectorView, null, false);

    int accentColor = getAccentColor();
    mSelectorView.setAccentColor(accentColor);

    mSummaryView = createStatisticsSummaryView();
    mSummaryView.setAccentColor(accentColor);
    mListView.addHeaderView(mSummaryView, null, false);

    mListHeaderView = createStatisticsListHeaderView();
    mListHeaderView.setAccentColor(accentColor);
    mListHeaderView.setOnComparatorChangeListener(this);
    mListView.addHeaderView(mListHeaderView, null, false);

    mAdapter = createArrayAdapter();
    mListView.setAdapter(mAdapter);

    final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

    mListView.setOnItemLongClickListener(
        (parent, view, position, id) -> {
          Object item = parent.getItemAtPosition(position);
          if (item instanceof Game) {
            vibrator.vibrate(50);
            createDeleteAlertDialog((Game) item).show();
            return true;
          }
          return false;
        });
  }

  @Override
  public void onStart() {
    super.onStart();
    mApplication.addOnStateChangedListener(this);
    updateDataSet();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mApplication.removeOnStateChangedListener(this);
  }

  protected abstract int getAccentColor();

  protected abstract String getLeaderboardId();

  protected abstract BaseStatisticsListHeaderView createStatisticsListHeaderView();

  protected abstract BaseStatisticsSummaryView createStatisticsSummaryView();

  protected abstract ArrayAdapter<Game> createArrayAdapter();

  protected abstract void updateDataSet();

  protected void shareCsv(String filename, String content) {
    ShareUtil.shareCsv(this, filename, content);
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

  @Override
  public void onStateChanged() {
    updateDataSet();
  }

  private AlertDialog createDeleteAlertDialog(final Game game) {
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
    builder.setCancelable(true);
    builder.setTitle(R.string.delete);
    builder.setPositiveButton(
        R.string.yes,
        (dialog, which) -> {
          deleteGame(game);
          dialog.dismiss();
        });
    builder.setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss());
    AlertDialog alert = builder.create();
    return alert;
  }

  protected abstract void deleteGame(Game game);
}
