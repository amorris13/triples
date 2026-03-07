package com.antsapps.triples.backend.multiplayer;

import com.google.firebase.database.IgnoreExtraProperties;
import java.util.List;

@IgnoreExtraProperties
public class TripleFoundEvent {
  public String playerId;
  public List<Integer> cardBytes;
  public long timestamp;

  public TripleFoundEvent() {
  }

  public TripleFoundEvent(String playerId, List<Integer> cardBytes, long timestamp) {
    this.playerId = playerId;
    this.cardBytes = cardBytes;
    this.timestamp = timestamp;
  }
}
