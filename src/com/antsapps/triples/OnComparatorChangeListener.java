package com.antsapps.triples;

import java.util.Comparator;

public interface OnComparatorChangeListener<T> {
  void onComparatorChange(Comparator<T> comparator);
}