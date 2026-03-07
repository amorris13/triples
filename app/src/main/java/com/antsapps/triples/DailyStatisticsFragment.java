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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import android.os.Bundle;

public class DailyStatisticsFragment extends Fragment {

  private Application mApplication;
  private List<DailyGame> mCompletedGames;
  private Map<Long, DailyGame> mCompletedGamesMap;
  private Calendar mCurrentMonth;
  private Calendar mSelectedDate;
  private GridView mCalendarGrid;
  private TextView mMonthTitle;
  private TextView mCurrentStreakTv;
  private TextView mLongestStreakTv;
  private TextView mTotalSolvedTv;
  private View mNextMonthBtn;
  private View mPrevMonthBtn;

  private TextView mDetailsDate;
  private TextView mDetailsStats;
  private Button mPlayButton;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    mApplication = Application.getInstance(getActivity());
    View view = inflater.inflate(R.layout.daily_stats_fragment, container, false);

    mMonthTitle = view.findViewById(R.id.month_title);
    mCalendarGrid = view.findViewById(R.id.calendar_grid);
    mCurrentStreakTv = view.findViewById(R.id.current_streak_tv);
    mLongestStreakTv = view.findViewById(R.id.longest_streak_tv);
    mTotalSolvedTv = view.findViewById(R.id.total_solved_tv);
    mPrevMonthBtn = view.findViewById(R.id.prev_month);
    mNextMonthBtn = view.findViewById(R.id.next_month);

    mDetailsDate = view.findViewById(R.id.details_date);
    mDetailsStats = view.findViewById(R.id.details_stats);
    mPlayButton = view.findViewById(R.id.play_button);

    mCurrentMonth = Calendar.getInstance();
    mCurrentMonth.set(Calendar.DAY_OF_MONTH, 1);
    mCurrentMonth.set(Calendar.HOUR_OF_DAY, 0);
    mCurrentMonth.set(Calendar.MINUTE, 0);
    mCurrentMonth.set(Calendar.SECOND, 0);
    mCurrentMonth.set(Calendar.MILLISECOND, 0);

    mSelectedDate = Calendar.getInstance();
    mSelectedDate.set(Calendar.HOUR_OF_DAY, 0);
    mSelectedDate.set(Calendar.MINUTE, 0);
    mSelectedDate.set(Calendar.SECOND, 0);
    mSelectedDate.set(Calendar.MILLISECOND, 0);

    mPrevMonthBtn.setOnClickListener(v -> {
      mCurrentMonth.add(Calendar.MONTH, -1);
      updateCalendar();
    });

    mNextMonthBtn.setOnClickListener(v -> {
      mCurrentMonth.add(Calendar.MONTH, 1);
      updateCalendar();
    });

    mMonthTitle.setOnClickListener(v -> {
      mCurrentMonth = Calendar.getInstance();
      mCurrentMonth.set(Calendar.DAY_OF_MONTH, 1);
      mCurrentMonth.set(Calendar.HOUR_OF_DAY, 0);
      mCurrentMonth.set(Calendar.MINUTE, 0);
      mCurrentMonth.set(Calendar.SECOND, 0);
      mCurrentMonth.set(Calendar.MILLISECOND, 0);
      updateCalendar();
    });

    mCompletedGames = new ArrayList<>();
    mCompletedGamesMap = new HashMap<>();
    for (DailyGame game : mApplication.getCompletedDailyGames()) {
      mCompletedGames.add(game);
      mCompletedGamesMap.put(getStartOfDay(game.getRandomSeed()), game);
    }
    Collections.sort(mCompletedGames, (g1, g2) -> g2.getDateStarted().compareTo(g1.getDateStarted()));

    updateCalendar();
    updateStreaks();
    updateDetails();

    return view;
  }

  private void updateCalendar() {
    SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    mMonthTitle.setText(sdf.format(mCurrentMonth.getTime()));

    Calendar today = Calendar.getInstance();
    boolean isCurrentMonth = mCurrentMonth.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
        mCurrentMonth.get(Calendar.MONTH) == today.get(Calendar.MONTH);

    mNextMonthBtn.setEnabled(!isCurrentMonth);
    mNextMonthBtn.setAlpha(isCurrentMonth ? 0.3f : 1f);

    CalendarAdapter adapter = new CalendarAdapter(getActivity(), mCurrentMonth, mCompletedGames, mSelectedDate);
    mCalendarGrid.setAdapter(adapter);

    mCalendarGrid.setOnItemClickListener((parent, view, position, id) -> {
      Calendar clickedDate = (Calendar) adapter.getItem(position);
      if (clickedDate != null) {
        mSelectedDate = (Calendar) clickedDate.clone();
        adapter.setSelectedDate(mSelectedDate);
        updateDetails();
      }
    });
  }

  private void updateDetails() {
    DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
    mDetailsDate.setText(df.format(mSelectedDate.getTime()));

    long daySeed = getStartOfDay(mSelectedDate.getTimeInMillis());
    DailyGame game = mCompletedGamesMap.get(daySeed);

    if (game != null && game.getGameState() == DailyGame.GameState.COMPLETED) {
      String hintAsterisk = game.areHintsUsed() ? " *" : "";
      long seconds = game.getTimeElapsed() / 1000;
      mDetailsStats.setText(String.format("%d triples found in %d:%02d%s",
          game.getNumTriplesFound(), seconds / 60, seconds % 60, hintAsterisk));
      mPlayButton.setVisibility(View.GONE);
    } else {
      mDetailsStats.setText("Not yet completed");
      mPlayButton.setVisibility(View.VISIBLE);
      mPlayButton.setOnClickListener(v -> {
        android.content.Intent intent = new android.content.Intent(getActivity(), DailyGameActivity.class);
        DailyGame dailyGame = mApplication.getDailyGameForDate(daySeed);
        intent.putExtra(com.antsapps.triples.backend.Game.ID_TAG, dailyGame.getId());
        startActivity(intent);
      });
    }
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

    mCurrentStreakTv.setText(String.valueOf(currentStreak));
    mLongestStreakTv.setText(String.valueOf(longestStreak));
    mTotalSolvedTv.setText(String.valueOf(mCompletedGames.size()));
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
    private final Set<Long> mHintUsedSeeds;
    private final long mTodaySeed;
    private final int mMonth;
    private Calendar mSelectedDate;

    CalendarAdapter(Context context, Calendar month, List<DailyGame> completedGames, Calendar selectedDate) {
      mContext = context;
      mMonth = month.get(Calendar.MONTH);
      mTodaySeed = getStartOfDay(System.currentTimeMillis());
      mSelectedDate = selectedDate;

      mDays = new ArrayList<>();
      Calendar cal = (Calendar) month.clone();
      cal.set(Calendar.DAY_OF_MONTH, 1);
      int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // 1 (Sun) to 7 (Sat)
      // Adjust to Monday = 1
      int shift = (firstDayOfWeek + 5) % 7;
      cal.add(Calendar.DAY_OF_MONTH, -shift);

      for (int i = 0; i < 42; i++) {
        mDays.add((Calendar) cal.clone());
        cal.add(Calendar.DAY_OF_MONTH, 1);
      }

      mCompletedOnDaySeeds = new HashSet<>();
      mCompletedLateSeeds = new HashSet<>();
      mHintUsedSeeds = new HashSet<>();
      for (DailyGame game : completedGames) {
        if (game.getDateCompleted() == null) continue;
        long startSeed = getStartOfDay(game.getDateStarted().getTime());
        if (getStartOfDay(game.getDateCompleted().getTime()) == startSeed) {
          mCompletedOnDaySeeds.add(startSeed);
        } else {
          mCompletedLateSeeds.add(startSeed);
        }
        if (game.areHintsUsed()) {
          mHintUsedSeeds.add(startSeed);
        }
      }
    }

    void setSelectedDate(Calendar selectedDate) {
      mSelectedDate = selectedDate;
      notifyDataSetChanged();
    }

    @Override
    public int getCount() { return mDays.size(); }
    @Override
    public Object getItem(int position) { return mDays.get(position); }
    @Override
    public long getItemId(int position) { return position; }

    @Override
    public boolean isEnabled(int position) {
      Calendar day = mDays.get(position);
      long daySeed = getStartOfDay(day.getTimeInMillis());
      return day.get(Calendar.MONTH) == mMonth && daySeed <= mTodaySeed;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      if (convertView == null) {
        convertView = new CalendarDayView(mContext);
      }
      CalendarDayView view = (CalendarDayView) convertView;
      Calendar day = mDays.get(position);
      long daySeed = getStartOfDay(day.getTimeInMillis());

      boolean isCurrentMonth = day.get(Calendar.MONTH) == mMonth;
      boolean isToday = daySeed == mTodaySeed;
      boolean isSelected = mSelectedDate != null && daySeed == getStartOfDay(mSelectedDate.getTimeInMillis());
      boolean isSolvedOnDay = mCompletedOnDaySeeds.contains(daySeed);
      boolean isSolvedLate = mCompletedLateSeeds.contains(daySeed);
      boolean isHintUsed = mHintUsedSeeds.contains(daySeed);
      boolean isFuture = daySeed > mTodaySeed;

      view.setDay(day, isCurrentMonth, isToday, isSelected, isSolvedOnDay, isSolvedLate, isHintUsed, isFuture);

      return view;
    }
  }

  private static class CalendarDayView extends TextView {
    private boolean mIsToday;
    private boolean mIsSelected;
    private boolean mIsSolvedOnDay;
    private boolean mIsSolvedLate;
    private boolean mIsHintUsed;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public CalendarDayView(Context context) {
      super(context);
      setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120));
      setGravity(android.view.Gravity.CENTER);
    }

    public void setDay(Calendar day, boolean isCurrentMonth, boolean isToday, boolean isSelected,
        boolean isSolvedOnDay, boolean isSolvedLate, boolean isHintUsed, boolean isFuture) {
      mIsToday = isToday;
      mIsSelected = isSelected;
      mIsSolvedOnDay = isSolvedOnDay;
      mIsSolvedLate = isSolvedLate;
      mIsHintUsed = isHintUsed;

      if (!isCurrentMonth) {
        setText("");
        setBackgroundColor(Color.TRANSPARENT);
        mIsToday = false;
        mIsSolvedOnDay = false;
        mIsSolvedLate = false;
        mIsHintUsed = false;
        invalidate();
        return;
      }

      String text = String.valueOf(day.get(Calendar.DAY_OF_MONTH));
      if (isHintUsed) {
        text += "*";
      }
      setText(text);

      if (isFuture) {
        setTextColor(Color.LTGRAY);
      } else {
        setTextColor(getResources().getColor(R.color.color_text_primary));
      }

      if (isSelected) {
        setBackgroundColor(getResources().getColor(R.color.daily_calendar_selected));
      } else {
        setBackgroundColor(Color.TRANSPARENT);
      }
      invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
      int width = getWidth();
      int height = getHeight();
      int radius = Math.min(width, height) / 2 - 8;

      if (mIsSolvedOnDay) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(getResources().getColor(R.color.daily_calendar_solved_on_day));
        canvas.drawCircle(width / 2f, height / 2f, radius, mPaint);
      } else if (mIsSolvedLate) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(getResources().getColor(R.color.daily_calendar_solved_late));
        canvas.drawCircle(width / 2f, height / 2f, radius, mPaint);
      }

      if (mIsToday) {
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(4);
        mPaint.setColor(getResources().getColor(R.color.daily_calendar_today_outline));
        canvas.drawCircle(width / 2f, height / 2f, radius, mPaint);
      }

      super.onDraw(canvas);
    }
  }
}
