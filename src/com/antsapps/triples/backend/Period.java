package com.antsapps.triples.backend;

import java.util.List;

import com.google.common.collect.Lists;

public interface Period<T extends Game> {

  public static final Period ALL_TIME = new Period<Game>() {
    @Override
    public List<Game> filter(Iterable<Game> games) {
      return Lists.newArrayList(games);
    }
  };

  List<T> filter(Iterable<T> games);
}