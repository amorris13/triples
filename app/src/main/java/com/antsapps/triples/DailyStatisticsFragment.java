package com.antsapps.triples;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
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

public class DailyStatisticsFragment extends Fragment {

  private Application mApplication;
  private List<DailyGame> mCompletedGames;
  private Calendar mCurrentMonth;
  private GridView mCalendarGrid;
  private Button mMonthTitle;
  private TextView mCurrentStreakTv;
  private TextView mLongestStreakTv;
  private TextView mTotalSolvedTv;
  private Button mNextMonthBtn;
  private Calendar mSelectedDate;
  private TextView mDetailDate;
  private TextView mDetailStatus;
  private Button mDetailPlayBtn;
  private View mDetailResultsContainer;
  private TextView mDetailTriples;
  private TextView mDetailTime;

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    mApplication = Application.getInstance(getActivity());
    View view = inflater.inflate(R.layout.daily_stats_fragment, container, false);

    mMonthTitle = view.findViewById(R.id.month_title);
    mCalendarGrid = view.findViewById(R.id.calendar_grid);
    mCurrentStreakTv = view.findViewById(R.id.current_streak_tv);
    mLongestStreakTv = view.findViewById(R.id.longest_streak_tv);
    mTotalSolvedTv = view.findViewById(R.id.total_solved_tv);
    mNextMonthBtn = view.findViewById(R.id.next_month);

    mDetailDate = view.findViewById(R.id.detail_date);
    mDetailStatus = view.findViewById(R.id.detail_status);
    mDetailPlayBtn = view.findViewById(R.id.detail_play_btn);
    mDetailPlayBtn.setBackgroundTintList(
        ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.daily_accent)));
    mDetailResultsContainer = view.findViewById(R.id.detail_results_container);
    mDetailTriples = view.findViewById(R.id.detail_triples);
    mDetailTime = view.findViewById(R.id.detail_time);

    mCurrentMonth = Calendar.getInstance();
    mCurrentMonth.set(Calendar.DAY_OF_MONTH, 1);
    mCurrentMonth.set(Calendar.HOUR_OF_DAY, 0);
    mCurrentMonth.set(Calendar.MINUTE, 0);
    mCurrentMonth.set(Calendar.SECOND, 0);
    mCurrentMonth.set(Calendar.MILLISECOND, 0);

    view.findViewById(R.id.prev_month)
        .setOnClickListener(
            v -> {
              mCurrentMonth.add(Calendar.MONTH, -1);
              updateCalendar();
            });

    mNextMonthBtn.setOnClickListener(
        v -> {
          Calendar nextMonth = (Calendar) mCurrentMonth.clone();
          nextMonth.add(Calendar.MONTH, 1);
          Calendar now = Calendar.getInstance();
          if (nextMonth.get(Calendar.YEAR) < now.get(Calendar.YEAR)
              || (nextMonth.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                  && nextMonth.get(Calendar.MONTH) <= now.get(Calendar.MONTH))) {
            mCurrentMonth.add(Calendar.MONTH, 1);
            updateCalendar();
          }
        });

    mMonthTitle.setOnClickListener(
        v -> {
          mCurrentMonth = Calendar.getInstance();
          mCurrentMonth.set(Calendar.DAY_OF_MONTH, 1);
          mCurrentMonth.set(Calendar.HOUR_OF_DAY, 0);
          mCurrentMonth.set(Calendar.MINUTE, 0);
          mCurrentMonth.set(Calendar.SECOND, 0);
          mCurrentMonth.set(Calendar.MILLISECOND, 0);
          mSelectedDate = Calendar.getInstance();
          updateCalendar();
        });

    mSelectedDate = Calendar.getInstance();

    GestureDetector gestureDetector =
        new GestureDetector(
            getActivity(),
            new GestureDetector.SimpleOnGestureListener() {
              @Override
              public boolean onFling(
                  MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (Math.abs(velocityX) > Math.abs(velocityY) && Math.abs(velocityX) > 100) {
                  if (velocityX > 0) {
                    mCurrentMonth.add(Calendar.MONTH, -1);
                    updateCalendar();
                    return true;
                  } else {
                    Calendar nextMonth = (Calendar) mCurrentMonth.clone();
                    nextMonth.add(Calendar.MONTH, 1);
                    Calendar now = Calendar.getInstance();
                    if (nextMonth.get(Calendar.YEAR) < now.get(Calendar.YEAR)
                        || (nextMonth.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                            && nextMonth.get(Calendar.MONTH) <= now.get(Calendar.MONTH))) {
                      mCurrentMonth.add(Calendar.MONTH, 1);
                      updateCalendar();
                      return true;
                    }
                  }
                }
                return false;
              }
            });

    mCalendarGrid.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

    mCompletedGames = new ArrayList<>();
    for (DailyGame game : mApplication.getCompletedDailyGames()) {
      mCompletedGames.add(game);
    }
    Collections.sort(
        mCompletedGames, (g1, g2) -> g2.getDateStarted().compareTo(g1.getDateStarted()));

    updateCalendar();
    updateStreaks();

    return view;
  }

  private void updateCalendar() {
    SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    mMonthTitle.setText(sdf.format(mCurrentMonth.getTime()));

    Calendar today = Calendar.getInstance();
    boolean isCurrentMonth =
        mCurrentMonth.get(Calendar.YEAR) == today.get(Calendar.YEAR)
            && mCurrentMonth.get(Calendar.MONTH) == today.get(Calendar.MONTH);

    mNextMonthBtn.setEnabled(!isCurrentMonth);

    CalendarAdapter adapter =
        new CalendarAdapter(
            getActivity(),
            mCurrentMonth,
            mCompletedGames,
            getStartOfDay(mSelectedDate.getTimeInMillis()));
    mCalendarGrid.setAdapter(adapter);

    mCalendarGrid.setOnItemClickListener(
        (parent, view1, position, id) -> {
          Calendar day = (Calendar) adapter.getItem(position);
          if (adapter.isEnabled(position)) {
            long daySeed = getStartOfDay(day.getTimeInMillis());
            mSelectedDate = (Calendar) day.clone();
            adapter.setSelectedSeed(daySeed);
            updateDetailSection();
          }
        });

    updateDetailSection();
  }

  private void updateDetailSection() {
    DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
    mDetailDate.setText(df.format(mSelectedDate.getTime()));

    long daySeed = getStartOfDay(mSelectedDate.getTimeInMillis());
    DailyGame game = null;
    for (DailyGame dg : mApplication.getDailyGames()) {
      if (dg.getRandomSeed() == daySeed) {
        game = dg;
        break;
      }
    }

    if (game == null || game.getGameState() != DailyGame.GameState.COMPLETED) {
      if (game == null || game.getNumTriplesFound() == 0) {
        mDetailStatus.setText(R.string.daily_not_started);
      } else {
        mDetailStatus.setText(
            getString(R.string.daily_incomplete)
                + " ("
                + game.getNumTriplesFound()
                + "/"
                + game.getTotalTriplesCount()
                + " triples found)");
      }
      mDetailResultsContainer.setVisibility(View.GONE);
      mDetailPlayBtn.setVisibility(View.VISIBLE);
      mDetailPlayBtn.setOnClickListener(
          v -> {
            android.content.Intent intent =
                new android.content.Intent(getActivity(), DailyGameActivity.class);
            DailyGame newGame = mApplication.getDailyGameForDate(daySeed);
            intent.putExtra(com.antsapps.triples.backend.Game.ID_TAG, newGame.getId());
            startActivity(intent);
          });
    } else {
      String status = getString(R.string.daily_completed);
      if (game.areHintsUsed()) {
        status += " " + getString(R.string.daily_hints_used_suffix);
      }
      mDetailStatus.setText(status);
      mDetailPlayBtn.setVisibility(View.GONE);
      mDetailResultsContainer.setVisibility(View.VISIBLE);
      mDetailTriples.setText(String.valueOf(game.getNumTriplesFound()));
      long seconds = game.getTimeElapsed() / 1000;
      mDetailTime.setText(String.format("%d:%02d", seconds / 60, seconds % 60));
    }
  }

  private void updateStreaks() {
    Set<Long> completedOnDaySeeds = new HashSet<>();
    int totalSolved = 0;
    for (DailyGame game : mCompletedGames) {
      if (game.getDateCompleted() == null || game.areHintsUsed()) continue;
      totalSolved++;
      long startSeed = getStartOfDay(game.getDateStarted().getTime());
      if (getStartOfDay(game.getDateCompleted().getTime()) == startSeed) {
        completedOnDaySeeds.add(startSeed);
      }
    }

    int currentStreak = 0;
    Calendar cal = Calendar.getInstance();
    if (!completedOnDaySeeds.contains(getStartOfDay(cal.getTimeInMillis()))) {
      cal.add(Calendar.DAY_OF_YEAR, -1);
    }
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
        if (expectedCal.get(Calendar.YEAR) == currentCal.get(Calendar.YEAR)
            && expectedCal.get(Calendar.DAY_OF_YEAR) == currentCal.get(Calendar.DAY_OF_YEAR)) {
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
    mTotalSolvedTv.setText(String.valueOf(totalSolved));
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
    private final int mTextPrimaryColor;
    private final int mTextSecondaryColor;
    private final List<Calendar> mDays;
    private final Set<Long> mCompletedOnDaySeeds;
    private final Set<Long> mCompletedLateSeeds;
    private final Set<Long> mHintSeeds;
    private final long mTodaySeed;
    private final int mMonth;
    private final int mYear;
    private long mSelectedSeed;

    CalendarAdapter(
        Context context, Calendar month, List<DailyGame> completedGames, long selectedSeed) {
      mContext = context;
      mTextPrimaryColor = ContextCompat.getColor(mContext, R.color.color_text_primary);
      mTextSecondaryColor = ContextCompat.getColor(mContext, R.color.color_text_secondary);
      mMonth = month.get(Calendar.MONTH);
      mYear = month.get(Calendar.YEAR);
      mTodaySeed = getStartOfDay(System.currentTimeMillis());
      mSelectedSeed = selectedSeed;

      mDays = new ArrayList<>();
      Calendar cal = (Calendar) month.clone();
      cal.set(Calendar.DAY_OF_MONTH, 1);
      int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
      // Adjust to Monday start: (Calendar.MONDAY is 2, SUNDAY is 1)
      int offset = (firstDayOfWeek + 5) % 7;
      cal.add(Calendar.DAY_OF_MONTH, -offset);

      for (int i = 0; i < 42; i++) {
        mDays.add((Calendar) cal.clone());
        cal.add(Calendar.DAY_OF_MONTH, 1);
      }

      mCompletedOnDaySeeds = new HashSet<>();
      mCompletedLateSeeds = new HashSet<>();
      mHintSeeds = new HashSet<>();
      for (DailyGame game : completedGames) {
        if (game.getDateCompleted() == null) continue;
        long startSeed = getStartOfDay(game.getDateStarted().getTime());
        if (game.areHintsUsed()) {
          mHintSeeds.add(startSeed);
        }
        if (getStartOfDay(game.getDateCompleted().getTime()) == startSeed) {
          mCompletedOnDaySeeds.add(startSeed);
        } else {
          mCompletedLateSeeds.add(startSeed);
        }
      }
    }

    void setSelectedSeed(long seed) {
      mSelectedSeed = seed;
      notifyDataSetChanged();
    }

    @Override
    public int getCount() {
      return mDays.size();
    }

    @Override
    public Object getItem(int position) {
      return mDays.get(position);
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public boolean areAllItemsEnabled() {
      return false;
    }

    @Override
    public boolean isEnabled(int position) {
      Calendar day = mDays.get(position);
      return day.get(Calendar.YEAR) == mYear
          && day.get(Calendar.MONTH) == mMonth
          && getStartOfDay(day.getTimeInMillis()) <= getStartOfDay(System.currentTimeMillis());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      TextView tv = (TextView) convertView;
      if (tv == null) {
        tv =
            new TextView(mContext) {
              private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

              @Override
              protected void onDraw(Canvas canvas) {
                Calendar day = (Calendar) getTag();
                if (day == null
                    || day.get(Calendar.MONTH) != mMonth
                    || day.get(Calendar.YEAR) != mYear) {
                  return;
                }

                float density = mContext.getResources().getDisplayMetrics().density;
                long daySeed = getStartOfDay(day.getTimeInMillis());
                float centerX = getWidth() / 2f;
                float centerY = getHeight() / 2f;
                float radius = Math.min(getWidth(), getHeight()) / 2f - 4 * density;

                // Background circle for solved games
                if (mCompletedOnDaySeeds.contains(daySeed)) {
                  mPaint.setStyle(Paint.Style.FILL);
                  mPaint.setColor(ContextCompat.getColor(mContext, R.color.daily_accent));
                  canvas.drawCircle(centerX, centerY, radius, mPaint);
                } else if (mCompletedLateSeeds.contains(daySeed)) {
                  mPaint.setStyle(Paint.Style.FILL);
                  int color = ContextCompat.getColor(mContext, R.color.daily_accent);
                  mPaint.setColor(
                      Color.argb(128, Color.red(color), Color.green(color), Color.blue(color)));
                  canvas.drawCircle(centerX, centerY, radius, mPaint);
                }

                // Selection indicator (outline)
                if (daySeed == mSelectedSeed) {
                  mPaint.setStyle(Paint.Style.STROKE);
                  mPaint.setStrokeWidth(2 * density);
                  mPaint.setColor(ContextCompat.getColor(mContext, R.color.color_text_primary));
                  canvas.drawCircle(centerX, centerY, radius + density, mPaint);
                }

                super.onDraw(canvas);
              }
            };
        tv.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120));
        tv.setGravity(android.view.Gravity.CENTER);
      }

      Calendar day = mDays.get(position);
      tv.setTag(day);
      long daySeed = getStartOfDay(day.getTimeInMillis());

      if (day.get(Calendar.MONTH) != mMonth || day.get(Calendar.YEAR) != mYear) {
        tv.setText("");
        tv.setBackground(null);
      } else {
        String text = String.valueOf(day.get(Calendar.DAY_OF_MONTH));
        if (mHintSeeds.contains(daySeed)) {
          text += "*";
        }
        tv.setText(text);

        if (daySeed == mTodaySeed) {
          tv.setTypeface(null, Typeface.BOLD);
          tv.setPaintFlags(tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        } else {
          tv.setTypeface(null, Typeface.NORMAL);
          tv.setPaintFlags(tv.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
        }

        if (daySeed > mTodaySeed) {
          tv.setTextColor(mTextSecondaryColor);
        } else {
          tv.setTextColor(mTextPrimaryColor);
        }
      }

      return tv;
    }
  }
}
