package com.antsapps.triples;

import android.content.Intent;
import android.text.format.DateUtils;

import androidx.fragment.app.Fragment;

import com.antsapps.triples.backend.ClassicGame;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.stats.ClassicStatisticsFragment;

import java.util.concurrent.TimeUnit;

public class ClassicGamesFragment extends BaseGamesFragment {

    @Override
    protected String getGameType() {
        return "Classic";
    }

    @Override
    protected Intent createNewGame() {
        ClassicGame game = ClassicGame.createFromSeed(System.currentTimeMillis());
        mApplication.addClassicGame(game);
        Intent newGameIntent = new Intent(getActivity(), ClassicGameActivity.class);
        newGameIntent.putExtra(Game.ID_TAG, game.getId());
        return newGameIntent;
    }

    @Override
    protected Fragment createStatisticsFragment() {
        return new ClassicStatisticsFragment();
    }

    @Override
    protected Iterable<? extends Game> getCurrentGames() {
        return mApplication.getCurrentClassicGames();
    }

    @Override
    protected void deleteGame(Game game) {
        mApplication.deleteClassicGame((ClassicGame) game);
    }

    @Override
    protected String getAnalyticsGameType() {
        return ClassicGame.GAME_TYPE_FOR_ANALYTICS;
    }

    @Override
    protected void bindGame(GameViewHolder holder, Game g) {
        holder.time.setText(DateUtils.formatElapsedTime(TimeUnit.MILLISECONDS.toSeconds(g.getTimeElapsed())));
        holder.progress.setText(String.valueOf(g.getCardsRemaining()));
        holder.whenStarted.setText(DateUtils.getRelativeTimeSpanString(g.getDateStarted().getTime()));
    }

    @Override
    protected Class<?> getGameActivityClass() {
        return ClassicGameActivity.class;
    }
}
