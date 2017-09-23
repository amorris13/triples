package com.antsapps.triples;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.OnStateChangedListener;

public abstract class BaseCurrentGameListFragment extends BaseGameListFragment
    implements OnStateChangedListener {

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    getListView()
        .setOnItemClickListener(
            new AdapterView.OnItemClickListener() {
              @Override
              public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Game game = (Game) parent.getItemAtPosition(position);
                if (game != null) {
                  Intent intent = new Intent(view.getContext(), getGameActivityClass());
                  intent.putExtra(Game.ID_TAG, game.getId());
                  startActivity(intent);
                }
              }
            });
  }

  protected abstract Class<? extends BaseGameActivity> getGameActivityClass();

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.game_list_fragment, null);
    return view;
  }

  protected abstract Iterable<? extends Game> getCurrentGames();

  @Override
  protected void updateDataSet() {
    mAdapter.clear();
    for (Game game : getCurrentGames()) {
      mAdapter.add(game);
    }
    mAdapter.notifyDataSetChanged();
  }
}
