package com.antsapps.triples.stats;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import com.antsapps.triples.BaseTriplesActivity;
import com.antsapps.triples.R;
import com.google.android.gms.games.PlayGames;

class StatisticsGamesServicesView extends FrameLayout
    implements View.OnClickListener, BaseTriplesActivity.OnSignInListener {

  private static final String TAG = "StatsGamesServicesView";
  private BaseTriplesActivity mActivity;
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
    v.findViewById(R.id.achievements).setOnClickListener(this);

    updateSignedInState();
  }

  public void setActivity(BaseTriplesActivity activity) {
    mActivity = activity;
    updateSignedInState();
  }

  @Override
  public void onClick(View view) {
    if (view.getId() == R.id.sign_in_button) {
      // start the asynchronous sign in flow
      mActivity.signIn();
    } else if (view.getId() == R.id.leaderboards) {
      PlayGames.getLeaderboardsClient(mActivity)
          .getLeaderboardIntent(mLeaderboardId)
          .addOnCompleteListener(
              task -> {
                if (task.isSuccessful()) {
                  mActivity.startActivityForResult(task.getResult(), 26);
                } else {
                  Log.e(TAG, "Error getting leaderboard intent", task.getException());
                }
              });
    } else if (view.getId() == R.id.achievements) {
      PlayGames.getAchievementsClient(mActivity)
          .getAchievementsIntent()
          .addOnCompleteListener(
              task -> {
                if (task.isSuccessful()) {
                  mActivity.startActivityForResult(task.getResult(), 27);
                } else {
                  Log.e(TAG, "Error getting achievements intent", task.getException());
                }
              });
    }
  }

  @Override
  public void onSignInStateChanged(boolean signedInAndConnected) {
    updateSignedInState();
  }

  private void updateSignedInState() {
    if (mActivity == null || !mActivity.isSignedIn()) {
      mSignInBar.setVisibility(View.VISIBLE);
      mGamesServicesBar.setVisibility(View.GONE);
    } else {
      mSignInBar.setVisibility(View.GONE);
      mGamesServicesBar.setVisibility(View.VISIBLE);
    }
  }
}
