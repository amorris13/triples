package com.antsapps.triples;

import android.content.Context;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.antsapps.triples.backend.ArcadeGame;
import com.antsapps.triples.backend.Game;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ArcadeCurrentGameListFragment extends BaseCurrentGameListFragment {
  protected static class CurrentGamesArrayAdapter extends ArrayAdapter<Game> {

    public CurrentGamesArrayAdapter(Context context, List<Game> games) {
      super(context, R.layout.game_list_item, games);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View v = convertView;
      if (v == null) {
        LayoutInflater vi =
            (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = vi.inflate(R.layout.game_list_item, null);
      }

      ArcadeGame g = (ArcadeGame) getItem(position);
      if (g != null) {
        String timeStr =
            DateUtils.formatElapsedTime(
                TimeUnit.MILLISECONDS.toSeconds(ArcadeGame.TIME_LIMIT_MS - g.getTimeElapsed()));
        if (g.areHintsUsed()) {
          timeStr += " (hinted)";
        }
        ((TextView) v.findViewById(R.id.time)).setText(timeStr);
        ((TextView) v.findViewById(R.id.progress)).setText(String.valueOf(g.getNumTriplesFound()));
        ((TextView) v.findViewById(R.id.when_started))
            .setText(DateUtils.getRelativeTimeSpanString(g.getDateStarted().getTime()));
      }

      return v;
    }
  }

  @Override
  protected ArrayAdapter<Game> createArrayAdapter() {
    return new CurrentGamesArrayAdapter(getActivity(), Lists.<Game>newArrayList(getCurrentGames()));
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = super.onCreateView(inflater, container, savedInstanceState);

    ((TextView) view.findViewById(R.id.timer_key_text)).setText("TIME REMAINING");
    ((TextView) view.findViewById(R.id.progress_key_text)).setText("TRIPLES FOUND");

    return view;
  }

  protected void deleteGame(Game game) {
    mApplication.deleteArcadeGame((ArcadeGame) game);
  }

  protected Class<? extends BaseGameActivity> getGameActivityClass() {
    return ArcadeGameActivity.class;
  }

  protected Iterable<ArcadeGame> getCurrentGames() {
    return mApplication.getCurrentArcadeGames();
  }
}
