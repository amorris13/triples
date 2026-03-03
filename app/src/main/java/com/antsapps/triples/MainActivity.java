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
import com.google.common.collect.Lists;

import java.util.ArrayList;

public class MainActivity extends BaseTriplesActivity {

  private Application mApplication;

  private Button mClassicResumeButton;
  private Button mArcadeResumeButton;
  private Button mClassicNewGameButton;
  private Button mArcadeNewGameButton;
  private Button mClassicStatisticsButton;
  private Button mArcadeStatisticsButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayShowHomeEnabled(true);
      getSupportActionBar().setIcon(R.drawable.launcher);
    }

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

    mClassicStatisticsButton = findViewById(R.id.classic_statistics_button);
    mClassicStatisticsButton.setOnClickListener(new View.OnClickListener() {
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

    mArcadeStatisticsButton = findViewById(R.id.arcade_statistics_button);
    mArcadeStatisticsButton.setOnClickListener(new View.OnClickListener() {
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
      mClassicResumeButton.setText(getString(R.string.resume_game_classic_format, classicGame.getCardsRemaining()));
      mClassicNewGameButton.setText(R.string.start_again);
    } else {
      mClassicResumeButton.setVisibility(View.GONE);
      mClassicNewGameButton.setText(R.string.new_game);
    }
    int numClassicCompleted = Iterables.size(mApplication.getCompletedClassicGames());
    mClassicStatisticsButton.setText(getString(R.string.statistics_format, numClassicCompleted));

    ArcadeGame arcadeGame = Iterables.getFirst(mApplication.getCurrentArcadeGames(), null);
    if (arcadeGame != null) {
      mArcadeResumeButton.setVisibility(View.VISIBLE);
      mArcadeResumeButton.setText(getString(R.string.resume_game_arcade_format, arcadeGame.getNumTriplesFound()));
      mArcadeNewGameButton.setText(R.string.start_again);
    } else {
      mArcadeResumeButton.setVisibility(View.GONE);
      mArcadeNewGameButton.setText(R.string.new_game);
    }
    int numArcadeCompleted = Iterables.size(mApplication.getCompletedArcadeGames());
    mArcadeStatisticsButton.setText(getString(R.string.statistics_format, numArcadeCompleted));
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
    for (ClassicGame game : new ArrayList<>(Lists.newArrayList(mApplication.getCurrentClassicGames()))) {
      mApplication.deleteClassicGame(game);
    }
    ClassicGame game = ClassicGame.createFromSeed(System.currentTimeMillis());
    mApplication.addClassicGame(game);
    Intent intent = new Intent(this, ClassicGameActivity.class);
    intent.putExtra(Game.ID_TAG, game.getId());
    startActivity(intent);
  }

  private void startNewArcadeGame() {
    for (ArcadeGame game : new ArrayList<>(Lists.newArrayList(mApplication.getCurrentArcadeGames()))) {
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
    MenuItem exportItem = menu.findItem(R.id.export_to_csv);
    if (exportItem != null) {
      exportItem.setVisible(false);
    }
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
