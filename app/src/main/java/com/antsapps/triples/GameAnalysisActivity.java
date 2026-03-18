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
import com.google.android.material.button.MaterialButton;
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
    int allSame = 0;
    int allDiff = 0;
    int mixed = 0;
    int totalAlternatives = 0;

    for (TripleAnalysis analysis : mAnalysis) {
      int diffCount = analysis.getNumDifferentProperties();
      if (diffCount == 0 || diffCount == 4) {
        if (diffCount == 4) allDiff++;
        else allSame++;
      } else {
        mixed++;
      }
      totalAlternatives += analysis.allAvailableTriples.size();
    }

    int total = mAnalysis.size();
    if (total > 0) {
      ((TextView) findViewById(R.id.bias_all_same))
          .setText(getString(R.string.analysis_all_same_bias, (allSame * 100) / total));
      ((TextView) findViewById(R.id.bias_all_diff))
          .setText(getString(R.string.analysis_all_diff_bias, (allDiff * 100) / total));
      ((TextView) findViewById(R.id.bias_mixed))
          .setText(getString(R.string.analysis_mixed_bias, (mixed * 100) / total));
      ((TextView) findViewById(R.id.avg_alternatives))
          .setText(
              getString(R.string.analysis_avg_alternatives, (float) totalAlternatives / total));

      int sameNum = 0, sameShape = 0, samePattern = 0, sameColor = 0;
      for (TripleAnalysis analysis : mAnalysis) {
        if (analysis.isPropertySame(Card.PropertyType.NUMBER)) sameNum++;
        if (analysis.isPropertySame(Card.PropertyType.SHAPE)) sameShape++;
        if (analysis.isPropertySame(Card.PropertyType.PATTERN)) samePattern++;
        if (analysis.isPropertySame(Card.PropertyType.COLOR)) sameColor++;
      }
      ((TextView) findViewById(R.id.bias_num))
          .setText(getString(R.string.number) + " " + (sameNum * 100 / total) + "% same");
      ((TextView) findViewById(R.id.bias_shape))
          .setText(getString(R.string.shape) + " " + (sameShape * 100 / total) + "% same");
      ((TextView) findViewById(R.id.bias_pattern))
          .setText(getString(R.string.pattern) + " " + (samePattern * 100 / total) + "% same");
      ((TextView) findViewById(R.id.bias_color))
          .setText(getString(R.string.colour) + " " + (sameColor * 100 / total) + "% same");
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
      holder.durationText.setText(
          getString(R.string.analysis_duration_format, analysis.duration / 1000f));

      holder.explanationView.setShowTicks(false);
      holder.explanationView.setShowOverallConclusion(false);
      holder.explanationView.setCards(analysis.foundTriple);
      holder.viewBoardButton.setText(
          getString(
              R.string.analysis_view_board_with_alternatives, analysis.allAvailableTriples.size()));

      holder.viewBoardButton.setOnClickListener(
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
    TextView stepText, durationText;
    com.antsapps.triples.views.TripleExplanationView explanationView;
    MaterialButton viewBoardButton;

    AnalysisViewHolder(View v) {
      super(v);
      stepText = v.findViewById(R.id.step_text);
      durationText = v.findViewById(R.id.duration_text);
      explanationView = v.findViewById(R.id.triple_explanation);
      viewBoardButton = v.findViewById(R.id.view_board_button);
    }
  }
}
