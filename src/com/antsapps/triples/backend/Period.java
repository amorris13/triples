package com.antsapps.triples.backend;

import java.util.List;

import com.google.common.collect.Lists;

public interface Period {

  public static final Period ALL_TIME = new Period() {
    @Override
    public List<Game> filter(Iterable<Game> games) {
      return Lists.newArrayList(games);
    }
  };

  List<Game> filter(Iterable<Game> games);
}