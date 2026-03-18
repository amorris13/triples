package com.antsapps.triples.backend;

import java.util.Calendar;
import java.util.Date;

public interface TimeProvider {
  long currentTimeMillis();

  Date now();

  Calendar getCalendar();
}
