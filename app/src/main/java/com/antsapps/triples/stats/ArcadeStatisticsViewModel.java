package com.antsapps.triples.stats;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.ArcadeStatistics;
import com.antsapps.triples.backend.Period;

public class ArcadeStatisticsViewModel extends ViewModel {
  private final MediatorLiveData<ArcadeStatistics> mStatistics = new MediatorLiveData<>();
  private final MutableLiveData<Period> mPeriod = new MutableLiveData<>(Period.ALL_TIME);
  private final MutableLiveData<Boolean> mIncludeHinted = new MutableLiveData<>(false);

  private boolean mInitialized = false;

  public void init(Application application) {
    if (mInitialized) {
      return;
    }
    mInitialized = true;

    mStatistics.addSource(application.getArcadeGamesLiveData(), games -> update(application));
    mStatistics.addSource(mPeriod, period -> update(application));
    mStatistics.addSource(mIncludeHinted, includeHinted -> update(application));
  }

  private void update(Application application) {
    mStatistics.setValue(
        application.getArcadeStatistics(mPeriod.getValue(), mIncludeHinted.getValue()));
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
}
