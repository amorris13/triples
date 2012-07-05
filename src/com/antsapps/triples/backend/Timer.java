package com.antsapps.triples.backend;

import android.os.Handler;
import android.os.Message;

class Timer {

  private static final int TICK_WHAT = 1;

  private OnTimerTickListener mListener;

  private long mTimeElapsedWhenLastResumed;

  private long mTimeOfLastResume;

  private final Handler mHandler;

  public void setOnTimerTickListener(OnTimerTickListener listener) {
    mListener = listener;
  }

  public Timer() {
    this(0);
  }

  public Timer(long timeElapsed) {
    mTimeElapsedWhenLastResumed = timeElapsed;
    mTimeOfLastResume = -1;
    mHandler = new Handler() {
      @Override
      public void handleMessage(Message m) {
        if (isActive()) {
          dispatchTimerTick();
          sendMessageDelayed(Message.obtain(this, TICK_WHAT), 1000);
        }
      }
    };
  }

  void start() {
    mTimeOfLastResume = System.currentTimeMillis();
    update();
  }

  void pause() {
    mTimeElapsedWhenLastResumed += System.currentTimeMillis()
        - mTimeOfLastResume;
    mTimeOfLastResume = -1;
    update();
  }

  void resume() {
    mTimeOfLastResume = System.currentTimeMillis();
    update();
  }

  long getElapsed() {
    return mTimeElapsedWhenLastResumed
        + (isActive() ? (System.currentTimeMillis() - mTimeOfLastResume) : 0);
  }

  private boolean isActive() {
    return (mTimeOfLastResume != -1);
  }

  private void update() {
    if (isActive()) {
      dispatchTimerTick();
      mHandler.sendMessageDelayed(Message.obtain(mHandler, TICK_WHAT), 1000);
    } else {
      mHandler.removeMessages(TICK_WHAT);
    }
  }

  private void dispatchTimerTick() {
    if (mListener != null) {
      mListener.onTimerTick(getElapsed());
    }
  }
}
