package com.antsapps.triples;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ViewSwitcher;

import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.Game.GameState;
import com.antsapps.triples.backend.Game.OnUpdateGameStateListener;
import com.antsapps.triples.cardsview.CardsView;

public abstract class BaseGameActivity extends Activity implements
    OnUpdateGameStateListener, GameHelper.GameHelperListener {

  public class SignInDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      // Use the Builder class for convenient dialog construction
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      // Get the layout inflater
      LayoutInflater inflater = getActivity().getLayoutInflater();

      // Inflate and set the layout for the dialog
      // Pass null as the parent view because its going in the dialog layout
      View view = inflater.inflate(R.layout.signin_dialog, null);
      view.findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          mHelper.beginUserInitiatedSignIn();
          SignInDialogFragment.this.dismiss();
        }
      });
      builder.setView(view);
      // Create the AlertDialog object and return it
      return builder.create();
    }
  }

  private ViewSwitcher mViewSwitcher;
  private CardsView mCardsView;
  private GameState mGameState;

  protected GameHelper mHelper;

  private boolean shouldSubmitScoreOnSignIn = false;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.game);

    init(savedInstanceState);

    getGame().addOnUpdateGameStateListener(this);
    GameState originalGameState = getGame().getGameState();

    mCardsView = (CardsView) findViewById(R.id.cards_view);
    mCardsView.setOnValidTripleSelectedListener(getGame());
    mCardsView.setEnabled(originalGameState != GameState.COMPLETED);
    getGame().addOnUpdateCardsInPlayListener(mCardsView);

    mViewSwitcher = (ViewSwitcher) findViewById(R.id.view_switcher);

    ActionBar actionBar = getActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);

    getGame().begin();

    if (originalGameState == GameState.STARTING) {
      mCardsView.shouldSlideIn();
    }

    mHelper = new GameHelper(this);
    mHelper.setup(this, GameHelper.CLIENT_PLUS | GameHelper.CLIENT_GAMES);
  }
  
  protected abstract Game getGame();

  /**
   * This must initialize the game (so that getGame() doesn't return null) and set the content
   * view with a layout that contains a StatusBar (R.id.status_bar) and a CardsView
   * (R.id.cards_view).
   * @param savedInstanceState
   */
  protected abstract void init(Bundle savedInstanceState);

  @Override
  protected void onStart() {
    super.onStart();
    mHelper.onStart(this);
  }

  @Override
  protected void onStop() {
    super.onStop();
    mHelper.onStop();
  }


  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    menu.findItem(R.id.pause).setVisible(mGameState == GameState.ACTIVE);
    menu.findItem(R.id.play).setVisible(mGameState == GameState.PAUSED);
    return true;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.game, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    switch (item.getItemId()) {
      case R.id.pause:
        getGame().pause();
        return true;
      case R.id.play:
        getGame().resume();
        return true;
      case R.id.help:
        Intent helpIntent = new Intent(getBaseContext(), HelpActivity.class);
        startActivity(helpIntent);
        return true;
      case R.id.settings:
        Intent settingsIntent = new Intent(getBaseContext(),
            SettingsActivity.class);
        startActivity(settingsIntent);
        return true;
      case android.R.id.home:
        // app icon in action bar clicked; go up one level
        Intent intent = new Intent(this, getParentClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  protected abstract Class<? extends BaseGameListActivity> getParentClass();

  @Override
  protected void onResume() {
    super.onResume();
    getGame().resumeFromLifecycle();

    SharedPreferences sharedPref = PreferenceManager
        .getDefaultSharedPreferences(this);
    if (sharedPref.getBoolean(getString(R.string.pref_screen_lock), true)) {
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    if (sharedPref.getBoolean(getString(R.string.pref_orientation_lock), false)) {
      String orientation = sharedPref.getString(
          getString(R.string.pref_orientation),
          "portrait");
      if (orientation.equals("portrait")) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
      } else if (orientation.equals("landscape")) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
      }
    } else {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }
    updateViewSwitcher();
  }

  @Override
  protected void onPause() {
    super.onPause();
    saveGame();
    getGame().pauseFromLifecycle();
    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    updateViewSwitcher();
  }

  protected abstract void saveGame();

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    outState.putLong(Game.ID_TAG, getGame().getId());
  }

  @Override
  protected void onDestroy() {
    getGame().removeOnUpdateCardsInPlayListener(mCardsView);
    getGame().removeOnUpdateGameStateListener(this);

    super.onDestroy();
  }

  @Override
  public void onUpdateGameState(GameState state) {
    mGameState = state;

    updateViewSwitcher();

    if (mGameState == GameState.COMPLETED) {
      mCardsView.setAlpha(0.5f);
    }

    invalidateOptionsMenu();
  }

  @Override
  public void gameFinished() {
    Log.i("BaseGameActivity", "game finished");
    mCardsView.setEnabled(false);
    if (mHelper.isSignedIn()) {
      submitScore();
    } else {
      shouldSubmitScoreOnSignIn = true;
      DialogFragment dialog = new SignInDialogFragment();
      dialog.show(getFragmentManager(), "SignInDialogFragment");
    }
  }

  protected abstract void submitScore();

  private void updateViewSwitcher() {
    int childToDisplay = 0;
    if(mGameState == GameState.PAUSED || !getGame().getActivityLifecycleActive()) {
      childToDisplay = 1;
    } else {
      childToDisplay = 0;
    }
    if (mViewSwitcher.getDisplayedChild() != childToDisplay) {
      mViewSwitcher.setDisplayedChild(childToDisplay);
    }
  }

  @Override
  public void onSignInFailed() {

  }

  @Override
  public void onSignInSucceeded() {
    if (mGameState == GameState.COMPLETED && shouldSubmitScoreOnSignIn) {
      submitScore();
    }
    shouldSubmitScoreOnSignIn = false;
  }

  @Override
  public void onSignOut() {

  }
}
