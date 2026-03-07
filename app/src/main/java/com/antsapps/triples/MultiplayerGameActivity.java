package com.antsapps.triples;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.Deck;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.Utils;
import com.antsapps.triples.backend.multiplayer.MultiplayerGame;
import com.antsapps.triples.backend.multiplayer.Player;
import com.antsapps.triples.backend.multiplayer.Room;
import com.antsapps.triples.backend.multiplayer.TripleFoundEvent;
import com.antsapps.triples.cardsview.CardsView;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MultiplayerGameActivity extends BaseGameActivity {

  private MultiplayerGame mGame;
  private String mRoomCode;
  private DatabaseReference mRoomRef;
  private TextView mTvPlayer1Score;
  private TextView mTvPlayer2Score;
  private String mMyId;
  private String mOpponentId;
  private ChildEventListener mTriplesFoundListener;
  private ValueEventListener mRoomUpdateListener;

  @Override
  protected void init(Bundle savedInstanceState) {
    mRoomCode = getIntent().getStringExtra("room_code");
    if (FirebaseAuth.getInstance().getCurrentUser() == null) {
      finish();
      return;
    }
    mMyId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    mRoomRef = FirebaseDatabase.getInstance().getReference("rooms").child(mRoomCode);

    ViewStub stub = findViewById(R.id.status_bar);
    stub.setLayoutResource(R.layout.multiplayer_statusbar);
    View statusBar = stub.inflate();
    mTvPlayer1Score = statusBar.findViewById(R.id.tv_player_1_score);
    mTvPlayer2Score = statusBar.findViewById(R.id.tv_player_2_score);

    // Initial dummy game, will be updated via sync
    mGame = new MultiplayerGame(mRoomCode, mMyId, 0, new ArrayList<>(), new ArrayList<>(), new Deck(new ArrayList<>()), 0, new Date(), Game.GameState.STARTING);

    mRoomUpdateListener = new ValueEventListener() {
      private boolean firstChange = true;
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        Room room = snapshot.getValue(Room.class);
        if (room != null) {
          updateScores(room);
          if (firstChange) {
            setupTriplesFoundListener();
            firstChange = false;
          }
        }
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {}
    };
    mRoomRef.addValueEventListener(mRoomUpdateListener);
  }

  private void setupTriplesFoundListener() {
    mTriplesFoundListener = mRoomRef.child("triplesFound").addChildEventListener(new ChildEventListener() {
      @Override
      public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
        TripleFoundEvent event = snapshot.getValue(TripleFoundEvent.class);
        if (event != null) {
          onTripleFoundEvent(event);
        }
      }

      @Override
      public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}
      @Override
      public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
      @Override
      public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}
      @Override
      public void onCancelled(@NonNull DatabaseError error) {}
    });
  }

  private void onTripleFoundEvent(TripleFoundEvent event) {
    Set<Card> triple = new HashSet<>();
    for (Integer b : event.cardBytes) {
      triple.add(Utils.cardFromByte(b.byteValue()));
    }

    CardsView cardsView = findViewById(R.id.cards_view);
    Rect targetRect = new Rect();
    if (event.playerId.equals(mMyId)) {
      mTvPlayer1Score.getGlobalVisibleRect(targetRect);
    } else {
      mTvPlayer2Score.getGlobalVisibleRect(targetRect);
    }

    // Convert global target Rect to local coordinates within CardsView
    int[] location = new int[2];
    cardsView.getLocationOnScreen(location);
    targetRect.offset(-location[0], -location[1]);

    animateTripleToTarget(triple, targetRect);
  }

  private void animateTripleToTarget(Set<Card> triple, Rect targetRect) {
    CardsView cardsView = findViewById(R.id.cards_view);
    cardsView.animateTripleFound(triple, targetRect);
  }

  private void updateScores(Room room) {
    List<Player> players = new ArrayList<>(room.players.values());
    if (players.size() >= 1) {
      Player p1 = null;
      Player p2 = null;
      for (Player p : players) {
        if (p.id.equals(mMyId)) p1 = p;
        else p2 = p;
      }
      if (p1 != null) mTvPlayer1Score.setText(p1.name + ": " + p1.score);
      if (p2 != null) {
        mTvPlayer2Score.setText(p2.name + ": " + p2.score);
        mOpponentId = p2.id;
      } else {
        mTvPlayer2Score.setText("Waiting...");
      }
    }
  }

  @Override
  protected Game getGame() {
    return mGame;
  }

  @Override
  protected int getAccentColor() {
    return getResources().getColor(R.color.classic_accent);
  }

  @Override
  protected void saveGame() {
    // No local save for multiplayer
  }

  @Override
  protected void submitScore() {
    // Scores are tracked in Firebase
  }

  @Override
  protected Intent createNewGame() {
    return new Intent(this, MatchmakingActivity.class);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mTriplesFoundListener != null) {
      mRoomRef.child("triplesFound").removeEventListener(mTriplesFoundListener);
    }
    if (mRoomUpdateListener != null) {
      mRoomRef.removeEventListener(mRoomUpdateListener);
    }
    if (mGame != null) {
      mGame.cleanup();
    }
  }
}
