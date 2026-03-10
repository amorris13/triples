package com.antsapps.triples.stats;

import android.content.Context;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.antsapps.triples.GamesServices;
import com.antsapps.triples.R;
import com.antsapps.triples.backend.ArcadeGame;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.Period;
import com.antsapps.triples.util.CsvUtil;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;

public class ArcadeStatisticsActivity extends BaseStatisticsListActivity {

  protected static class StatisticsGamesArrayAdapter extends ArrayAdapter<Game> {

    public StatisticsGamesArrayAdapter(Context context, List<Game> games) {
      super(context, R.layout.stats_game_list_item, games);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View v = convertView;
      if (v == null) {
        LayoutInflater vi =
            (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = vi.inflate(R.layout.stats_game_list_item, null);
      }

      ArcadeGame g = (ArcadeGame) getItem(position);
      if (g != null) {
        String result = String.valueOf(g.getNumTriplesFound());
        if (g.areHintsUsed()) {
          result += " (hinted)";
        }
        ((TextView) v.findViewById(R.id.result)).setText(result);
        ((TextView) v.findViewById(R.id.date_played))
            .setText(DateUtils.formatDateTime(getContext(), g.getDateStarted().getTime(), 0));
      }

      return v;
    }
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setTitle(R.string.arcade_label);
  }

  @Override
  protected ArrayAdapter<Game> createArrayAdapter() {
    return new StatisticsGamesArrayAdapter(this, Lists.<Game>newArrayList());
  }

  @Override
  protected BaseStatisticsListHeaderView createStatisticsListHeaderView() {
    return new ArcadeStatisticsListHeaderView(this);
  }

  @Override
  protected BaseStatisticsSummaryView createStatisticsSummaryView() {
    return new ArcadeStatisticsSummaryView(this);
  }

  @Override
  protected String getGameType() {
    return "Arcade";
  }

  @Override
  protected String getLeaderboardId() {
    return GamesServices.Leaderboard.ARCADE;
  }

  @Override
  protected void deleteGame(Game game) {
    mApplication.deleteArcadeGame((ArcadeGame) game);
  }

  @Override
  public void onPeriodChange(Period period) {
    onStatisticsChange(mApplication.getArcadeStatistics(period));
  }

  @Override
  protected void updateDataSet() {
    onStatisticsChange(mApplication.getArcadeStatistics(mSelectorView.getPeriod()));
  }

  @Override
  public void exportToCsv() {
    List<Game> games = new ArrayList<>();
    for (int i = 0; i < mAdapter.getCount(); i++) {
      games.add(mAdapter.getItem(i));
    }
    shareCsv("arcade_statistics.csv", CsvUtil.getArcadeCsvContent(games));
  }
}
