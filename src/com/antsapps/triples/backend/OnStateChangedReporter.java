package com.antsapps.triples.backend;

import java.util.Collection;

import android.util.Log;

import com.google.common.collect.Sets;

public abstract class OnStateChangedReporter {
  Collection<OnStateChangedListener> onStateChangedListeners = Sets
      .newHashSet();

  public void addOnStateChangedListener(OnStateChangedListener listener) {
    Log.i("OSCR", "StateChangedListener " + listener.getClass().getSimpleName()
        + " added to " + this.getClass().getSimpleName());
    onStateChangedListeners.add(listener);
  }

  public void removeOnStateChangedListener(OnStateChangedListener listener) {
    onStateChangedListeners.remove(listener);
  }

  /**
   * This should be called whenever the state of the object is changed.
   */
  protected void notifyStateChanged() {
    for (OnStateChangedListener listener : onStateChangedListeners) {
      Log.i("OSCR", "onStateChanged called from "
          + this.getClass().getSimpleName() + " to "
          + listener.getClass().getSimpleName());
      listener.onStateChanged();
    }
  }
}