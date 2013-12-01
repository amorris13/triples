package com.antsapps.triples.stats;

import java.util.List;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.antsapps.triples.GamesServices;
import com.antsapps.triples.R;
import com.antsapps.triples.backend.ArcadeGame;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.Period;
import com.google.common.collect.Lists;

public class ArcadeStatisticsFragment extends BaseStatisticsFragment {

  protected static class StatisticsGamesArrayAdapter extends ArrayAdapter<Game> {

    public StatisticsGamesArrayAdapter(Context context, List<Game> games) {
      super(context, R.layout.stats_game_list_item, games);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View v = convertView;
      if (v == null) {
        LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
            Context.LAYOUT_INFLATER_SERVICE);
        v = vi.inflate(R.layout.stats_game_list_item, null);
      }

      ArcadeGame g = (ArcadeGame) getItem(position);
      if (g != null) {
        ((TextView) v.findViewById(R.id.result)).setText(String.valueOf(g.getNumTriplesFound()));
        ((TextView) v.findViewById(R.id.date_played)).setText(DateUtils
            .formatDateTime(getContext(), g.getDateStarted().getTime(), 0));
      }

      return v;
    }
  }

  @Override
  protected ArrayAdapter<Game> createArrayAdapter() {
    return new StatisticsGamesArrayAdapter(getSherlockActivity(),
        Lists.<Game>newArrayList());
  }

  @Override
  protected BaseStatisticsListHeaderView createStatisticsListHeaderView() {
    return new ArcadeStatisticsListHeaderView(getSherlockActivity());
  }

  protected String getLeaderboardId() {
    return GamesServices.Leaderboard.ARCADE;
  }

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
}
