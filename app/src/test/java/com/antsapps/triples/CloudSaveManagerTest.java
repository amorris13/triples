package com.antsapps.triples;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.ArcadeGame;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.CloudSaveSerializer;
import com.antsapps.triples.backend.Deck;
import com.antsapps.triples.backend.Game;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.Test;

public class CloudSaveManagerTest {

  @Test
  public void testMergeNewGames() {
    Application application = mock(Application.class);

    // Setup local games
    ClassicGame localClassic = new ClassicGame(1, 0, Collections.<Card>emptyList(),
        Collections.<Long>emptyList(), new Deck(Collections.<Card>emptyList()),
        0, new Date(1000), Game.GameState.COMPLETED, false);
    when(application.getCompletedClassicGames()).thenReturn(Lists.newArrayList(localClassic));

    ArcadeGame localArcade = new ArcadeGame(2, 0, Collections.<Card>emptyList(),
        Collections.<Long>emptyList(), new Deck(Collections.<Card>emptyList()),
        0, new Date(2000), Game.GameState.COMPLETED, 10, false);
    when(application.getCompletedArcadeGames()).thenReturn(Lists.newArrayList(localArcade));

    // Setup cloud data
    ClassicGame cloudClassicNew = new ClassicGame(-1, 0, Collections.<Card>emptyList(),
        Collections.<Long>emptyList(), new Deck(Collections.<Card>emptyList()),
        0, new Date(3000), Game.GameState.COMPLETED, false);
    ClassicGame cloudClassicExisting = new ClassicGame(-1, 0, Collections.<Card>emptyList(),
        Collections.<Long>emptyList(), new Deck(Collections.<Card>emptyList()),
        0, new Date(1000), Game.GameState.COMPLETED, false);

    ArcadeGame cloudArcadeNew = new ArcadeGame(-1, 0, Collections.<Card>emptyList(),
        Collections.<Long>emptyList(), new Deck(Collections.<Card>emptyList()),
        0, new Date(4000), Game.GameState.COMPLETED, 15, false);

    CloudSaveSerializer.CloudData cloudData = new CloudSaveSerializer.CloudData(
        Lists.newArrayList(cloudClassicNew, cloudClassicExisting),
        Lists.newArrayList(cloudArcadeNew)
    );

    boolean changed = CloudSaveManager.merge(application, cloudData);

    assertThat(changed).isTrue();
    verify(application).addClassicGame(cloudClassicNew);
    verify(application).addArcadeGame(cloudArcadeNew);
  }
}
