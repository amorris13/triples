package com.antsapps.triples.stats;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import com.antsapps.triples.R;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.TripleAnalysis;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;

public class TripleAnalysisSummaryView extends LinearLayout {

  private LinearLayout mAvailableContainer;
  private LinearLayout mSameDiffContainer;
  private LinearLayout mPropertyBiasContainer;

  public TripleAnalysisSummaryView(Context context) {
    this(context, null);
  }

  public TripleAnalysisSummaryView(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public TripleAnalysisSummaryView(
      Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    setOrientation(VERTICAL);
    LayoutInflater.from(context).inflate(R.layout.triple_analysis_summary, this, true);

    mAvailableContainer = findViewById(R.id.available_triples_container);
    mSameDiffContainer = findViewById(R.id.same_diff_container);
    mPropertyBiasContainer = findViewById(R.id.property_bias_container);

    setupInfoButton(
        R.id.available_triples_info_button,
        R.string.analysis_available_triples,
        R.string.analysis_available_triples_tooltip);
    setupInfoButton(
        R.id.same_diff_info_button,
        R.string.analysis_same_diff_preference,
        R.string.analysis_same_diff_tooltip);
    setupInfoButton(
        R.id.property_bias_info_button,
        R.string.analysis_property_sameness_bias,
        R.string.analysis_property_bias_tooltip);
  }

  private void setupInfoButton(int buttonId, int titleResId, int messageResId) {
    findViewById(buttonId)
        .setOnClickListener(
            v ->
                showInfoDialog(
                    getContext().getString(titleResId), getContext().getString(messageResId)));
  }

  private void showInfoDialog(String title, String message) {
    new AlertDialog.Builder(getContext())
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(android.R.string.ok, null)
        .show();
  }

  public void setAnalysis(List<TripleAnalysis> analysis) {
    mAvailableContainer.removeAllViews();
    mSameDiffContainer.removeAllViews();
    mPropertyBiasContainer.removeAllViews();

    int total = analysis.size();
    if (total == 0) return;

    // === Available Valid Triples table ===
    TreeMap<Integer, List<TripleAnalysis>> byOptions = new TreeMap<>();
    long totalDuration = 0;
    long totalOptions = 0;
    for (TripleAnalysis a : analysis) {
      int opts = a.allAvailableTriples.size();
      byOptions.computeIfAbsent(opts, k -> new ArrayList<>()).add(a);
      totalDuration += a.duration;
      totalOptions += opts;
    }

    addTableHeader(
        mAvailableContainer,
        getContext().getString(R.string.analysis_col_options),
        getContext().getString(R.string.analysis_col_steps),
        getContext().getString(R.string.analysis_col_avg_time));

    for (TreeMap.Entry<Integer, List<TripleAnalysis>> entry : byOptions.entrySet()) {
      int opts = entry.getKey();
      List<TripleAnalysis> steps = entry.getValue();
      long sumDuration = 0;
      for (TripleAnalysis a : steps) sumDuration += a.duration;
      long avgDur = sumDuration / steps.size();
      addTableRow(
          mAvailableContainer,
          String.valueOf(opts),
          String.valueOf(steps.size()),
          formatDuration(avgDur));
    }

    addTableRowBold(
        mAvailableContainer,
        String.format(Locale.getDefault(), "%.1f avg", (double) totalOptions / total),
        total + " total",
        formatDuration(totalDuration / total));

    // === Same/Diff Preference table ===
    int[] foundByDiff = new int[5];
    long[] timeByDiff = new long[5];
    int[] availByDiff = new int[5];

    for (TripleAnalysis a : analysis) {
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

    addTableHeader(
        mSameDiffContainer,
        getContext().getString(R.string.analysis_col_triple_type),
        getContext().getString(R.string.analysis_col_chosen),
        getContext().getString(R.string.analysis_col_available),
        getContext().getString(R.string.analysis_col_avg_time));

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
      addTableRow(mSameDiffContainer, diffLabels[d], chosenStr, availStr, timeStr);
    }

    // === Property Sameness Bias table ===
    Card.PropertyType[] props = {
      Card.PropertyType.NUMBER,
      Card.PropertyType.SHAPE,
      Card.PropertyType.PATTERN,
      Card.PropertyType.COLOR
    };
    String[] propLabels = {
      getContext().getString(R.string.number),
      getContext().getString(R.string.shape),
      getContext().getString(R.string.pattern),
      getContext().getString(R.string.colour)
    };

    int totalAvailTriples = 0;
    for (TripleAnalysis a : analysis) totalAvailTriples += a.allAvailableTriples.size();

    addTableHeader(
        mPropertyBiasContainer,
        getContext().getString(R.string.analysis_col_property),
        getContext().getString(R.string.analysis_col_chosen),
        getContext().getString(R.string.analysis_col_available),
        getContext().getString(R.string.analysis_col_avg_time));

    for (int p = 0; p < 4; p++) {
      int foundSame = 0;
      int availSame = 0;
      long timeSame = 0;

      for (TripleAnalysis a : analysis) {
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
      addTableRow(mPropertyBiasContainer, propLabels[p], chosenStr, availStr, timeStr);
    }
  }

  private static boolean isPropertySameInTriple(Set<Card> triple, Card.PropertyType type) {
    Card[] cards = triple.toArray(new Card[0]);
    return cards[0].getValue(type) == cards[1].getValue(type)
        && cards[1].getValue(type) == cards[2].getValue(type);
  }

  /** Format duration in milliseconds as [m:]ss.ss[s] */
  public static String formatDuration(long ms) {
    long totalHundredths = ms / 10;
    long minutes = totalHundredths / 6000;
    long hundredths = totalHundredths % 6000;
    if (minutes > 0) {
      return String.format(Locale.getDefault(), "%d:%05.2f", minutes, hundredths / 100.0);
    } else {
      return String.format(Locale.getDefault(), "%.2fs", hundredths / 100.0);
    }
  }

  private void addTableHeader(LinearLayout container, String... cols) {
    LinearLayout row = createTableRow(container.getContext());
    for (String col : cols) {
      TextView tv = createTableCell(container.getContext(), col, true);
      row.addView(tv);
    }
    container.addView(row);

    View divider = new View(container.getContext());
    divider.setLayoutParams(
        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1)));
    TypedValue value = new TypedValue();
    getContext().getTheme().resolveAttribute(android.R.attr.listDivider, value, true);
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
    View divider = new View(container.getContext());
    divider.setLayoutParams(
        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1)));
    TypedValue value = new TypedValue();
    getContext().getTheme().resolveAttribute(android.R.attr.listDivider, value, true);
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
}
