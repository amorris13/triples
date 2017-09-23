package com.antsapps.triples.backend;

public interface OnStateChangedListener {

  /** This will be called to notify that the state of something that this depends on has changed. */
  void onStateChanged();
}
