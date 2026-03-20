package com.antsapps.triples;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.TripleAnalysis;
import com.antsapps.triples.stats.TripleAnalysisSummaryView;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class GameAnalysisActivity extends BaseTriplesActivity {

  public static final String GAME_ID = "game_id";
  public static final String GAME_TYPE = "game_type";

  private List<TripleAnalysis> mAnalysis;
  private boolean mIsDailyGame;
  private long mGameId;
  private String mGameType;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.analysis);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setTitle(R.string.analysis_title);
    }

    mGameId = getIntent().getLongExtra(GAME_ID, -1);
    mGameType = getIntent().getStringExtra(GAME_TYPE);
    mIsDailyGame = "Daily".equalsIgnoreCase(mGameType);

    Game game = getGame(mGameId, mGameType);
    if (game == null) {
      finish();
      return;
    }

    mAnalysis = game.reconstruct();
    TripleAnalysisSummaryView summaryView = findViewById(R.id.analysis_summary);
    summaryView.setAnalysis(mAnalysis);

    RecyclerView recyclerView = findViewById(R.id.triples_list);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    recyclerView.setAdapter(new AnalysisAdapter(mAnalysis));

    // For daily games, show a single "View Board" button instead of per-row links
    View replaySubtitle = findViewById(R.id.replay_subtitle);
    MaterialButton viewBoardButton = findViewById(R.id.view_board_button);
    if (mIsDailyGame && !mAnalysis.isEmpty()) {
      replaySubtitle.setVisibility(View.GONE);
      viewBoardButton.setVisibility(View.VISIBLE);
      viewBoardButton.setOnClickListener(
          v -> {
            Intent intent = new Intent(GameAnalysisActivity.this, BoardHistoryActivity.class);
            intent.putExtra(BoardHistoryActivity.EXTRA_GAME_ID, mGameId);
            intent.putExtra(BoardHistoryActivity.EXTRA_GAME_TYPE, mGameType);
            intent.putExtra(BoardHistoryActivity.EXTRA_INITIAL_STEP, 1);
            startActivity(intent);
          });
    }
  }

  private Game getGame(long id, String type) {
    Application app = Application.getInstance(this);
    if ("Classic".equalsIgnoreCase(type)) return app.getClassicGame(id);
    if ("Arcade".equalsIgnoreCase(type)) return app.getArcadeGame(id);
    if ("Daily".equalsIgnoreCase(type)) return app.getDailyGame(id);
    return null;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private class AnalysisAdapter extends RecyclerView.Adapter<AnalysisViewHolder> {
    private final List<TripleAnalysis> mData;

    AnalysisAdapter(List<TripleAnalysis> data) {
      mData = data;
    }

    @NonNull
    @Override
    public AnalysisViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View v =
          LayoutInflater.from(parent.getContext()).inflate(R.layout.analysis_item, parent, false);
      return new AnalysisViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AnalysisViewHolder holder, int position) {
      TripleAnalysis analysis = mData.get(position);

      holder.explanationView.setShowHeader(false);
      holder.explanationView.setShowTicks(false);
      holder.explanationView.setShowOverallConclusion(false);
      holder.explanationView.setShowTypeSummary(true);
      holder.explanationView.setCards(analysis.foundTriple);

      holder.stepText.setText(getString(R.string.analysis_step_format, position + 1));
      holder.durationText.setText(TripleAnalysisSummaryView.formatDuration(analysis.duration));
      holder.optionsText.setText(
          getString(R.string.analysis_opts_format, analysis.allAvailableTriples.size()));

      if (!mIsDailyGame) {
        holder.itemView.setOnClickListener(
            v -> {
              Intent intent = new Intent(GameAnalysisActivity.this, BoardHistoryActivity.class);
              intent.putExtra(BoardHistoryActivity.EXTRA_GAME_ID, mGameId);
              intent.putExtra(BoardHistoryActivity.EXTRA_GAME_TYPE, mGameType);
              intent.putExtra(BoardHistoryActivity.EXTRA_INITIAL_STEP, position + 1);
              startActivity(intent);
            });
      } else {
        holder.itemView.setClickable(false);
      }
    }

    @Override
    public int getItemCount() {
      return mData.size();
    }
  }

  private static class AnalysisViewHolder extends RecyclerView.ViewHolder {
    TextView stepText, durationText, optionsText;
    com.antsapps.triples.views.TripleExplanationView explanationView;

    AnalysisViewHolder(View v) {
      super(v);
      stepText = v.findViewById(R.id.step_text);
      durationText = v.findViewById(R.id.duration_text);
      optionsText = v.findViewById(R.id.options_text);
      explanationView = v.findViewById(R.id.triple_explanation);
    }
  }
}
