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
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.GameReconstructor;
import com.antsapps.triples.backend.TripleAnalysis;
import java.util.List;

public class GameAnalysisActivity extends BaseTriplesActivity {

  public static final String GAME_ID = "game_id";
  public static final String GAME_TYPE = "game_type";

  private List<TripleAnalysis> mAnalysis;

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

    long gameId = getIntent().getLongExtra(GAME_ID, -1);
    String gameType = getIntent().getStringExtra(GAME_TYPE);

    Game game = getGame(gameId, gameType);
    if (game == null) {
      finish();
      return;
    }

    mAnalysis = GameReconstructor.reconstruct(game);
    updateSummary();

    RecyclerView recyclerView = findViewById(R.id.triples_list);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    recyclerView.setAdapter(new AnalysisAdapter(mAnalysis));
  }

  private Game getGame(long id, String type) {
    Application app = Application.getInstance(this);
    if ("Classic".equalsIgnoreCase(type)) return app.getClassicGame(id);
    if ("Arcade".equalsIgnoreCase(type)) return app.getArcadeGame(id);
    if ("Daily".equalsIgnoreCase(type)) return app.getDailyGame(id);
    return null;
  }

  private void updateSummary() {
    int total = mAnalysis.size();
    if (total == 0) return;

    // Triple Types: 0=all same, 1=1s3d, 2=2s2d, 3=3s1d, 4=all diff
    int[] playerTypeCounts = new int[5];
    int[] availTypeCounts = new int[5];
    long[] playerTypeDurations = new long[5];
    int totalAvailable = 0;

    // Property Bias
    int[] playerPropSameCounts = new int[4];
    int[] availPropSameCounts = new int[4];
    long[] playerPropSameDurations = new long[4];

    Card.PropertyType[] props = Card.PropertyType.values();

    for (TripleAnalysis analysis : mAnalysis) {
      int playerDiffCount = analysis.getNumDifferentProperties();
      playerTypeCounts[playerDiffCount]++;
      playerTypeDurations[playerDiffCount] += analysis.duration;

      for (int i = 0; i < 4; i++) {
        if (analysis.isPropertySame(props[i])) {
          playerPropSameCounts[i]++;
          playerPropSameDurations[i] += analysis.duration;
        }
      }

      for (java.util.Set<Card> available : analysis.allAvailableTriples) {
        totalAvailable++;
        int availDiffCount = TripleAnalysis.getNumDifferentProperties(available);
        availTypeCounts[availDiffCount]++;

        Card[] cards = available.toArray(new Card[0]);
        for (int i = 0; i < 4; i++) {
          if (TripleAnalysis.isPropertySame(
              cards[0].getValue(props[i]),
              cards[1].getValue(props[i]),
              cards[2].getValue(props[i]))) {
            availPropSameCounts[i]++;
          }
        }
      }
    }

    // Populate Triple Types (User requested: 1s3d, 2s2d, 3s1d, all diff)
    // Map: 1 diff = 3 same, 1 diff (3s1d); 2 diff = 2s2d; 3 diff = 1s3d; 4 diff = all diff
    updateSummaryRow(
        R.id.type_1s3d_player,
        R.id.type_1s3d_avail,
        R.id.type_1s3d_time,
        playerTypeCounts[3],
        availTypeCounts[3],
        playerTypeDurations[3],
        total,
        totalAvailable);
    updateSummaryRow(
        R.id.type_2s2d_player,
        R.id.type_2s2d_avail,
        R.id.type_2s2d_time,
        playerTypeCounts[2],
        availTypeCounts[2],
        playerTypeDurations[2],
        total,
        totalAvailable);
    updateSummaryRow(
        R.id.type_3s1d_player,
        R.id.type_3s1d_avail,
        R.id.type_3s1d_time,
        playerTypeCounts[1],
        availTypeCounts[1],
        playerTypeDurations[1],
        total,
        totalAvailable);
    updateSummaryRow(
        R.id.type_all_diff_player,
        R.id.type_all_diff_avail,
        R.id.type_all_diff_time,
        playerTypeCounts[4],
        availTypeCounts[4],
        playerTypeDurations[4],
        total,
        totalAvailable);

    // Property Bias
    updateSummaryRow(
        R.id.bias_num_player,
        R.id.bias_num_avail,
        R.id.bias_num_time,
        playerPropSameCounts[0],
        availPropSameCounts[0],
        playerPropSameDurations[0],
        total,
        totalAvailable);
    updateSummaryRow(
        R.id.bias_shape_player,
        R.id.bias_shape_avail,
        R.id.bias_shape_time,
        playerPropSameCounts[1],
        availPropSameCounts[1],
        playerPropSameDurations[1],
        total,
        totalAvailable);
    updateSummaryRow(
        R.id.bias_pattern_player,
        R.id.bias_pattern_avail,
        R.id.bias_pattern_time,
        playerPropSameCounts[2],
        availPropSameCounts[2],
        playerPropSameDurations[2],
        total,
        totalAvailable);
    updateSummaryRow(
        R.id.bias_color_player,
        R.id.bias_color_avail,
        R.id.bias_color_time,
        playerPropSameCounts[3],
        availPropSameCounts[3],
        playerPropSameDurations[3],
        total,
        totalAvailable);

    // Overall Averages
    long totalDuration = 0;
    for (TripleAnalysis analysis : mAnalysis) {
      totalDuration += analysis.duration;
    }
    ((TextView) findViewById(R.id.avg_find_time))
        .setText(
            getString(R.string.analysis_cell_time_format, totalDuration / (float) total / 1000f));
    ((TextView) findViewById(R.id.avg_options))
        .setText(getString(R.string.analysis_cell_options_format, totalAvailable / (float) total));
  }

  private void updateSummaryRow(
      int playerId,
      int availId,
      int timeId,
      int playerCount,
      int availCount,
      long totalDuration,
      int playerTotal,
      int availTotal) {
    ((TextView) findViewById(playerId))
        .setText(
            getString(
                R.string.analysis_cell_player_format,
                (playerCount * 100) / playerTotal,
                playerCount));
    ((TextView) findViewById(availId))
        .setText(
            getString(
                R.string.analysis_cell_percentage_format,
                availTotal > 0 ? (availCount * 100) / availTotal : 0));
    ((TextView) findViewById(timeId))
        .setText(
            getString(
                R.string.analysis_cell_time_format,
                playerCount > 0 ? (totalDuration / (float) playerCount) / 1000f : 0f));
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
      holder.stepText.setText(getString(R.string.analysis_step_format, position + 1));
      holder.durationText.setText(
          getString(R.string.analysis_duration_format, analysis.duration / 1000f));
      holder.optionsText.setText(
          getString(R.string.analysis_step_options_format, analysis.allAvailableTriples.size()));

      holder.explanationView.setShowTicks(false);
      holder.explanationView.setShowOverallConclusion(false);
      holder.explanationView.setShowHeader(false);
      holder.explanationView.setCards(analysis.foundTriple);

      holder.itemView.setOnClickListener(
          v -> {
            Intent intent = new Intent(GameAnalysisActivity.this, BoardHistoryActivity.class);
            BoardHistoryActivity.sAnalysis = analysis;
            BoardHistoryActivity.sStep = position + 1;
            startActivity(intent);
          });
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
