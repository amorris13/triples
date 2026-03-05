package com.antsapps.triples;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.DailyGame;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class DailyStatisticsFragment extends Fragment {

  private Application mApplication;
  private List<DailyGame> mCompletedGames;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    mApplication = Application.getInstance(getActivity());
    View view = inflater.inflate(R.layout.daily_stats_fragment, container, false);

    CalendarView calendarView = view.findViewById(R.id.calendar_view);
    ListView listView = view.findViewById(R.id.daily_games_list);

    mCompletedGames = new ArrayList<>();
    for (DailyGame game : mApplication.getCompletedDailyGames()) {
      mCompletedGames.add(game);
    }
    Collections.sort(mCompletedGames, (g1, g2) -> g2.getDateStarted().compareTo(g1.getDateStarted()));

    DailyGamesAdapter adapter = new DailyGamesAdapter(getActivity(), mCompletedGames);
    listView.setAdapter(adapter);

    calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
      Calendar cal = Calendar.getInstance();
      cal.set(year, month, dayOfMonth, 0, 0, 0);
      cal.set(Calendar.MILLISECOND, 0);
      long selectedDaySeed = cal.getTimeInMillis();

      int position = -1;
      for (int i = 0; i < mCompletedGames.size(); i++) {
        Calendar gameCal = Calendar.getInstance();
        gameCal.setTime(mCompletedGames.get(i).getDateStarted());
        gameCal.set(Calendar.HOUR_OF_DAY, 0);
        gameCal.set(Calendar.MINUTE, 0);
        gameCal.set(Calendar.SECOND, 0);
        gameCal.set(Calendar.MILLISECOND, 0);
        if (gameCal.getTimeInMillis() == selectedDaySeed) {
          position = i;
          break;
        }
      }
      if (position != -1) {
        listView.setSelection(position);
      }
    });

    return view;
  }

  private static class DailyGamesAdapter extends BaseAdapter {
    private final Context mContext;
    private final List<DailyGame> mGames;
    private final DateFormat mDateFormat = DateFormat.getDateInstance();

    DailyGamesAdapter(Context context, List<DailyGame> games) {
      mContext = context;
      mGames = games;
    }

    @Override
    public int getCount() {
      return mGames.size();
    }

    @Override
    public Object getItem(int position) {
      return mGames.get(position);
    }

    @Override
    public long getItemId(int position) {
      return mGames.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      if (convertView == null) {
        convertView = LayoutInflater.from(mContext).inflate(R.layout.stats_game_list_item, parent, false);
      }
      DailyGame game = mGames.get(position);
      TextView dateTv = convertView.findViewById(R.id.date_played);
      TextView timeTv = convertView.findViewById(R.id.result);

      dateTv.setText(mDateFormat.format(game.getDateStarted()));

      long seconds = game.getTimeElapsed() / 1000;
      timeTv.setText(String.format("%d:%02d", seconds / 60, seconds % 60));

      return convertView;
    }
  }
}
