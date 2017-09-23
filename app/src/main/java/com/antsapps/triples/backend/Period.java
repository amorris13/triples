package com.antsapps.triples.backend;

import com.google.common.collect.Lists;

import java.util.List;

public interface Period<T extends Game> {

  public static final Period ALL_TIME =
      new Period<Game>() {
        @Override
        public List<Game> filter(Iterable<Game> games) {
          return Lists.newArrayList(games);
        }
      };

  List<T> filter(Iterable<T> games);
}
