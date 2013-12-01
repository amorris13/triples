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

import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.Game;
import com.google.common.collect.Lists;

/**
 * Created by anthony on 1/12/13.
 */
public class ClassicCurrentGameListFragment extends BaseCurrentGameListFragment {
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
        ((TextView) v.findViewById(R.id.time)).setText(DateUtils
            .formatElapsedTime(TimeUnit.MILLISECONDS.toSeconds(g
                .getTimeElapsed())));
        ((TextView) v.findViewById(R.id.progress)).setText(String
            .valueOf(g.getCardsRemaining()));
        ((TextView) v.findViewById(R.id.when_started)).setText(DateUtils
            .getRelativeTimeSpanString(g.getDateStarted().getTime()));
      }

      return v;
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    View view = super.onCreateView(inflater, container, savedInstanceState);

    ((TextView) view.findViewById(R.id.timer_key_text)).setText("TIME ELAPSED");
    ((TextView) view.findViewById(R.id.progress_key_text)).setText("CARDS REMAINING");

    return view;
  }

  @Override
  protected ArrayAdapter<Game> createArrayAdapter() {
    return new CurrentGamesArrayAdapter(getSherlockActivity(),
        Lists.<Game>newArrayList(getCurrentGames()));
  }

  protected void deleteGame(Game game) {
    mApplication.deleteClassicGame((ClassicGame) game);
  }

  protected Class<? extends BaseGameActivity> getGameActivityClass(){
    return ClassicGameActivity.class;
  }

  protected Iterable<ClassicGame> getCurrentGames(){
    return mApplication.getCurrentClassicGames();
  }
}
