package com.antsapps.triples.backend;

import java.util.Comparator;

public class ReversableComparator<T> implements Comparator<T> {

  private final Comparator<T> mUnderlyingComparator;
  private boolean mAscending = true;

  public ReversableComparator(Comparator<T> comparatorToWrap) {
    mUnderlyingComparator = comparatorToWrap;
  }

  public void reverse() {
    mAscending = !mAscending;
  }

  public boolean isAscending() {
    return mAscending;
  }

  @Override
  public int compare(T lhs, T rhs) {
    if (mAscending) {
      return mUnderlyingComparator.compare(lhs, rhs);
    } else {
      return mUnderlyingComparator.compare(rhs, lhs);
    }
  }
}