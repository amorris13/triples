package com.antsapps.triples;

import static com.google.common.truth.Truth.assertThat;

import android.content.Intent;
import com.antsapps.triples.backend.Game;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;

public class GameActivityNullGameTest extends BaseRobolectricTest {

  @Test
  public void testClassicGameActivity_nullGame_finishesGracefully() {
    Intent intent = new Intent();
    intent.putExtra(Game.ID_TAG, 999999L); // Game ID that does not exist

    ActivityController<ClassicGameActivity> controller =
        Robolectric.buildActivity(ClassicGameActivity.class, intent);
    ClassicGameActivity activity = controller.create().get();

    assertThat(activity.isFinishing()).isTrue();
  }

  @Test
  public void testArcadeGameActivity_nullGame_finishesGracefully() {
    Intent intent = new Intent();
    intent.putExtra(Game.ID_TAG, 999999L); // Game ID that does not exist

    ActivityController<ArcadeGameActivity> controller =
        Robolectric.buildActivity(ArcadeGameActivity.class, intent);
    ArcadeGameActivity activity = controller.create().get();

    assertThat(activity.isFinishing()).isTrue();
  }
}
