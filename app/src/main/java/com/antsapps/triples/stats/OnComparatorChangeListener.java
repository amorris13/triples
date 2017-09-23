package com.antsapps.triples.stats;

import java.util.Comparator;

interface OnComparatorChangeListener<T> {
  void onComparatorChange(Comparator<T> comparator);
}
