package com.antsapps.triples;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.GameReconstructor;
import com.antsapps.triples.backend.TripleAnalysis;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;

public class GameAnalysisActivity extends BaseTriplesActivity {

  public static final String GAME_ID = "game_id";
  public static final String GAME_TYPE = "game_type";

  private List<TripleAnalysis> mAnalysis;
  private boolean mIsDailyGame;

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
    mIsDailyGame = "Daily".equalsIgnoreCase(gameType);

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

    // Info button click handlers
    ImageButton availableTriplesInfo = findViewById(R.id.available_triples_info_button);
    availableTriplesInfo.setOnClickListener(
        v ->
            showInfoDialog(
                getString(R.string.analysis_available_triples),
                getString(R.string.analysis_available_triples_tooltip)));

    ImageButton sameDiffInfo = findViewById(R.id.same_diff_info_button);
    sameDiffInfo.setOnClickListener(
        v ->
            showInfoDialog(
                getString(R.string.analysis_same_diff_preference),
                getString(R.string.analysis_same_diff_tooltip)));

    ImageButton propertyBiasInfo = findViewById(R.id.property_bias_info_button);
    propertyBiasInfo.setOnClickListener(
        v ->
            showInfoDialog(
                getString(R.string.analysis_property_sameness_bias),
                getString(R.string.analysis_property_bias_tooltip)));

    // For daily games, show a single "View Board" button instead of per-row links
    View replaySubtitle = findViewById(R.id.replay_subtitle);
    MaterialButton viewBoardButton = findViewById(R.id.view_board_button);
    if (mIsDailyGame && !mAnalysis.isEmpty()) {
      replaySubtitle.setVisibility(View.GONE);
      viewBoardButton.setVisibility(View.VISIBLE);
      TripleAnalysis firstStep = mAnalysis.get(0);
      viewBoardButton.setOnClickListener(
          v -> {
            Intent intent = new Intent(GameAnalysisActivity.this, BoardHistoryActivity.class);
            BoardHistoryActivity.sAnalysis = firstStep;
            BoardHistoryActivity.sStep = 1;
            startActivity(intent);
          });
    }
  }

  private void showInfoDialog(String title, String message) {
    new AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(android.R.string.ok, null)
        .show();
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

    // === Available Valid Triples table ===
    // Group steps by the number of available options at that step
    TreeMap<Integer, List<TripleAnalysis>> byOptions = new TreeMap<>();
    long totalDuration = 0;
    long totalOptions = 0;
    for (TripleAnalysis a : mAnalysis) {
      int opts = a.allAvailableTriples.size();
      byOptions.computeIfAbsent(opts, k -> new ArrayList<>()).add(a);
      totalDuration += a.duration;
      totalOptions += opts;
    }

    LinearLayout availableContainer = findViewById(R.id.available_triples_container);
    addTableHeader(
        availableContainer,
        getString(R.string.analysis_col_options),
        getString(R.string.analysis_col_steps),
        getString(R.string.analysis_col_avg_time));

    for (TreeMap.Entry<Integer, List<TripleAnalysis>> entry : byOptions.entrySet()) {
      int opts = entry.getKey();
      List<TripleAnalysis> steps = entry.getValue();
      long sumDuration = 0;
      for (TripleAnalysis a : steps) sumDuration += a.duration;
      long avgDur = sumDuration / steps.size();
      addTableRow(
          availableContainer,
          String.valueOf(opts),
          String.valueOf(steps.size()),
          formatDuration(avgDur));
    }

    // Total row: avg options, total steps, overall avg time
    addTableRowBold(
        availableContainer,
        String.format(Locale.getDefault(), "%.1f avg", (double) totalOptions / total),
        total + " total",
        formatDuration(totalDuration / total));

    // === Same/Diff Preference table ===
    // Categories: diffCount 1 (3 same 1 diff), 2 (2 same 2 diff), 3 (1 same 3 diff), 4 (all diff)
    int[] foundByDiff = new int[5];
    long[] timeByDiff = new long[5];
    int[] availByDiff = new int[5];

    for (TripleAnalysis a : mAnalysis) {
      int diff = a.getNumDifferentProperties();
      if (diff >= 1 && diff <= 4) {
        foundByDiff[diff]++;
        timeByDiff[diff] += a.duration;
      }
      for (Set<Card> triple : a.allAvailableTriples) {
        int d = TripleAnalysis.getNumDifferentProperties(triple);
        if (d >= 1 && d <= 4) availByDiff[d]++;
      }
    }

    int totalAvail = 0;
    for (int d = 1; d <= 4; d++) totalAvail += availByDiff[d];

    LinearLayout sameDiffContainer = findViewById(R.id.same_diff_container);
    addTableHeader(
        sameDiffContainer,
        getString(R.string.analysis_col_triple_type),
        getString(R.string.analysis_col_chosen),
        getString(R.string.analysis_col_available),
        getString(R.string.analysis_col_avg_time));

    String[] diffLabels = {"", "3 same 1 diff", "2 same 2 diff", "1 same 3 diff", "All diff"};
    for (int d = 1; d <= 4; d++) {
      int found = foundByDiff[d];
      int avail = availByDiff[d];
      String chosenStr =
          String.format(Locale.getDefault(), "%d%% (%d)", found * 100 / total, found);
      String availStr =
          totalAvail > 0
              ? String.format(Locale.getDefault(), "%d%%", avail * 100 / totalAvail)
              : "0%";
      String timeStr = found > 0 ? formatDuration(timeByDiff[d] / found) : "-";
      addTableRow(sameDiffContainer, diffLabels[d], chosenStr, availStr, timeStr);
    }

    // === Property Sameness Bias table ===
    Card.PropertyType[] props = {
      Card.PropertyType.NUMBER,
      Card.PropertyType.SHAPE,
      Card.PropertyType.PATTERN,
      Card.PropertyType.COLOR
    };
    String[] propLabels = {
      getString(R.string.number),
      getString(R.string.shape),
      getString(R.string.pattern),
      getString(R.string.colour)
    };

    // Precompute total available triple-steps (same for all properties)
    int totalAvailTriples = 0;
    for (TripleAnalysis a : mAnalysis) totalAvailTriples += a.allAvailableTriples.size();

    LinearLayout propertyBiasContainer = findViewById(R.id.property_bias_container);
    addTableHeader(
        propertyBiasContainer,
        getString(R.string.analysis_col_property),
        getString(R.string.analysis_col_chosen),
        getString(R.string.analysis_col_available),
        getString(R.string.analysis_col_avg_time));

    for (int p = 0; p < 4; p++) {
      int foundSame = 0;
      int availSame = 0;
      long timeSame = 0;

      for (TripleAnalysis a : mAnalysis) {
        if (a.isPropertySame(props[p])) {
          foundSame++;
          timeSame += a.duration;
        }
        for (Set<Card> triple : a.allAvailableTriples) {
          if (isPropertySameInTriple(triple, props[p])) availSame++;
        }
      }

      String chosenStr =
          String.format(Locale.getDefault(), "%d%% (%d)", foundSame * 100 / total, foundSame);
      String availStr =
          totalAvailTriples > 0
              ? String.format(Locale.getDefault(), "%d%%", availSame * 100 / totalAvailTriples)
              : "0%";
      String timeStr = foundSame > 0 ? formatDuration(timeSame / foundSame) : "-";
      addTableRow(propertyBiasContainer, propLabels[p], chosenStr, availStr, timeStr);
    }
  }

  private static boolean isPropertySameInTriple(Set<Card> triple, Card.PropertyType type) {
    Card[] cards = triple.toArray(new Card[0]);
    return cards[0].getValue(type) == cards[1].getValue(type)
        && cards[1].getValue(type) == cards[2].getValue(type);
  }

  /** Format duration in milliseconds as [m:]ss.ss[s] */
  private String formatDuration(long ms) {
    long totalHundredths = ms / 10;
    long minutes = totalHundredths / 6000;
    long hundredths = totalHundredths % 6000;
    if (minutes > 0) {
      return String.format(Locale.getDefault(), "%d:%05.2f", minutes, hundredths / 100.0);
    } else {
      return String.format(Locale.getDefault(), "%.2fs", hundredths / 100.0);
    }
  }

  // --- Table building helpers ---

  private void addTableHeader(LinearLayout container, String... cols) {
    LinearLayout row = createTableRow(container.getContext());
    for (String col : cols) {
      TextView tv = createTableCell(container.getContext(), col, true);
      row.addView(tv);
    }
    container.addView(row);

    // Divider under header
    View divider = new View(container.getContext());
    divider.setLayoutParams(
        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1)));
    TypedValue value = new TypedValue();
    getTheme().resolveAttribute(android.R.attr.listDivider, value, true);
    divider.setBackgroundResource(value.resourceId);
    container.addView(divider);
  }

  private void addTableRow(LinearLayout container, String... cols) {
    LinearLayout row = createTableRow(container.getContext());
    for (String col : cols) {
      row.addView(createTableCell(container.getContext(), col, false));
    }
    container.addView(row);
  }

  private void addTableRowBold(LinearLayout container, String... cols) {
    // Divider above total row
    View divider = new View(container.getContext());
    divider.setLayoutParams(
        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1)));
    TypedValue value = new TypedValue();
    getTheme().resolveAttribute(android.R.attr.listDivider, value, true);
    divider.setBackgroundResource(value.resourceId);
    container.addView(divider);

    LinearLayout row = createTableRow(container.getContext());
    for (String col : cols) {
      TextView tv = createTableCell(container.getContext(), col, true);
      row.addView(tv);
    }
    container.addView(row);
  }

  private LinearLayout createTableRow(android.content.Context context) {
    LinearLayout row = new LinearLayout(context);
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setLayoutParams(
        new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    return row;
  }

  private TextView createTableCell(android.content.Context context, String text, boolean bold) {
    TextView tv = new TextView(context);
    tv.setText(text);
    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
    tv.setGravity(Gravity.CENTER);
    tv.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
    tv.setLayoutParams(
        new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
    if (bold) tv.setTypeface(null, android.graphics.Typeface.BOLD);
    return tv;
  }

  private int dpToPx(float dp) {
    return (int)
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
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
      holder.durationText.setText(formatDuration(analysis.duration));
      holder.optionsText.setText(
          getString(R.string.analysis_opts_format, analysis.allAvailableTriples.size()));

      if (!mIsDailyGame) {
        holder.itemView.setOnClickListener(
            v -> {
              Intent intent = new Intent(GameAnalysisActivity.this, BoardHistoryActivity.class);
              BoardHistoryActivity.sAnalysis = analysis;
              BoardHistoryActivity.sStep = position + 1;
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
