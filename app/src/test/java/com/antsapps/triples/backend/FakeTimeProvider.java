package com.antsapps.triples.backend;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class FakeTimeProvider implements TimeProvider {
  private long mCurrentTimeMillis;

  public FakeTimeProvider(long initialTimeMillis) {
    mCurrentTimeMillis = initialTimeMillis;
  }

  public void advanceTime(long millis) {
    mCurrentTimeMillis += millis;
  }

  public void setTime(long millis) {
    mCurrentTimeMillis = millis;
  }

  @Override
  public long currentTimeMillis() {
    return mCurrentTimeMillis;
  }

  @Override
  public Date now() {
    return new Date(mCurrentTimeMillis);
  }

  @Override
  public Calendar getCalendar() {
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    cal.setTimeInMillis(mCurrentTimeMillis);
    return cal;
  }
}
