package com.antsapps.triples;

import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.Game;

/**
 * Created by anthony on 1/12/13.
 */
public class ClassicCurrentGameListFragment extends BaseCurrentGameListFragment {
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
