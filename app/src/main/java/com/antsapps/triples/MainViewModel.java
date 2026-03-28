package com.antsapps.triples;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.ArcadeGame;
import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.DailyGame;
import com.antsapps.triples.backend.DailyStatisticsUtil;
import com.antsapps.triples.backend.Game;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.List;

public class MainViewModel extends ViewModel {

  public static class ClassicResumeState {
    public final boolean visible;
    public final int cardsRemaining;

    public ClassicResumeState(boolean visible, int cardsRemaining) {
      this.visible = visible;
      this.cardsRemaining = cardsRemaining;
    }
  }

  public static class ArcadeResumeState {
    public final boolean visible;
    public final int triplesFound;
    public final ArcadeGame.ArcadeStyle style;

    public ArcadeResumeState(boolean visible, int triplesFound, ArcadeGame.ArcadeStyle style) {
      this.visible = visible;
      this.triplesFound = triplesFound;
      this.style = style;
    }
  }

  public static class DailyState {
    public final boolean completed;
    public final int triplesFound;
    public final int totalTriples;
    public final int currentStreak;
    public final int totalSolved;

    public DailyState(
        boolean completed, int triplesFound, int totalTriples, int currentStreak, int totalSolved) {
      this.completed = completed;
      this.triplesFound = triplesFound;
      this.totalTriples = totalTriples;
      this.currentStreak = currentStreak;
      this.totalSolved = totalSolved;
    }
  }

  private final MediatorLiveData<ClassicResumeState> mClassicResumeState = new MediatorLiveData<>();
  private final MediatorLiveData<ArcadeResumeState> mArcadeResumeState = new MediatorLiveData<>();
  private final MediatorLiveData<DailyState> mDailyState = new MediatorLiveData<>();
  private final MediatorLiveData<Integer> mClassicCompletedCount = new MediatorLiveData<>();
  private final MediatorLiveData<Integer> mArcadeCompletedCount = new MediatorLiveData<>();

  private boolean mInitialized = false;

  public void init(Application application) {
    if (mInitialized) {
      return;
    }
    mInitialized = true;

    mClassicResumeState.addSource(
        application.getClassicGamesLiveData(),
        games -> {
          ClassicGame classicGame =
              Iterables.getFirst(
                  Iterables.filter(
                      games,
                      g ->
                          g.getGameState() == Game.GameState.ACTIVE
                              || g.getGameState() == Game.GameState.PAUSED),
                  null);
          if (classicGame != null && !classicGame.getTripleFindTimes().isEmpty()) {
            mClassicResumeState.setValue(
                new ClassicResumeState(true, classicGame.getCardsRemaining()));
          } else {
            mClassicResumeState.setValue(new ClassicResumeState(false, 0));
          }
        });

    mArcadeResumeState.addSource(
        application.getArcadeGamesLiveData(),
        games -> {
          ArcadeGame arcadeGame =
              Iterables.getFirst(
                  Iterables.filter(
                      games,
                      g ->
                          g.getGameState() == Game.GameState.ACTIVE
                              || g.getGameState() == Game.GameState.PAUSED),
                  null);
          if (arcadeGame != null && !arcadeGame.getTripleFindTimes().isEmpty()) {
            mArcadeResumeState.setValue(
                new ArcadeResumeState(true, arcadeGame.getNumTriplesFound(), arcadeGame.getStyle()));
          } else {
            mArcadeResumeState.setValue(new ArcadeResumeState(false, 0, null));
          }
        });

    mDailyState.addSource(
        application.getDailyGamesLiveData(),
        games -> {
          DailyGame.Day today = DailyGame.Day.forToday();
          DailyGame todayGame = null;
          List<DailyGame> completedGames = Lists.newArrayList();
          for (DailyGame dg : games) {
            if (dg.getGameDay().equals(today)) {
              todayGame = dg;
            }
            if (dg.getGameState() == Game.GameState.COMPLETED && !dg.areHintsUsed()) {
              completedGames.add(dg);
            }
          }
          DailyStatisticsUtil.DailyStatistics stats =
              DailyStatisticsUtil.computeDailyStatistics(completedGames);

          boolean completed =
              todayGame != null && todayGame.getGameState() == Game.GameState.COMPLETED;
          int triplesFound = todayGame != null ? todayGame.getNumTriplesFound() : 0;
          int totalTriples = todayGame != null ? todayGame.getTotalTriplesCount() : 0;

          mDailyState.setValue(
              new DailyState(
                  completed,
                  triplesFound,
                  totalTriples,
                  stats.currentStreak,
                  stats.totalGamesCompleted));
        });

    mClassicCompletedCount.addSource(
        application.getClassicGamesLiveData(),
        games -> {
          int count = 0;
          for (ClassicGame g : games) {
            if (g.getGameState() == Game.GameState.COMPLETED && !g.areHintsUsed()) {
              count++;
            }
          }
          mClassicCompletedCount.setValue(count);
        });

    mArcadeCompletedCount.addSource(
        application.getArcadeGamesLiveData(),
        games -> {
          int count = 0;
          for (ArcadeGame g : games) {
            if (g.getGameState() == Game.GameState.COMPLETED && !g.areHintsUsed()) {
              count++;
            }
          }
          mArcadeCompletedCount.setValue(count);
        });
  }

  public LiveData<ClassicResumeState> getClassicResumeState() {
    return mClassicResumeState;
  }

  public LiveData<ArcadeResumeState> getArcadeResumeState() {
    return mArcadeResumeState;
  }

  public LiveData<DailyState> getDailyState() {
    return mDailyState;
  }

  public LiveData<Integer> getClassicCompletedCount() {
    return mClassicCompletedCount;
  }

  public LiveData<Integer> getArcadeCompletedCount() {
    return mArcadeCompletedCount;
  }
}
