package com.antsapps.triples;

import android.os.Bundle;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.antsapps.triples.backend.Game;

public class GameActivity extends SherlockActivity {
  private Game mGame;
  private CardsView mCardsView;
  private boolean mPaused;
  private StatusBar mStatusBar;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    mGame = new Game(System.currentTimeMillis());

    mStatusBar = new StatusBar((TextView) findViewById(R.id.timer_value_text),
        (TextView) findViewById(R.id.cards_remaining_text));
    mGame.setOnTimerTickListener(mStatusBar);
    mGame.addOnUpdateGameStateListener(mStatusBar);

    mCardsView = (CardsView) findViewById(R.id.cards_view);
    mCardsView.setGame(mGame);

    ActionBar actionBar = getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);

    mGame.begin();
    mPaused = false;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getSupportMenuInflater();
    inflater.inflate(R.menu.game, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    switch (item.getItemId()) {
      case R.id.pause:
        if (mPaused) {
          mPaused = false;
          mGame.resume();
          mCardsView.resume();
          item.setIcon(R.drawable.av_pause_holo_dark);
          item.setTitle("Pause");
        } else {
          mPaused = true;
          mGame.pause();
          mCardsView.pause();
          item.setIcon(R.drawable.av_play_holo_dark);
          item.setTitle("Play");
        }
        return true;
      case android.R.id.home:
        // app icon in action bar clicked; go up one level
        // Intent intent = new Intent(this, RoundList.class);
        // intent.putExtra(Match.ID_TAG, mRound.getMatch().getId());
        // startActivity(intent);
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

}