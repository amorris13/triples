package com.antsapps.triples;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.DatePeriod;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.NumGamesPeriod;
import com.antsapps.triples.backend.Period;
import com.antsapps.triples.backend.Statistics;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class StatisticsActivity extends Activity {

  private static final long MS_PER_DAY = (long) 24 * 60 * 60 * 1000;

  private static final Map<String, Period> PERIODS = Maps.newLinkedHashMap();
  private Spinner mSpinner;

  private TextView mNumberOfGames;
  private TextView mFastestTime;
  private TextView mAverageTime;

  private GraphView mGraphView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.stats);

    getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

    mGraphView = (GraphView) findViewById(R.id.graph);

    mNumberOfGames = (TextView) findViewById(R.id.number_completed);
    mFastestTime = (TextView) findViewById(R.id.fastest_time);
    mAverageTime = (TextView) findViewById(R.id.average_time);

    initSpinner();
    updatePeriod(PERIODS.get(getString(R.string.all_time)));
  }

  private void initSpinner() {
    mSpinner = (Spinner) findViewById(R.id.period_spinner);

    ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
        getBaseContext(), android.R.layout.simple_spinner_item);
    adapter
        .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    initPeriodsMap();
    for (String key : PERIODS.keySet()) {
      adapter.add(key);
    }
    mSpinner.setAdapter(adapter);

    mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int pos,
          long id) {
        String string = (String) parent.getItemAtPosition(pos);
        updatePeriod(PERIODS.get(string));
      }

      @Override
      public void onNothingSelected(AdapterView<?> arg0) {
      }
    });
  }

  private void initPeriodsMap() {
    PERIODS.put(getString(R.string.all_time), new Period() {
      @Override
      public List<Game> filter(Iterable<Game> games) {
        return Lists.newArrayList(games);
      }
    });
    PERIODS.put(
        getString(R.string.past_day),
        DatePeriod.fromTimePeriod(1 * MS_PER_DAY));
    PERIODS.put(
        getString(R.string.past_week),
        DatePeriod.fromTimePeriod(7 * MS_PER_DAY));
    PERIODS.put(
        getString(R.string.past_month),
        DatePeriod.fromTimePeriod(30 * MS_PER_DAY));
    PERIODS.put(
        getString(R.string.past_3_months),
        DatePeriod.fromTimePeriod(91 * MS_PER_DAY));
    PERIODS.put(
        getString(R.string.past_6_months),
        DatePeriod.fromTimePeriod(182 * MS_PER_DAY));
    PERIODS.put(
        getString(R.string.past_year),
        DatePeriod.fromTimePeriod(365 * MS_PER_DAY));
    PERIODS.put(getString(R.string.past_10_games), new NumGamesPeriod(10));
    PERIODS.put(getString(R.string.past_50_games), new NumGamesPeriod(50));
  }

  private void updatePeriod(Period period) {
    Statistics statistics = Application
        .getInstance(getApplicationContext()).getStatistics(period);
    mGraphView.setStatistics(statistics);

    int numGames = statistics.getNumGames();
    mNumberOfGames.setText(String.valueOf(numGames));
    mFastestTime.setText(numGames != 0 ? DateUtils
        .formatElapsedTime(TimeUnit.MILLISECONDS.toSeconds(statistics
            .getFastestTime())) : "-");
    mAverageTime.setText(numGames != 0 ? DateUtils
        .formatElapsedTime(TimeUnit.MILLISECONDS.toSeconds(statistics
            .getAverageTime())) : "-");
  }
}
