package com.antsapps.triples;

import static com.google.common.truth.Truth.assertThat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.Game;
import org.junit.Test;

public class HintMenuVisibilityTest extends BaseRobolectricTest {

  @Test
  public void testHintMenuVisibility() {
    Application app = Application.getInstance(ApplicationProvider.getApplicationContext());
    ClassicGame game = ClassicGame.createFromSeed(12345L);
    app.addClassicGame(game);

    Intent intent =
        new Intent(ApplicationProvider.getApplicationContext(), ClassicGameActivity.class);
    intent.putExtra(Game.ID_TAG, game.getId());

    try (ActivityScenario<ClassicGameActivity> scenario = ActivityScenario.launch(intent)) {
      // Case 1: pref_hide_hint is false (default)
      scenario.onActivity(
          activity -> {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
            String hideHintKey = activity.getString(R.string.pref_hide_hint);
            prefs.edit().putBoolean(hideHintKey, false).commit();
            activity.invalidateOptionsMenu();
          });

      scenario.onActivity(
          activity -> {
            Toolbar toolbar = activity.findViewById(R.id.toolbar);
            Menu menu = toolbar.getMenu();
            assertThat(menu).isNotNull();
            MenuItem hintItem = menu.findItem(R.id.hint);
            assertThat(hintItem).isNotNull();
            assertThat(hintItem.isVisible()).isTrue();
          });

      // Case 2: pref_hide_hint is true
      scenario.onActivity(
          activity -> {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
            String hideHintKey = activity.getString(R.string.pref_hide_hint);
            prefs.edit().putBoolean(hideHintKey, true).commit();
            activity.invalidateOptionsMenu();
          });

      scenario.onActivity(
          activity -> {
            Toolbar toolbar = activity.findViewById(R.id.toolbar);
            Menu menu = toolbar.getMenu();
            assertThat(menu).isNotNull();
            MenuItem hintItem = menu.findItem(R.id.hint);
            assertThat(hintItem).isNotNull();
            assertThat(hintItem.isVisible()).isTrue();
          });
    }
  }
}
