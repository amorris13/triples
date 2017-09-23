package com.antsapps.triples.backend;

import com.google.common.collect.Lists;

import java.util.Date;
import java.util.List;

public final class DatePeriod<T extends Game> implements Period<T> {

  private final Date mSince;

  public static DatePeriod fromSince(Date since) {
    return new DatePeriod(since);
  }

  public static DatePeriod fromTimePeriod(long millis) {
    return new DatePeriod(new Date(System.currentTimeMillis() - millis));
  }

  private DatePeriod(Date since) {
    mSince = since;
  }

  @Override
  public List<T> filter(Iterable<T> games) {
    List<T> filtered = Lists.newArrayList();
    for (T game : games) {
      if (game.getDateStarted().compareTo(mSince) >= 0) {
        filtered.add(game);
      }
    }
    return filtered;
  }
}
