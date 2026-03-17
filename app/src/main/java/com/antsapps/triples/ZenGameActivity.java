package com.antsapps.triples;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import android.widget.TextView;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.ZenGame;

/** Zen/Beginner Game */
public class ZenGameActivity extends BaseGameActivity {

  public static final String IS_BEGINNER = "is_beginner";

  private ZenGame mGame;
  private Application mApplication;

  @Override
  protected void init(Bundle savedInstanceState) {
    mApplication = Application.getInstance(this);

    boolean isBeginner = getIntent().getBooleanExtra(IS_BEGINNER, false);
    mGame = mApplication.getZenGame(isBeginner);

    setTitle(isBeginner ? R.string.beginner_title : R.string.zen_title);

    ViewStub stub = (ViewStub) findViewById(R.id.status_bar);
    if (isBeginner) {
      stub.setLayoutResource(R.layout.beginner_statusbar);
      stub.inflate();
      mExplanationView.setVisibility(View.VISIBLE);
      logExplanationEvent(AnalyticsConstants.Param.TRIGGER_SOURCE_AUTO);
      invalidateOptionsMenu();
    } else {
      stub.setVisibility(View.GONE);
    }
    mGame.addOnUpdateCardsInPlayListener(this);
  }

  @Override
  protected Game getGame() {
    return mGame;
  }

  @Override
  protected String getCompletedStats() {
    return "";
  }

  @Override
  protected void updatePerformanceDescriptionInternal(TextView performanceTv) {}

  @Override
  protected String getGameType() {
    return "Classic";
  }

  @Override
  protected void awardAchievements() {}

  @Override
  protected void saveGame() {
    // Zen games are ephemeral and not saved to the database.
  }

  @Override
  protected void submitScore() {
    // Zen mode has no scores/leaderboards
  }

  @Override
  protected int getAccentColor() {
    return getResources().getColor(R.color.zen_accent);
  }

  @Override
  protected Intent createNewGame() {
    mApplication.resetZenGame(mGame.isBeginner());
    Intent newGameIntent = new Intent(getBaseContext(), ZenGameActivity.class);
    newGameIntent.putExtra(IS_BEGINNER, mGame.isBeginner());
    return newGameIntent;
  }
}
