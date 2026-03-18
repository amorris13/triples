package com.antsapps.triples;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.GameReconstructor;
import com.antsapps.triples.backend.TripleAnalysis;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GameAnalysisActivity extends BaseTriplesActivity {

  public static final String GAME_ID = "game_id";
  public static final String GAME_TYPE = "game_type";

  private List<TripleAnalysis> mAnalysis;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
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

    // Available Valid Triples grouping
    Map<Integer, Integer> optionsCount = new HashMap<>();
    Map<Integer, Long> optionsDuration = new HashMap<>();
    int totalOptionsAcrossSteps = 0;
    long totalDurationAcrossSteps = 0;

    // Triple Types: 0=all same, 1=3s1d, 2=2s2d, 3=1s3d, 4=all diff
    int[] playerTypeCounts = new int[5];
    int[] availTypeCounts = new int[5];
    long[] playerTypeDurations = new long[5];
    int totalAvailableTriplesCount = 0;

    // Property Bias
    int[] playerPropSameCounts = new int[4];
    int[] availPropSameCounts = new int[4];
    long[] playerPropSameDurations = new long[4];

    Card.PropertyType[] props = Card.PropertyType.values();

    for (TripleAnalysis analysis : mAnalysis) {
      int options = analysis.allAvailableTriples.size();
      optionsCount.put(options, optionsCount.getOrDefault(options, 0) + 1);
      optionsDuration.put(options, optionsDuration.getOrDefault(options, 0L) + analysis.duration);
      totalOptionsAcrossSteps += options;
      totalDurationAcrossSteps += analysis.duration;

      int playerDiffCount = TripleAnalysis.getNumDifferentProperties(analysis.foundTriple);
      playerTypeCounts[playerDiffCount]++;
      playerTypeDurations[playerDiffCount] += analysis.duration;

      for (int i = 0; i < 4; i++) {
        if (analysis.isPropertySame(props[i])) {
          playerPropSameCounts[i]++;
          playerPropSameDurations[i] += analysis.duration;
        }
      }

      for (java.util.Set<Card> available : analysis.allAvailableTriples) {
        totalAvailableTriplesCount++;
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

    populateAvailTriplesTable(
        optionsCount, optionsDuration, total, totalOptionsAcrossSteps, totalDurationAcrossSteps);

    // Same/Diff Preference (requested order: 1s3d, 2s2d, 3s1d, all diff)
    updateSummaryRow(
        R.id.type_1s3d_player,
        R.id.type_1s3d_avail,
        R.id.type_1s3d_time,
        playerTypeCounts[3],
        availTypeCounts[3],
        playerTypeDurations[3],
        total,
        totalAvailableTriplesCount);
    updateSummaryRow(
        R.id.type_2s2d_player,
        R.id.type_2s2d_avail,
        R.id.type_2s2d_time,
        playerTypeCounts[2],
        availTypeCounts[2],
        playerTypeDurations[2],
        total,
        totalAvailableTriplesCount);
    updateSummaryRow(
        R.id.type_3s1d_player,
        R.id.type_3s1d_avail,
        R.id.type_3s1d_time,
        playerTypeCounts[1],
        availTypeCounts[1],
        playerTypeDurations[1],
        total,
        totalAvailableTriplesCount);
    updateSummaryRow(
        R.id.type_all_diff_player,
        R.id.type_all_diff_avail,
        R.id.type_all_diff_time,
        playerTypeCounts[4],
        availTypeCounts[4],
        playerTypeDurations[4],
        total,
        totalAvailableTriplesCount);

    // Property Bias
    updateSummaryRow(
        R.id.bias_num_player,
        R.id.bias_num_avail,
        R.id.bias_num_time,
        playerPropSameCounts[0],
        availPropSameCounts[0],
        playerPropSameDurations[0],
        total,
        totalAvailableTriplesCount);
    updateSummaryRow(
        R.id.bias_shape_player,
        R.id.bias_shape_avail,
        R.id.bias_shape_time,
        playerPropSameCounts[1],
        availPropSameCounts[1],
        playerPropSameDurations[1],
        total,
        totalAvailableTriplesCount);
    updateSummaryRow(
        R.id.bias_pattern_player,
        R.id.bias_pattern_avail,
        R.id.bias_pattern_time,
        playerPropSameCounts[2],
        availPropSameCounts[2],
        playerPropSameDurations[2],
        total,
        totalAvailableTriplesCount);
    updateSummaryRow(
        R.id.bias_color_player,
        R.id.bias_color_avail,
        R.id.bias_color_time,
        playerPropSameCounts[3],
        availPropSameCounts[3],
        playerPropSameDurations[3],
        total,
        totalAvailableTriplesCount);
  }

  private void populateAvailTriplesTable(
      Map<Integer, Integer> counts,
      Map<Integer, Long> durations,
      int totalSteps,
      int totalOptions,
      long totalDuration) {
    TableLayout table = findViewById(R.id.avail_triples_table);
    List<Integer> sortedOptions = new ArrayList<>(counts.keySet());
    Collections.sort(sortedOptions);

    for (int options : sortedOptions) {
      int steps = counts.get(options);
      long duration = durations.get(options);
      table.addView(
          createAvailRow(
              String.valueOf(options), String.valueOf(steps), duration / (float) steps / 1000f));
    }

    // Total row
    View divider = new View(this);
    divider.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, 2));
    TypedValue outValue = new TypedValue();
    if (getTheme()
        .resolveAttribute(com.google.android.material.R.attr.colorOutline, outValue, true)) {
      divider.setBackgroundColor(outValue.data);
    } else {
      divider.setBackgroundColor(0xFF888888);
    }
    table.addView(divider);

    // Final Average Row (combining options count and find time)
    TableRow finalAvgRow = new TableRow(this);
    finalAvgRow.setPadding(4, 8, 4, 8);
    finalAvgRow.addView(createCell(getString(R.string.analysis_label_averages)));
    finalAvgRow.addView(
        createCell(
            getString(R.string.analysis_cell_options_format, (float) totalOptions / totalSteps)));
    finalAvgRow.addView(
        createCell(
            getString(
                R.string.analysis_cell_time_format, totalDuration / (float) totalSteps / 1000f)));
    table.addView(finalAvgRow);
  }

  private TableRow createAvailRow(String opt, String steps, float avgTime) {
    TableRow row = new TableRow(this);
    row.setPadding(4, 4, 4, 4);
    row.addView(createCell(opt));
    row.addView(createCell(steps));
    row.addView(createCell(getString(R.string.analysis_cell_time_format, avgTime)));
    return row;
  }

  private TextView createCell(String text) {
    TextView tv = new TextView(this);
    tv.setText(text);
    tv.setGravity(Gravity.CENTER);
    return tv;
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

  public static String formatDurationCompact(long elapsedMillis) {
    long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis);
    long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis) % 60;
    long hundredths = (elapsedMillis % 1000) / 10;
    if (minutes == 0) {
      return String.format(Locale.US, "%d.%02d", seconds, hundredths);
    } else {
      return String.format(Locale.US, "%d:%02d.%02d", minutes, seconds, hundredths);
    }
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
      holder.durationText.setText(formatDurationCompact(analysis.duration));
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
