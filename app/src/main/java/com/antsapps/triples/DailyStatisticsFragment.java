package com.antsapps.triples;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.DailyGame;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.os.Bundle;

public class DailyStatisticsFragment extends Fragment {

  private Application mApplication;
  private List<DailyGame> mCompletedGames;
  private Calendar mCurrentMonth;
  private GridView mCalendarGrid;
  private TextView mMonthTitle;
  private TextView mCurrentStreakTv;
  private TextView mLongestStreakTv;
  private ListView mGamesListView;
  private DailyGamesAdapter mGamesAdapter;
  private Button mNextMonthBtn;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    mApplication = Application.getInstance(getActivity());
    View view = inflater.inflate(R.layout.daily_stats_fragment, container, false);

    mMonthTitle = view.findViewById(R.id.month_title);
    mCalendarGrid = view.findViewById(R.id.calendar_grid);
    mCurrentStreakTv = view.findViewById(R.id.current_streak_tv);
    mLongestStreakTv = view.findViewById(R.id.longest_streak_tv);
    mGamesListView = view.findViewById(R.id.daily_games_list);
    mNextMonthBtn = view.findViewById(R.id.next_month);

    mCurrentMonth = Calendar.getInstance();
    mCurrentMonth.set(Calendar.DAY_OF_MONTH, 1);
    mCurrentMonth.set(Calendar.HOUR_OF_DAY, 0);
    mCurrentMonth.set(Calendar.MINUTE, 0);
    mCurrentMonth.set(Calendar.SECOND, 0);
    mCurrentMonth.set(Calendar.MILLISECOND, 0);

    view.findViewById(R.id.prev_month).setOnClickListener(v -> {
      mCurrentMonth.add(Calendar.MONTH, -1);
      updateCalendar();
    });

    mNextMonthBtn.setOnClickListener(v -> {
      mCurrentMonth.add(Calendar.MONTH, 1);
      updateCalendar();
    });

    view.findViewById(R.id.today_btn).setOnClickListener(v -> {
      mCurrentMonth = Calendar.getInstance();
      mCurrentMonth.set(Calendar.DAY_OF_MONTH, 1);
      mCurrentMonth.set(Calendar.HOUR_OF_DAY, 0);
      mCurrentMonth.set(Calendar.MINUTE, 0);
      mCurrentMonth.set(Calendar.SECOND, 0);
      mCurrentMonth.set(Calendar.MILLISECOND, 0);
      updateCalendar();
    });

    mCompletedGames = new ArrayList<>();
    for (DailyGame game : mApplication.getCompletedDailyGames()) {
      mCompletedGames.add(game);
    }
    Collections.sort(mCompletedGames, (g1, g2) -> g2.getDateStarted().compareTo(g1.getDateStarted()));

    mGamesAdapter = new DailyGamesAdapter(getActivity(), mCompletedGames);
    mGamesListView.setAdapter(mGamesAdapter);

    updateCalendar();
    updateStreaks();

    return view;
  }

  private void updateCalendar() {
    SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    mMonthTitle.setText(sdf.format(mCurrentMonth.getTime()));

    Calendar today = Calendar.getInstance();
    boolean isCurrentMonth = mCurrentMonth.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                             mCurrentMonth.get(Calendar.MONTH) == today.get(Calendar.MONTH);

    mNextMonthBtn.setEnabled(!isCurrentMonth);

    CalendarAdapter adapter = new CalendarAdapter(getActivity(), mCurrentMonth, mCompletedGames);
    mCalendarGrid.setAdapter(adapter);

    mCalendarGrid.setOnItemClickListener((parent, view, position, id) -> {
        Calendar selectedDate = (Calendar) adapter.getItem(position);
        if (selectedDate != null) {
            long daySeed = getStartOfDay(selectedDate.getTimeInMillis());
            if (daySeed > getStartOfDay(System.currentTimeMillis())) {
                return;
            }
            DailyGame game = null;
            for (DailyGame dg : mApplication.getCompletedDailyGames()) {
                if (dg.getRandomSeed() == daySeed) {
                    game = dg;
                    break;
                }
            }
            if (game == null || game.getGameState() != DailyGame.GameState.COMPLETED) {
                // Launch DailyGameActivity for this date
                android.content.Intent intent = new android.content.Intent(getActivity(), DailyGameActivity.class);
                DailyGame newGame = mApplication.getDailyGameForDate(daySeed);
                intent.putExtra(com.antsapps.triples.backend.Game.ID_TAG, newGame.getId());
                startActivity(intent);
            }
        }
    });
  }

  private void updateStreaks() {
    Set<Long> completedOnDaySeeds = new HashSet<>();
    for (DailyGame game : mCompletedGames) {
        if (game.getDateCompleted() == null) continue;
        long startSeed = getStartOfDay(game.getDateStarted().getTime());
        if (getStartOfDay(game.getDateCompleted().getTime()) == startSeed) {
            completedOnDaySeeds.add(startSeed);
        }
    }

    int currentStreak = 0;
    Calendar cal = Calendar.getInstance();
    while (completedOnDaySeeds.contains(getStartOfDay(cal.getTimeInMillis()))) {
        currentStreak++;
        cal.add(Calendar.DAY_OF_YEAR, -1);
    }

    int longestStreak = 0;
    int tempStreak = 0;
    List<Long> sortedSeeds = new ArrayList<>(completedOnDaySeeds);
    Collections.sort(sortedSeeds);
    Calendar lastCal = null;
    for (Long seed : sortedSeeds) {
        Calendar currentCal = Calendar.getInstance();
        currentCal.setTimeInMillis(seed);
        if (lastCal != null) {
            Calendar expectedCal = (Calendar) lastCal.clone();
            expectedCal.add(Calendar.DAY_OF_YEAR, 1);
            if (expectedCal.get(Calendar.YEAR) == currentCal.get(Calendar.YEAR) &&
                expectedCal.get(Calendar.DAY_OF_YEAR) == currentCal.get(Calendar.DAY_OF_YEAR)) {
                tempStreak++;
            } else {
                tempStreak = 1;
            }
        } else {
            tempStreak = 1;
        }
        lastCal = currentCal;
        longestStreak = Math.max(longestStreak, tempStreak);
    }

    mCurrentStreakTv.setText(getString(R.string.current_streak) + " " + currentStreak);
    mLongestStreakTv.setText(getString(R.string.longest_streak) + " " + longestStreak);
  }

  private static long getStartOfDay(long time) {
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(time);
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    return cal.getTimeInMillis();
  }

  private static class CalendarAdapter extends BaseAdapter {
    private final Context mContext;
    private final List<Calendar> mDays;
    private final Set<Long> mCompletedOnDaySeeds;
    private final Set<Long> mCompletedLateSeeds;
    private final long mTodaySeed;
    private final int mMonth;

    CalendarAdapter(Context context, Calendar month, List<DailyGame> completedGames) {
      mContext = context;
      mMonth = month.get(Calendar.MONTH);
      mTodaySeed = getStartOfDay(System.currentTimeMillis());

      mDays = new ArrayList<>();
      Calendar cal = (Calendar) month.clone();
      cal.set(Calendar.DAY_OF_MONTH, 1);
      int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1; // 0-indexed starting Sunday
      cal.add(Calendar.DAY_OF_MONTH, -firstDayOfWeek);

      for (int i = 0; i < 42; i++) {
        mDays.add((Calendar) cal.clone());
        cal.add(Calendar.DAY_OF_MONTH, 1);
      }

      mCompletedOnDaySeeds = new HashSet<>();
      mCompletedLateSeeds = new HashSet<>();
      for (DailyGame game : completedGames) {
          if (game.getDateCompleted() == null) continue;
          long startSeed = getStartOfDay(game.getDateStarted().getTime());
          if (getStartOfDay(game.getDateCompleted().getTime()) == startSeed) {
              mCompletedOnDaySeeds.add(startSeed);
          } else {
              mCompletedLateSeeds.add(startSeed);
          }
      }
    }

    @Override
    public int getCount() { return mDays.size(); }
    @Override
    public Object getItem(int position) { return mDays.get(position); }
    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      TextView tv = (TextView) convertView;
      if (tv == null) {
        tv = new TextView(mContext) {
            private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            {
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(2);
                mPaint.setColor(Color.BLACK);
            }
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                Calendar day = (Calendar) getTag();
                if (day != null && getStartOfDay(day.getTimeInMillis()) == mTodaySeed) {
                    canvas.drawCircle(getWidth()/2f, getHeight()/2f, Math.min(getWidth(), getHeight())/2f - 4, mPaint);
                }
            }
        };
        tv.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100));
        tv.setGravity(android.view.Gravity.CENTER);
      }

      Calendar day = mDays.get(position);
      tv.setTag(day);
      tv.setText(String.valueOf(day.get(Calendar.DAY_OF_MONTH)));

      long daySeed = getStartOfDay(day.getTimeInMillis());

      if (day.get(Calendar.MONTH) != mMonth) {
        tv.setTextColor(Color.LTGRAY);
        tv.setBackgroundColor(Color.TRANSPARENT);
      } else if (daySeed > mTodaySeed) {
        tv.setTextColor(Color.LTGRAY);
        tv.setBackgroundColor(Color.TRANSPARENT);
      } else {
        tv.setTextColor(Color.BLACK);
        if (mCompletedOnDaySeeds.contains(daySeed)) {
          tv.setBackgroundColor(mContext.getResources().getColor(R.color.daily_accent));
        } else if (mCompletedLateSeeds.contains(daySeed)) {
          tv.setBackgroundColor(mContext.getResources().getColor(R.color.daily_background));
        } else {
          tv.setBackgroundColor(Color.TRANSPARENT);
        }
      }

      return tv;
    }
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
    public int getCount() { return mGames.size(); }
    @Override
    public Object getItem(int position) { return mGames.get(position); }
    @Override
    public long getItemId(int position) { return mGames.get(position).getId(); }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      if (convertView == null) {
        convertView = LayoutInflater.from(mContext).inflate(R.layout.stats_game_list_item, parent, false);
      }
      DailyGame game = mGames.get(position);
      TextView dateTv = convertView.findViewById(R.id.date_played);
      TextView resultTv = convertView.findViewById(R.id.result);

      String hintAsterisk = game.areHintsUsed() ? "*" : "";
      dateTv.setText(mDateFormat.format(game.getDateStarted()) + " (" + game.getNumTriplesFound() + " triples)" + hintAsterisk);

      long seconds = game.getTimeElapsed() / 1000;
      resultTv.setText(String.format("%d:%02d", seconds / 60, seconds % 60));

      return convertView;
    }
  }
}
