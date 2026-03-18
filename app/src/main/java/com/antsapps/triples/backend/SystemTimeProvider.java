package com.antsapps.triples.backend;

import java.util.Calendar;
import java.util.Date;

public class SystemTimeProvider implements TimeProvider {
  @Override
  public long currentTimeMillis() {
    return System.currentTimeMillis();
  }

  @Override
  public Date now() {
    return new Date();
  }

  @Override
  public Calendar getCalendar() {
    return Calendar.getInstance();
  }
}
