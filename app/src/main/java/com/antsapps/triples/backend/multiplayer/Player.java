package com.antsapps.triples.backend.multiplayer;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Player {
  public String id;
  public String name;
  public int score;
  public boolean active;

  public Player() {
    // Default constructor required for calls to DataSnapshot.getValue(Player.class)
  }

  public Player(String id, String name) {
    this.id = id;
    this.name = name;
    this.score = 0;
    this.active = true;
  }
}
