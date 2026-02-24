package com.antsapps.triples;

import android.content.Intent;
import android.text.format.DateUtils;

import androidx.fragment.app.Fragment;

import com.antsapps.triples.backend.ArcadeGame;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.stats.ArcadeStatisticsFragment;

import java.util.concurrent.TimeUnit;

public class ArcadeGamesFragment extends BaseGamesFragment {

    @Override
    protected String getGameType() {
        return "Arcade";
    }

    @Override
    protected Intent createNewGame() {
        ArcadeGame game = ArcadeGame.createFromSeed(System.currentTimeMillis());
        mApplication.addArcadeGame(game);
        Intent newGameIntent = new Intent(getActivity(), ArcadeGameActivity.class);
        newGameIntent.putExtra(Game.ID_TAG, game.getId());
        return newGameIntent;
    }

    @Override
    protected Fragment createStatisticsFragment() {
        return new ArcadeStatisticsFragment();
    }

    @Override
    protected Iterable<? extends Game> getCurrentGames() {
        return mApplication.getCurrentArcadeGames();
    }

    @Override
    protected void deleteGame(Game game) {
        mApplication.deleteArcadeGame((ArcadeGame) game);
    }

    @Override
    protected String getAnalyticsGameType() {
        return ArcadeGame.GAME_TYPE_FOR_ANALYTICS;
    }

    @Override
    protected void bindGame(GameViewHolder holder, Game game) {
        ArcadeGame g = (ArcadeGame) game;
        holder.time.setText(DateUtils.formatElapsedTime(TimeUnit.MILLISECONDS.toSeconds(ArcadeGame.TIME_LIMIT_MS - g.getTimeElapsed())));
        holder.progress.setText(String.valueOf(g.getNumTriplesFound()));
        holder.whenStarted.setText(DateUtils.getRelativeTimeSpanString(g.getDateStarted().getTime()));
    }

    @Override
    protected Class<?> getGameActivityClass() {
        return ArcadeGameActivity.class;
    }
}
