package com.antsapps.triples;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.OnStateChangedListener;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.List;

public abstract class BaseGamesFragment extends Fragment implements OnStateChangedListener {

    protected Application mApplication;
    protected RecyclerView mRecyclerView;
    protected GameAdapter mAdapter;
    protected TextView mEmptyView;
    protected FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_games, container, false);

        mApplication = Application.getInstance(getActivity());
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        TextView titleView = view.findViewById(R.id.btn_new_game);
        titleView.setText("NEW " + getGameType().toUpperCase() + " GAME");
        titleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newGameIntent = createNewGame();
                logNewGame();
                startActivity(newGameIntent);
            }
        });

        mRecyclerView = view.findViewById(R.id.rv_current_games);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new GameAdapter();
        mRecyclerView.setAdapter(mAdapter);

        mEmptyView = view.findViewById(R.id.tv_no_current_games);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.statistics_container, createStatisticsFragment())
                .commit();

        return view;
    }

    protected abstract String getGameType();
    protected abstract Intent createNewGame();
    protected abstract Fragment createStatisticsFragment();
    protected abstract Iterable<? extends Game> getCurrentGames();
    protected abstract void deleteGame(Game game);
    protected abstract String getAnalyticsGameType();

    private void logNewGame() {
        Bundle bundle = new Bundle();
        bundle.putString(AnalyticsConstants.Param.GAME_TYPE, getAnalyticsGameType());
        mFirebaseAnalytics.logEvent(AnalyticsConstants.Event.NEW_GAME, bundle);
    }

    @Override
    public void onStart() {
        super.onStart();
        mApplication.addOnStateChangedListener(this);
        updateDataSet();
    }

    @Override
    public void onStop() {
        super.onStop();
        mApplication.removeOnStateChangedListener(this);
    }

    @Override
    public void onStateChanged() {
        updateDataSet();
    }

    protected void updateDataSet() {
        List<Game> games = Lists.newArrayList();
        Iterables.addAll(games, getCurrentGames());
        mAdapter.setGames(games);
        mEmptyView.setVisibility(games.isEmpty() ? View.VISIBLE : View.GONE);
    }

    protected class GameAdapter extends RecyclerView.Adapter<GameViewHolder> {
        private List<Game> mGames = Lists.newArrayList();

        public void setGames(List<Game> games) {
            mGames = games;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.game_list_item, parent, false);
            return new GameViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
            final Game g = mGames.get(position);
            bindGame(holder, g);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), getGameActivityClass());
                    intent.putExtra(Game.ID_TAG, g.getId());
                    startActivity(intent);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(50);
                    showDeleteDialog(g);
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return mGames.size();
        }
    }

    protected abstract void bindGame(GameViewHolder holder, Game game);
    protected abstract Class<?> getGameActivityClass();

    protected static class GameViewHolder extends RecyclerView.ViewHolder {
        TextView time;
        TextView progress;
        TextView whenStarted;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.time);
            progress = itemView.findViewById(R.id.progress);
            whenStarted = itemView.findViewById(R.id.when_started);
        }
    }

    private void showDeleteDialog(final Game game) {
        new AlertDialog.Builder(getActivity())
                .setCancelable(true)
                .setTitle(R.string.delete)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteGame(game);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
}
