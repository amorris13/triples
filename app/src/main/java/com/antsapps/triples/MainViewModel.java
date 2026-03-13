package com.antsapps.triples;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.ArcadeGame;
import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.DailyGame;
import com.antsapps.triples.backend.Game;
import com.google.common.collect.Iterables;

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

    public ArcadeResumeState(boolean visible, int triplesFound) {
      this.visible = visible;
      this.triplesFound = triplesFound;
    }
  }

  private final MediatorLiveData<ClassicResumeState> mClassicResumeState = new MediatorLiveData<>();
  private final MediatorLiveData<ArcadeResumeState> mArcadeResumeState = new MediatorLiveData<>();
  private final MediatorLiveData<Boolean> mDailyCompleted = new MediatorLiveData<>();
  private final MediatorLiveData<Integer> mClassicCompletedCount = new MediatorLiveData<>();
  private final MediatorLiveData<Integer> mArcadeCompletedCount = new MediatorLiveData<>();
  private final MediatorLiveData<Integer> mDailyCompletedCount = new MediatorLiveData<>();

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
            mArcadeResumeState.setValue(new ArcadeResumeState(true, arcadeGame.getNumTriplesFound()));
          } else {
            mArcadeResumeState.setValue(new ArcadeResumeState(false, 0));
          }
        });

    mDailyCompleted.addSource(
        application.getDailyGamesLiveData(),
        games -> {
          DailyGame.Day today = DailyGame.Day.forToday();
          boolean dailyCompleted = false;
          for (DailyGame dg : games) {
            if (dg.getGameDay().equals(today) && dg.getGameState() == Game.GameState.COMPLETED) {
              dailyCompleted = true;
              break;
            }
          }
          mDailyCompleted.setValue(dailyCompleted);
        });

    mClassicCompletedCount.addSource(
        application.getClassicGamesLiveData(),
        games -> {
          int count = 0;
          for (ClassicGame g : games) {
            if (g.getGameState() == Game.GameState.COMPLETED) {
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
            if (g.getGameState() == Game.GameState.COMPLETED) {
              count++;
            }
          }
          mArcadeCompletedCount.setValue(count);
        });

    mDailyCompletedCount.addSource(
        application.getDailyGamesLiveData(),
        games -> {
          int count = 0;
          for (DailyGame g : games) {
            if (g.getGameState() == Game.GameState.COMPLETED) {
              count++;
            }
          }
          mDailyCompletedCount.setValue(count);
        });
  }

  public LiveData<ClassicResumeState> getClassicResumeState() {
    return mClassicResumeState;
  }

  public LiveData<ArcadeResumeState> getArcadeResumeState() {
    return mArcadeResumeState;
  }

  public LiveData<Boolean> getDailyCompleted() {
    return mDailyCompleted;
  }

  public LiveData<Integer> getClassicCompletedCount() {
    return mClassicCompletedCount;
  }

  public LiveData<Integer> getArcadeCompletedCount() {
    return mArcadeCompletedCount;
  }

  public LiveData<Integer> getDailyCompletedCount() {
    return mDailyCompletedCount;
  }
}
