package com.antsapps.triples.backend.multiplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.Deck;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.Utils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MultiplayerGame extends Game {

  public interface OnOpponentTripleFoundListener {
    void onOpponentTripleFound(String playerId, Set<Card> triple);
  }

  private final String mRoomCode;
  private final String mMyPlayerId;
  private final DatabaseReference mRoomRef;
  private OnOpponentTripleFoundListener mOpponentTripleFoundListener;
  private ValueEventListener mRoomListener;

  public MultiplayerGame(
      String roomCode,
      String myPlayerId,
      long seed,
      List<Card> cardsInPlay,
      List<Long> tripleFindTimes,
      Deck cardsInDeck,
      long timeElapsed,
      Date date,
      GameState gameState) {
    super(-1, seed, cardsInPlay, tripleFindTimes, cardsInDeck, timeElapsed, date, gameState, false);
    mRoomCode = roomCode;
    mMyPlayerId = myPlayerId;
    mRoomRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomCode);
    attachRoomListener();
  }

  public void setOnOpponentTripleFoundListener(OnOpponentTripleFoundListener listener) {
    mOpponentTripleFoundListener = listener;
  }

  private void attachRoomListener() {
    mRoomListener = new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        Room room = snapshot.getValue(Room.class);
        if (room == null) return;

        syncFromRoom(room);
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {
      }
    };
    mRoomRef.addValueEventListener(mRoomListener);
  }

  public void cleanup() {
    if (mRoomListener != null) {
      mRoomRef.removeEventListener(mRoomListener);
    }
  }

  private synchronized void syncFromRoom(Room room) {
    // 1. Update Board
    if (room.boardCardBytes != null) {
      List<Card> newCards = new ArrayList<>();
      for (Integer b : room.boardCardBytes) {
        newCards.add(Utils.cardFromByte(b.byteValue()));
      }
      if (!mCardsInPlay.equals(newCards)) {
        ImmutableList<Card> oldCards = ImmutableList.copyOf(mCardsInPlay);
        mCardsInPlay.clear();
        mCardsInPlay.addAll(newCards);
        dispatchCardsInPlayUpdate(oldCards);
      }
    }

    // 2. Update Deck
    if (room.deckCardBytes != null) {
      List<Card> newDeckCards = new ArrayList<>();
      for (Integer b : room.deckCardBytes) {
        newDeckCards.add(Utils.cardFromByte(b.byteValue()));
      }
      mDeck.setCards(newDeckCards);
    }

    // 3. Game State
    if (Room.STATE_COMPLETED.equals(room.gameState) && mGameState != GameState.COMPLETED) {
      finish();
    }

    // 4. Opponent Animations (triplesFound)
    // This part is tricky to do with just ValueEventListener because we need to know what's NEW.
    // We might need ChildEventListener for triplesFound.
  }

  @Override
  protected boolean isGameInValidState() {
    return true; // Controlled by server
  }

  @Override
  public void commitTriple(final Card... cards) {
    // Override commitTriple to use Firebase Transaction
    mRoomRef.runTransaction(new Transaction.Handler() {
      @NonNull
      @Override
      public Transaction.Result doTransaction(@NonNull MutableData currentData) {
        Room room = currentData.getValue(Room.class);
        if (room == null || !Room.STATE_ACTIVE.equals(room.gameState)) {
          return Transaction.success(currentData);
        }

        List<Card> roomBoard = new ArrayList<>();
        for (Integer b : room.boardCardBytes) {
          roomBoard.add(Utils.cardFromByte(b.byteValue()));
        }

        if (roomBoard.containsAll(Lists.newArrayList(cards)) && isValidTriple(cards)) {
          // Success! This player claimed the triple.

          // Update score
          Player me = room.players.get(mMyPlayerId);
          if (me != null) {
            me.score++;
          }

          // Remove cards from board
          for (Card c : cards) {
            int idx = roomBoard.indexOf(c);
            roomBoard.set(idx, null);
          }

          // Refill board logic
          Deck deck = new Deck(Collections.emptyList());
          List<Card> deckCards = new ArrayList<>();
          for (Integer b : room.deckCardBytes) {
            deckCards.add(Utils.cardFromByte(b.byteValue()));
          }
          deck.setCards(deckCards);

          while (numNotNull(roomBoard) < MIN_CARDS_IN_PLAY && !deck.isEmpty()) {
            int nullIdx = roomBoard.indexOf(null);
            roomBoard.set(nullIdx, deck.getNextCard());
          }

          // Remove trailing nulls and compact
          List<Card> compactedBoard = new ArrayList<>();
          for (Card c : roomBoard) {
            if (c != null) compactedBoard.add(c);
          }
          roomBoard = compactedBoard;

          while (!checkIfAnyValidTriples(roomBoard) && !deck.isEmpty()) {
            for (int i = 0; i < 3; i++) {
              if (!deck.isEmpty()) {
                roomBoard.add(deck.getNextCard());
              }
            }
          }

          // Update room data
          room.boardCardBytes = new ArrayList<>();
          for (Card c : roomBoard) {
            room.boardCardBytes.add((int) Utils.cardToByte(c));
          }
          room.deckCardBytes = new ArrayList<>();
          for (Card c : deck.getCardsRemainingList()) {
            room.deckCardBytes.add((int) Utils.cardToByte(c));
          }

          if (deck.isEmpty() && !checkIfAnyValidTriples(roomBoard)) {
            room.gameState = Room.STATE_COMPLETED;
          }

          // Add to triplesFound for animation
          TripleFoundEvent event = new TripleFoundEvent(mMyPlayerId,
              Lists.newArrayList((int)Utils.cardToByte(cards[0]), (int)Utils.cardToByte(cards[1]), (int)Utils.cardToByte(cards[2])),
              System.currentTimeMillis());
          room.triplesFound.put(mRoomRef.child("triplesFound").push().getKey(), event);

          currentData.setValue(room);
          return Transaction.success(currentData);
        } else {
          // Triple no longer valid or cards gone
          return Transaction.abort();
        }
      }

      @Override
      public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
        if (!committed) {
          // Maybe show a "Too slow!" message?
          mGameRenderer.clearSelectedCards();
        }
      }
    });
  }

  private static int numNotNull(Iterable<Card> cards) {
    int countNotNull = 0;
    for (Card card : cards) {
      if (card != null) countNotNull++;
    }
    return countNotNull;
  }

  private boolean checkIfAnyValidTriples(List<Card> cards) {
    return Game.getAValidTriple(cards, com.google.common.collect.Sets.<Card>newHashSet()) != null;
  }

  @Override
  public String getGameTypeForAnalytics() {
    return "multiplayer";
  }
}
