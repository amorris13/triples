package com.antsapps.triples.stats;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.ClassicStatistics;
import com.antsapps.triples.backend.Period;

public class ClassicStatisticsViewModel extends ViewModel {
  private final MediatorLiveData<ClassicStatistics> mStatistics = new MediatorLiveData<>();
  private final MutableLiveData<Period> mPeriod = new MutableLiveData<>(Period.ALL_TIME);
  private final MutableLiveData<Boolean> mIncludeHinted = new MutableLiveData<>(false);

  private boolean mInitialized = false;

  public void init(Application application) {
    if (mInitialized) {
      return;
    }
    mInitialized = true;

    mStatistics.addSource(application.getClassicGamesLiveData(), games -> update(application));
    mStatistics.addSource(mPeriod, period -> update(application));
    mStatistics.addSource(mIncludeHinted, includeHinted -> update(application));
  }

  private void update(Application application) {
    mStatistics.setValue(
        application.getClassicStatistics(mPeriod.getValue(), mIncludeHinted.getValue()));
  }

  public LiveData<ClassicStatistics> getStatistics() {
    return mStatistics;
  }

  public void setPeriod(Period period) {
    mPeriod.setValue(period);
  }

  public void setIncludeHinted(boolean includeHinted) {
    mIncludeHinted.setValue(includeHinted);
  }
}
