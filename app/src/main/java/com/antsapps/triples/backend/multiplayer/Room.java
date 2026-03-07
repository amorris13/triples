package com.antsapps.triples.backend.multiplayer;

import com.google.firebase.database.IgnoreExtraProperties;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class Room {
  public static final String STATE_LOBBY = "LOBBY";
  public static final String STATE_ACTIVE = "ACTIVE";
  public static final String STATE_COMPLETED = "COMPLETED";

  public String code;
  public long seed;
  public String gameState;
  public Map<String, Player> players = new HashMap<>();
  public List<Integer> boardCardBytes;
  public List<Integer> deckCardBytes;
  public Map<String, TripleFoundEvent> triplesFound = new HashMap<>(); // key can be timestamp or push id

  public Room() {
  }
}
