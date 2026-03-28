package com.antsapps.triples.stats;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.ArcadeGame;
import com.antsapps.triples.backend.ArcadeStatistics;
import com.antsapps.triples.backend.Period;

public class ArcadeStatisticsViewModel extends ViewModel {
  private final MediatorLiveData<ArcadeStatistics> mStatistics = new MediatorLiveData<>();
  private final MutableLiveData<Period> mPeriod = new MutableLiveData<>(Period.ALL_TIME);
  private final MutableLiveData<Boolean> mIncludeHinted = new MutableLiveData<>(false);
  private final MutableLiveData<ArcadeGame.ArcadeStyle> mStyle =
      new MutableLiveData<>(ArcadeGame.ArcadeStyle.FIXED);

  private boolean mInitialized = false;

  public void init(Application application) {
    if (mInitialized) {
      return;
    }
    mInitialized = true;

    Iterable<ArcadeGame> completedGames = application.getCompletedArcadeGames();
    if (!com.google.common.collect.Iterables.isEmpty(completedGames)) {
      ArcadeGame lastPlayed = com.google.common.collect.Iterables.getLast(completedGames);
      if (lastPlayed != null) {
        mStyle.setValue(lastPlayed.getStyle());
      }
    }

    mStatistics.addSource(application.getArcadeGamesLiveData(), games -> update(application));
    mStatistics.addSource(mPeriod, period -> update(application));
    mStatistics.addSource(mIncludeHinted, includeHinted -> update(application));
    mStatistics.addSource(mStyle, style -> update(application));
  }

  private void update(Application application) {
    mStatistics.setValue(
        application.getArcadeStatistics(
            mPeriod.getValue(), mIncludeHinted.getValue(), mStyle.getValue()));
  }

  public LiveData<ArcadeStatistics> getStatistics() {
    return mStatistics;
  }

  public void setPeriod(Period period) {
    mPeriod.setValue(period);
  }

  public void setIncludeHinted(boolean includeHinted) {
    mIncludeHinted.setValue(includeHinted);
  }

  public void setStyle(ArcadeGame.ArcadeStyle style) {
    mStyle.setValue(style);
  }

  public ArcadeGame.ArcadeStyle getStyle() {
    return mStyle.getValue();
  }
}
