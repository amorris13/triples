package com.antsapps.triples.backend;

import java.util.List;

public interface Period {
  List<Game> filter(Iterable<Game> games);
}