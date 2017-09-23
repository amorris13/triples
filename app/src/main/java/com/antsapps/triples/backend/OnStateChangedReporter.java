package com.antsapps.triples.backend;

import com.google.common.collect.Sets;

import java.util.Collection;

public abstract class OnStateChangedReporter {
  Collection<OnStateChangedListener> onStateChangedListeners = Sets.newHashSet();

  public void addOnStateChangedListener(OnStateChangedListener listener) {
    onStateChangedListeners.add(listener);
  }

  public void removeOnStateChangedListener(OnStateChangedListener listener) {
    onStateChangedListeners.remove(listener);
  }

  /** This should be called whenever the state of the object is changed. */
  protected void notifyStateChanged() {
    for (OnStateChangedListener listener : onStateChangedListeners) {
      listener.onStateChanged();
    }
  }
}
