package com.antsapps.triples;

/** Constants for use with analytics events. */
public class AnalyticsConstants {

  public static class Param {
    public static final String GAME_TYPE = "game_type";

    private Param() {};
  }

  public static class Event {

    public static final String NEW_GAME = "new_game";
    public static final String FINISH_GAME = "complete_game";
    public static final String VIEW_HELP = "view_help";

    private Event() {};
  }

  private AnalyticsConstants() {};
}
