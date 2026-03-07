package com.antsapps.triples;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;
import androidx.annotation.NonNull;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.Deck;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.Utils;
import com.antsapps.triples.backend.multiplayer.Player;
import com.antsapps.triples.backend.multiplayer.Room;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MatchmakingActivity extends BaseTriplesActivity {

  private static final int STATE_MAIN = 0;
  private static final int STATE_FRIENDS = 1;
  private static final int STATE_SEARCHING = 2;

  private ViewAnimator mAnimator;
  private TextView mTvStatus;
  private TextView mTvRoomCodeDisplay;
  private EditText mEtRoomCode;
  private View mBtnStartGame;

  private DatabaseReference mRoomsRef;
  private DatabaseReference mCurrentRoomRef;
  private ValueEventListener mRoomListener;

  private final Handler mHandler = new Handler();
  private final Runnable mTimeoutRunnable = () -> {
    if (mCurrentRoomRef != null) {
      Toast.makeText(this, R.string.matchmaking_timeout, Toast.LENGTH_LONG).show();
      cancelMatchmaking();
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (FirebaseAuth.getInstance().getCurrentUser() == null) {
      Toast.makeText(this, "Please sign in to play multiplayer", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }

    setContentView(R.layout.matchmaking);

    mAnimator = findViewById(R.id.matchmaking_animator);
    mTvStatus = findViewById(R.id.tv_status);
    mTvRoomCodeDisplay = findViewById(R.id.tv_room_code_display);
    mEtRoomCode = findViewById(R.id.et_room_code);
    mBtnStartGame = findViewById(R.id.btn_start_game);

    mRoomsRef = FirebaseDatabase.getInstance().getReference("rooms");

    findViewById(R.id.btn_random_match).setOnClickListener(v -> startRandomMatch());
    findViewById(R.id.btn_play_with_friends).setOnClickListener(v -> mAnimator.setDisplayedChild(STATE_FRIENDS));
    findViewById(R.id.btn_create_room).setOnClickListener(v -> createFriendRoom());
    findViewById(R.id.btn_join_room).setOnClickListener(v -> joinFriendRoom());
    findViewById(R.id.btn_cancel_matchmaking).setOnClickListener(v -> cancelMatchmaking());
    findViewById(R.id.btn_start_game).setOnClickListener(v -> startGame());
  }

  private void startRandomMatch() {
    mAnimator.setDisplayedChild(STATE_SEARCHING);
    mTvStatus.setText(R.string.searching_for_opponent);
    mTvRoomCodeDisplay.setVisibility(View.GONE);
    mBtnStartGame.setVisibility(View.GONE);

    mRoomsRef.orderByChild("gameState").equalTo(Room.STATE_LOBBY).limitToFirst(1)
        .addListenerForSingleValueEvent(new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.hasChildren()) {
              DataSnapshot roomSnapshot = snapshot.getChildren().iterator().next();
              joinRoom(roomSnapshot.getKey());
            } else {
              createRoom(true);
            }
          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {}
        });

    mHandler.postDelayed(mTimeoutRunnable, 30000);
  }

  private void createFriendRoom() {
    createRoom(false);
  }

  private void joinFriendRoom() {
    String code = mEtRoomCode.getText().toString().trim().toUpperCase();
    if (!code.isEmpty()) {
      joinRoom(code);
    }
  }

  private void createRoom(boolean isRandom) {
    String code = generateRoomCode();
    mCurrentRoomRef = mRoomsRef.child(code);

    Room room = new Room();
    room.code = code;
    room.gameState = Room.STATE_LOBBY;
    room.seed = new Random().nextLong();

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    Player me = new Player(user.getUid(), user.getDisplayName() != null ? user.getDisplayName() : "Player 1");
    room.players.put(user.getUid(), me);

    mCurrentRoomRef.setValue(room).addOnSuccessListener(aVoid -> {
      mAnimator.setDisplayedChild(STATE_SEARCHING);
      mTvStatus.setText(isRandom ? R.string.searching_for_opponent : R.string.waiting_for_players);
      mTvRoomCodeDisplay.setVisibility(View.VISIBLE);
      mTvRoomCodeDisplay.setText(getString(R.string.room_code_format, code));
      mBtnStartGame.setVisibility(isRandom ? View.GONE : View.VISIBLE);
      listenToRoom(code);
    });
  }

  private void joinRoom(String code) {
    mCurrentRoomRef = mRoomsRef.child(code);
    mCurrentRoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        Room room = snapshot.getValue(Room.class);
        if (room != null && Room.STATE_LOBBY.equals(room.gameState)) {
          FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
          Player me = new Player(user.getUid(), user.getDisplayName() != null ? user.getDisplayName() : "Player " + (room.players.size() + 1));
          mCurrentRoomRef.child("players").child(user.getUid()).setValue(me);

          mAnimator.setDisplayedChild(STATE_SEARCHING);
          mTvStatus.setText(R.string.waiting_for_players);
          mTvRoomCodeDisplay.setVisibility(View.VISIBLE);
          mTvRoomCodeDisplay.setText(getString(R.string.room_code_format, code));
          listenToRoom(code);
        } else {
          Toast.makeText(MatchmakingActivity.this, "Room not found or already started.", Toast.LENGTH_SHORT).show();
        }
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {}
    });
  }

  private void listenToRoom(String code) {
    mRoomListener = mCurrentRoomRef.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        Room room = snapshot.getValue(Room.class);
        if (room == null) return;

        if (Room.STATE_ACTIVE.equals(room.gameState)) {
          mHandler.removeCallbacks(mTimeoutRunnable);
          mCurrentRoomRef.removeEventListener(this);
          mRoomListener = null;
          startMultiplayerGameActivity(room);
        } else if (room.players.size() >= 2 && mTvStatus.getText().equals(getString(R.string.searching_for_opponent))) {
            // If random match and we have 2 players, start!
            if (mBtnStartGame.getVisibility() != View.VISIBLE) { // Host of random match doesn't need to press start
                startGame();
            }
        }
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {}
    });
  }

  private void startGame() {
    if (mCurrentRoomRef == null) return;

    mCurrentRoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            Room room = snapshot.getValue(Room.class);
            if (room == null) return;

            // Initialize Deck and Board
            Deck deck = new Deck(new Random(room.seed));
            List<Card> cardsInPlay = new ArrayList<>();
            while (cardsInPlay.size() < 12 || Game.getAValidTriple(cardsInPlay, com.google.common.collect.Sets.newHashSet()) == null) {
                for (int i = 0; i < 3; i++) {
                    cardsInPlay.add(deck.getNextCard());
                }
            }

            List<Integer> boardBytes = new ArrayList<>();
            for (Card c : cardsInPlay) boardBytes.add((int) Utils.cardToByte(c));
            List<Integer> deckBytes = new ArrayList<>();
            for (Card c : deck.getCardsRemainingList()) deckBytes.add((int) Utils.cardToByte(c));

            mCurrentRoomRef.child("boardCardBytes").setValue(boardBytes);
            mCurrentRoomRef.child("deckCardBytes").setValue(deckBytes);
            mCurrentRoomRef.child("gameState").setValue(Room.STATE_ACTIVE);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {}
    });
  }

  private void startMultiplayerGameActivity(Room room) {
    Intent intent = new Intent(this, MultiplayerGameActivity.class);
    intent.putExtra("room_code", room.code);
    startActivity(intent);
    finish();
  }

  private void cancelMatchmaking() {
    mHandler.removeCallbacks(mTimeoutRunnable);
    if (mCurrentRoomRef != null) {
      if (mRoomListener != null) {
        mCurrentRoomRef.removeEventListener(mRoomListener);
        mRoomListener = null;
      }
      FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
      mCurrentRoomRef.child("players").child(user.getUid()).removeValue();
      // If no players left, could delete room, but for simplicity just leave it.
      mCurrentRoomRef = null;
    }
    mAnimator.setDisplayedChild(STATE_MAIN);
  }

  private String generateRoomCode() {
    String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    StringBuilder sb = new StringBuilder();
    Random r = new Random();
    for (int i = 0; i < 4; i++) {
      sb.append(chars.charAt(r.nextInt(chars.length())));
    }
    return sb.toString();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mHandler.removeCallbacks(mTimeoutRunnable);
    if (mCurrentRoomRef != null && mRoomListener != null) {
      mCurrentRoomRef.removeEventListener(mRoomListener);
    }
  }
}
