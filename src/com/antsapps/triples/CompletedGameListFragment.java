package com.antsapps.triples;

import java.util.List;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.OnStateChangedListener;
import com.google.common.collect.Lists;

public class CompletedGameListFragment extends GameListFragment implements
    OnStateChangedListener {
  public final static String TAG = "CompletedGameListFragment";

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
        ((TextView) v.findViewById(R.id.time_elapsed)).setText(g
            .getTimeElapsed() + " elapsed");
        ((TextView) v.findViewById(R.id.cards_remaining)).setText(g
            .getCardsRemaining() + " cards left");
        ((TextView) v.findViewById(R.id.when_started))
            .setText("Started: "
                + DateUtils.getRelativeTimeSpanString(g
                    .getDateStarted().getTime()));
      }

      return v;

    }
  }

  @Override
  protected String getEmptyText() {
    return getSherlockActivity().getString(R.string.no_completed_games);
  }

  @Override
  protected ArrayAdapter<Game> createArrayAdapter() {
    return new CurrentGamesArrayAdapter(getSherlockActivity(),
        Lists.newArrayList(mApplication.getCompletedGames()));
  }

  @Override
  protected void updateDataSet() {
    mAdapter.clear();
    for(Game game : mApplication.getCompletedGames()) {
      mAdapter.add(game);
    }
    mAdapter.notifyDataSetChanged();
  }
}