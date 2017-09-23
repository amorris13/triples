package com.antsapps.triples.backend;

import android.os.Handler;
import android.os.Message;

import com.google.common.collect.Sets;

import java.util.Set;

class Timer {

  private static final int TICK_WHAT = 1;

  private final Set<OnTimerTickListener> mListeners = Sets.newHashSet();

  private long mTimeElapsedWhenLastResumed;

  private long mTimeOfLastResume;

  private final Handler mHandler;

  public void addOnTimerTickListener(OnTimerTickListener listener) {
    mListeners.add(listener);
  }

  public void removeOnTimerTickListener(OnTimerTickListener listener) {
    mListeners.remove(listener);
  }

  public Timer() {
    this(0);
  }

  public Timer(long timeElapsed) {
    mTimeElapsedWhenLastResumed = timeElapsed;
    mTimeOfLastResume = -1;
    mHandler =
        new Handler() {
          @Override
          public void handleMessage(Message m) {
            if (isActive()) {
              dispatchTimerTick();
              sendMessageDelayed(Message.obtain(this, TICK_WHAT), 1000);
            }
          }
        };
  }

  void resume() {
    if (!isActive()) {
      mTimeOfLastResume = System.currentTimeMillis();
      update();
    }
  }

  void pause() {
    if (isActive()) {
      mTimeElapsedWhenLastResumed += System.currentTimeMillis() - mTimeOfLastResume;
      mTimeOfLastResume = -1;
      update();
    }
  }

  long getElapsed() {
    return mTimeElapsedWhenLastResumed
        + (isActive() ? (System.currentTimeMillis() - mTimeOfLastResume) : 0);
  }

  private boolean isActive() {
    return (mTimeOfLastResume != -1);
  }

  private void update() {
    dispatchTimerTick();
    if (isActive()) {
      mHandler.sendMessageDelayed(Message.obtain(mHandler, TICK_WHAT), 1000);
    } else {
      mHandler.removeMessages(TICK_WHAT);
    }
  }

  private void dispatchTimerTick() {
    for (OnTimerTickListener listener : mListeners) {
      listener.onTimerTick(getElapsed());
    }
  }
}
