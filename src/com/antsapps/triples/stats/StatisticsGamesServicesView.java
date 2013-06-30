package com.antsapps.triples.stats;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.antsapps.triples.GameHelper;
import com.antsapps.triples.GamesServices;
import com.antsapps.triples.R;

class StatisticsGamesServicesView extends FrameLayout implements View.OnClickListener, GameHelper.GameHelperListener {

  private GameHelper mHelper;
  private View mSignInBar;
  private View mGamesServicesBar;

  public StatisticsGamesServicesView(Context context) {
    this(context, null);
  }

  public StatisticsGamesServicesView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public StatisticsGamesServicesView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);

    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
        Context.LAYOUT_INFLATER_SERVICE);
    View v = inflater.inflate(R.layout.game_services_summary, this);

    mSignInBar = v.findViewById(R.id.sign_in_bar);
    mGamesServicesBar = v.findViewById(R.id.games_services_bar);

    v.findViewById(R.id.sign_in_button).setOnClickListener(this);
    v.findViewById(R.id.achievements).setOnClickListener(this);
    v.findViewById(R.id.leaderboards).setOnClickListener(this);

    updateSignedInState();
  }

  void setGameHelper(GameHelper helper) {
    mHelper = helper;
  }

  @Override
  public void onClick(View view) {
    if (view.getId() == R.id.sign_in_button) {
      // start the asynchronous sign in flow
      mHelper.beginUserInitiatedSignIn();
    } else if (view.getId() == R.id.achievements) {
      ((Activity) getContext()).startActivityForResult(mHelper.getGamesClient().getAchievementsIntent(), 25);
    } else if (view.getId() == R.id.leaderboards) {
      ((Activity) getContext()).startActivityForResult(mHelper.getGamesClient().getLeaderboardIntent(GamesServices.Leaderboard.CLASSIC), 26);
    }
  }

  @Override
  public void onSignInFailed() {
    updateSignedInState();
  }

  @Override
  public void onSignInSucceeded() {
    updateSignedInState();
  }

  private void updateSignedInState() {
    if (mHelper == null || !mHelper.isSignedIn()) {
      mSignInBar.setVisibility(View.VISIBLE);
      mGamesServicesBar.setVisibility(View.GONE);
    } else {
      mSignInBar.setVisibility(View.GONE);
      mGamesServicesBar.setVisibility(View.VISIBLE);
    }
  }
}
