package com.antsapps.triples.stats;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.antsapps.triples.R;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.GameHelper;

class StatisticsGamesServicesView extends FrameLayout
    implements View.OnClickListener, GameHelper.GameHelperListener {

  private GameHelper mHelper;
  private View mSignInBar;
  private View mGamesServicesBar;
  private String mLeaderboardId;

  public StatisticsGamesServicesView(Context context, String leaderboardId) {
    super(context);

    mLeaderboardId = leaderboardId;
    LayoutInflater inflater =
        (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View v = inflater.inflate(R.layout.game_services_summary, this);

    mSignInBar = v.findViewById(R.id.sign_in_bar);
    mGamesServicesBar = v.findViewById(R.id.games_services_bar);

    v.findViewById(R.id.sign_in_button).setOnClickListener(this);
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
    } else if (view.getId() == R.id.leaderboards) {
      Intent leaderboardIntent =
          Games.Leaderboards.getLeaderboardIntent(mHelper.getApiClient(), mLeaderboardId);
      ((Activity) getContext()).startActivityForResult(leaderboardIntent, 26);
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
