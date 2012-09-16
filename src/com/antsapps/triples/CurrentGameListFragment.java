package com.antsapps.triples;

import java.util.List;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.OnStateChangedListener;
import com.google.common.collect.Lists;

public class CurrentGameListFragment extends GameListFragment implements
    OnStateChangedListener {
  public final static String TAG = "CurrentGameListFragment";

  protected static class CurrentGamesArrayAdapter extends ArrayAdapter<Game> {

    public CurrentGamesArrayAdapter(Context context, List<Game> games) {
      super(context, R.layout.game_list_item, games);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View v = convertView;
      if (v == null) {
        LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
            Context.LAYOUT_INFLATER_SERVICE);
        v = vi.inflate(R.layout.game_list_item, null);
      }

      Game g = getItem(position);
      if (g != null) {
        ((TextView) v.findViewById(R.id.time_elapsed)).setText(DateUtils
            .formatElapsedTime(TimeUnit.MILLISECONDS.toSeconds(g
                .getTimeElapsed())));
        ((TextView) v.findViewById(R.id.cards_remaining)).setText(String
            .valueOf(g.getCardsRemaining()));
        ((TextView) v.findViewById(R.id.when_started)).setText(DateUtils
            .getRelativeTimeSpanString(g.getDateStarted().getTime()));
      }

      return v;
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    View view = inflater.inflate(R.layout.game_list_fragment, null);

    ((TextView) view.findViewById(android.R.id.empty))
        .setText(R.string.no_current_games);
    return view;
  }

  @Override
  protected ArrayAdapter<Game> createArrayAdapter() {
    return new CurrentGamesArrayAdapter(getSherlockActivity(),
        Lists.newArrayList(mApplication.getCurrentGames()));
  }

  @Override
  protected void updateDataSet() {
    mAdapter.clear();
    for (Game game : mApplication.getCurrentGames()) {
      mAdapter.add(game);
    }
    mAdapter.notifyDataSetChanged();
  }
}