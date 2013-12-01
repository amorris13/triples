package com.antsapps.triples;

import com.antsapps.triples.backend.ArcadeGame;
import com.antsapps.triples.backend.Game;

/**
 * Created by anthony on 1/12/13.
 */
public class ArcadeCurrentGameListFragment extends BaseCurrentGameListFragment {
  protected void deleteGame(Game game) {
    mApplication.deleteArcadeGame((ArcadeGame) game);
  }

  protected Class<? extends BaseGameActivity> getGameActivityClass(){
    return ArcadeGameActivity.class;
  }

  protected Iterable<ArcadeGame> getCurrentGames(){
    return mApplication.getCurrentArcadeGames();
  }
}
