package com.antsapps.triples.backend;

import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;

public final class DatePeriod implements Period {

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
  public List<Game> filter(Iterable<Game> games) {
    List<Game> filtered = Lists.newArrayList();
    for (Game game : games) {
      if (game.getDateStarted().compareTo(mSince) >= 0) {
        filtered.add(game);
      }
    }
    return filtered;
  }
}