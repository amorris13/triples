package com.antsapps.triples;

import static com.google.common.truth.Truth.assertThat;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.Game;
import org.junit.Test;

public class CompletedGameMenuVisibilityTest extends BaseRobolectricTest {

  @Test
  public void testMenuVisibilityWhenCompleted() {
    Application app = Application.getInstance(ApplicationProvider.getApplicationContext());
    ClassicGame game = ClassicGame.createFromSeed(12345L);
    app.addClassicGame(game);

    Intent intent =
        new Intent(ApplicationProvider.getApplicationContext(), ClassicGameActivity.class);
    intent.putExtra(Game.ID_TAG, game.getId());

    try (ActivityScenario<ClassicGameActivity> scenario = ActivityScenario.launch(intent)) {
      // First, verify they are visible when active
      scenario.onActivity(
          activity -> {
            Toolbar toolbar = activity.findViewById(R.id.toolbar);
            Menu menu = toolbar.getMenu();
            assertThat(menu.findItem(R.id.hint).isVisible()).isTrue();
            assertThat(menu.findItem(R.id.explanation).isVisible()).isTrue();
          });

      // Now complete the game
      scenario.onActivity(
          activity -> {
            game.finish();
            activity.invalidateOptionsMenu();
          });

      // Verify they are hidden (this should fail before the fix)
      scenario.onActivity(
          activity -> {
            Toolbar toolbar = activity.findViewById(R.id.toolbar);
            Menu menu = toolbar.getMenu();
            assertThat(menu.findItem(R.id.hint).isVisible()).isFalse();
            assertThat(menu.findItem(R.id.explanation).isVisible()).isFalse();
          });
    }
  }
}
