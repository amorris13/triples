package com.antsapps.triples;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.DailyGame;
import com.antsapps.triples.backend.DailyStatisticsUtil;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;

public class DailyStatisticsViewModel extends ViewModel {
  private final MediatorLiveData<List<DailyGame>> mDailyGames = new MediatorLiveData<>();
  private final MediatorLiveData<DailyStatisticsUtil.DailyStatistics> mDailyStatistics =
      new MediatorLiveData<>();

  private boolean mInitialized = false;

  public void init(Application application) {
    if (mInitialized) {
      return;
    }
    mInitialized = true;

    mDailyGames.addSource(
        application.getDailyGamesLiveData(),
        games -> {
          List<DailyGame> dailyGames = Lists.newArrayList(games);
          Collections.sort(dailyGames, (g1, g2) -> g2.getGameDay().compareTo(g1.getGameDay()));
          mDailyGames.setValue(dailyGames);

          List<DailyGame> completedGames = Lists.newArrayList();
          for (DailyGame game : games) {
            if (game.getGameState() == DailyGame.GameState.COMPLETED) {
              completedGames.add(game);
            }
          }
          mDailyStatistics.setValue(DailyStatisticsUtil.computeDailyStatistics(completedGames));
        });
  }

  public LiveData<List<DailyGame>> getDailyGames() {
    return mDailyGames;
  }

  public LiveData<DailyStatisticsUtil.DailyStatistics> getDailyStatistics() {
    return mDailyStatistics;
  }
}
