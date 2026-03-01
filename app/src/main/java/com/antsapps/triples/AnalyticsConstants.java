package com.antsapps.triples;

/** Constants for use with analytics events. */
public class AnalyticsConstants {

  public static class Param {
    public static final String GAME_TYPE = "game_type";
    public static final String SETTING_NAME = "setting_name";
    public static final String SETTING_VALUE = "setting_value";
    public static final String MODE = "mode";
    public static final String TAB_NAME = "tab_name";

    private Param() {};
  }

  public static class Event {

    public static final String NEW_GAME = "new_game";
    public static final String FINISH_GAME = "complete_game";
    public static final String VIEW_HELP = "view_help";
    public static final String PAUSE_GAME = "pause_game";
    public static final String RESUME_GAME = "resume_game";
    public static final String USE_HINT = "use_hint";
    public static final String SIGN_IN = "sign_in";
    public static final String SIGN_OUT = "sign_out";
    public static final String CHANGE_SETTING = "change_setting";
    public static final String SWITCH_GAME_MODE = "switch_game_mode";
    public static final String SELECT_TAB = "select_tab";

    private Event() {};
  }

  private AnalyticsConstants() {};
}
