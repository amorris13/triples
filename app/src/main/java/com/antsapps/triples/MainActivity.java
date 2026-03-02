package com.antsapps.triples;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.ArcadeGame;
import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.Game;
import com.google.common.collect.Iterables;

public class MainActivity extends BaseTriplesActivity {

  private Application mApplication;

  private Button mClassicResumeButton;
  private Button mArcadeResumeButton;
  private Button mClassicNewGameButton;
  private Button mArcadeNewGameButton;
  private TextView mClassicGameInfo;
  private TextView mArcadeGameInfo;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mApplication = Application.getInstance(getApplication());

    mClassicResumeButton = findViewById(R.id.classic_resume_button);
    mClassicResumeButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        resumeGame(mApplication.getCurrentClassicGames(), ClassicGameActivity.class);
      }
    });

    mClassicNewGameButton = findViewById(R.id.classic_new_game_button);
    mClassicNewGameButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startNewClassicGame();
      }
    });

    mClassicGameInfo = findViewById(R.id.classic_game_info);

    findViewById(R.id.classic_statistics_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        showStatistics("Classic");
      }
    });

    mArcadeResumeButton = findViewById(R.id.arcade_resume_button);
    mArcadeResumeButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        resumeGame(mApplication.getCurrentArcadeGames(), ArcadeGameActivity.class);
      }
    });

    mArcadeNewGameButton = findViewById(R.id.arcade_new_game_button);
    mArcadeNewGameButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startNewArcadeGame();
      }
    });

    mArcadeGameInfo = findViewById(R.id.arcade_game_info);

    findViewById(R.id.arcade_statistics_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        showStatistics("Arcade");
      }
    });
  }

  @Override
  protected void onResume() {
    super.onResume();
    updateResumeButtons();
  }

  private void updateResumeButtons() {
    ClassicGame classicGame = Iterables.getFirst(mApplication.getCurrentClassicGames(), null);
    if (classicGame != null) {
      mClassicResumeButton.setVisibility(View.VISIBLE);
      mClassicNewGameButton.setText(R.string.start_again);
      mClassicGameInfo.setVisibility(View.VISIBLE);
      mClassicGameInfo.setText(getString(R.string.cards_remaining_format, classicGame.getCardsRemaining()));
    } else {
      mClassicResumeButton.setVisibility(View.GONE);
      mClassicNewGameButton.setText(R.string.new_game);
      mClassicGameInfo.setVisibility(View.GONE);
    }

    ArcadeGame arcadeGame = Iterables.getFirst(mApplication.getCurrentArcadeGames(), null);
    if (arcadeGame != null) {
      mArcadeResumeButton.setVisibility(View.VISIBLE);
      mArcadeNewGameButton.setText(R.string.start_again);
      mArcadeGameInfo.setVisibility(View.VISIBLE);
      mArcadeGameInfo.setText(getString(R.string.triples_found_format, arcadeGame.getNumTriplesFound()));
    } else {
      mArcadeResumeButton.setVisibility(View.GONE);
      mArcadeNewGameButton.setText(R.string.new_game);
      mArcadeGameInfo.setVisibility(View.GONE);
    }
  }

  private void resumeGame(Iterable<? extends Game> currentGames, Class<?> activityClass) {
    Game game = Iterables.getFirst(currentGames, null);
    if (game != null) {
      Intent intent = new Intent(this, activityClass);
      intent.putExtra(Game.ID_TAG, game.getId());
      startActivity(intent);
    }
  }

  private void startNewClassicGame() {
    for (ClassicGame game : mApplication.getCurrentClassicGames()) {
      mApplication.deleteClassicGame(game);
    }
    ClassicGame game = ClassicGame.createFromSeed(System.currentTimeMillis());
    mApplication.addClassicGame(game);
    Intent intent = new Intent(this, ClassicGameActivity.class);
    intent.putExtra(Game.ID_TAG, game.getId());
    startActivity(intent);
  }

  private void startNewArcadeGame() {
    for (ArcadeGame game : mApplication.getCurrentArcadeGames()) {
      mApplication.deleteArcadeGame(game);
    }
    ArcadeGame game = ArcadeGame.createFromSeed(System.currentTimeMillis());
    mApplication.addArcadeGame(game);
    Intent intent = new Intent(this, ArcadeGameActivity.class);
    intent.putExtra(Game.ID_TAG, game.getId());
    startActivity(intent);
  }

  private void showStatistics(String gameType) {
    Intent intent = new Intent(this, StatisticsActivity.class);
    intent.putExtra(StatisticsActivity.GAME_TYPE, gameType);
    startActivity(intent);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.statistics, menu); // Reuse statistics menu for main activity
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    menu.findItem(R.id.signout).setVisible(isSignedIn());
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int itemId = item.getItemId();
    if (itemId == R.id.help) {
      Intent helpIntent = new Intent(getBaseContext(), HelpActivity.class);
      startActivity(helpIntent);
      return true;
    } else if (itemId == R.id.settings) {
      Intent settingsIntent = new Intent(getBaseContext(), SettingsActivity.class);
      startActivity(settingsIntent);
      return true;
    } else if (itemId == R.id.signout) {
      signOut();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onSignInSucceeded() {
    super.onSignInSucceeded();
    invalidateOptionsMenu();
  }

  @Override
  public void onSignOut() {
    super.onSignOut();
    invalidateOptionsMenu();
  }
}
