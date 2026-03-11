package com.antsapps.triples;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
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
import com.antsapps.triples.backend.DailyStatisticsUtil;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.util.CsvExportable;
import com.antsapps.triples.util.CsvUtil;
import com.antsapps.triples.util.ShareUtil;
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

public class DailyStatisticsFragment extends Fragment implements CsvExportable {

  private Application mApplication;
  private List<DailyGame> mCompletedGames;
  private Calendar mCurrentMonth;
  private GridView mCalendarGrid;
  private Button mMonthTitle;
  private TextView mCurrentStreakTv;
  private TextView mLongestStreakTv;
  private TextView mTotalSolvedTv;
  private Button mNextMonthBtn;
  private DailyGame.Day mSelectedDay;
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
          mSelectedDay = DailyGame.Day.forToday();
          updateCalendar();
        });

    mSelectedDay = DailyGame.Day.forToday();

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
    Collections.sort(mCompletedGames, (g1, g2) -> g2.getGameDay().compareTo(g1.getGameDay()));

    updateCalendar();
    updateStreaks();

    return view;
  }

  private void updateCalendar() {
    SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    mMonthTitle.setText(sdf.format(mCurrentMonth.getTime()));

    Calendar now = Calendar.getInstance();
    boolean isCurrentMonth =
        mCurrentMonth.get(Calendar.YEAR) == now.get(Calendar.YEAR)
            && mCurrentMonth.get(Calendar.MONTH) == now.get(Calendar.MONTH);

    mNextMonthBtn.setEnabled(!isCurrentMonth);

    CalendarAdapter adapter =
        new CalendarAdapter(
            getActivity(), mCurrentMonth, mApplication.getDailyGames(), mSelectedDay);
    mCalendarGrid.setAdapter(adapter);

    mCalendarGrid.setOnItemClickListener(
        (parent, view1, position, id) -> {
          Calendar day = (Calendar) adapter.getItem(position);
          if (adapter.isEnabled(position)) {
            mSelectedDay = DailyGame.Day.forCalendar(day);
            adapter.setSelectedDay(mSelectedDay);
            updateDetailSection();
          }
        });

    updateDetailSection();
  }

  private void updateDetailSection() {
    DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
    mDetailDate.setText(df.format(mSelectedDay.getCalendar().getTime()));

    DailyGame game = null;
    for (DailyGame dg : mApplication.getDailyGames()) {
      if (dg.getGameDay().equals(mSelectedDay)) {
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
            Intent intent = new Intent(getActivity(), DailyGameActivity.class);
            DailyGame newGame = mApplication.getDailyGameForDate(mSelectedDay);
            intent.putExtra(Game.ID_TAG, newGame.getId());
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

  public void exportToCsv() {
    ShareUtil.shareCsv(
        getActivity(), "daily_statistics.csv", CsvUtil.getDailyCsvContent(mCompletedGames));
  }

  private void updateStreaks() {
    DailyStatisticsUtil.DailyStatistics dailyStatistics =
        DailyStatisticsUtil.computeDailyStatistics(mCompletedGames);

    mCurrentStreakTv.setText(String.valueOf(dailyStatistics.currentStreak));
    mLongestStreakTv.setText(String.valueOf(dailyStatistics.longestStreak));
    mTotalSolvedTv.setText(String.valueOf(dailyStatistics.totalGamesCompleted));
  }

  private static class CalendarAdapter extends BaseAdapter {
    public static final int PADDING_DP = 4;
    private final Context mContext;
    private final int mTextPrimaryColor;
    private final int mTextSecondaryColor;
    private final List<Calendar> mDays;
    private final Set<DailyGame.Day> mCompletedOnDayDates;
    private final Set<DailyGame.Day> mCompletedLateDates;
    private final Set<DailyGame.Day> mHintDates;
    private final DailyGame.Day mToday;
    private final int mMonth; // 1 indexed
    private final int mYear;
    private final int mSelectableItemBackground;
    private DailyGame.Day mSelectedDate;
    private final Map<DailyGame.Day, Float> mProgressMap;

    CalendarAdapter(
        Context context, Calendar month, List<DailyGame> allGames, DailyGame.Day selectedDay) {
      mContext = context;
      mTextPrimaryColor = ContextCompat.getColor(mContext, R.color.color_text_primary);
      mTextSecondaryColor = ContextCompat.getColor(mContext, R.color.color_text_secondary);

      TypedValue outValue = new TypedValue();
      mContext.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
      mSelectableItemBackground = outValue.resourceId;
      mMonth = month.get(Calendar.MONTH) + 1;
      mYear = month.get(Calendar.YEAR);
      mToday = DailyGame.Day.forToday();
      mSelectedDate = selectedDay;

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

      mCompletedOnDayDates = new HashSet<>();
      mCompletedLateDates = new HashSet<>();
      mHintDates = new HashSet<>();
      mProgressMap = new HashMap<>();
      for (DailyGame game : allGames) {
        DailyGame.Day day = game.getGameDay();
        if (game.areHintsUsed()) {
          mHintDates.add(day);
        }
        if (game.getDateCompleted() != null) {
          if (game.isCompletedOnTime()) {
            mCompletedOnDayDates.add(day);
          } else {
            mCompletedLateDates.add(day);
          }
        } else if (game.getNumTriplesFound() > 0) {
          mProgressMap.put(
              day, (float) game.getNumTriplesFound() / (float) game.getTotalTriplesCount());
        }
      }
    }

    void setSelectedDay(DailyGame.Day day) {
      mSelectedDate = day;
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
          && day.get(Calendar.MONTH) == mMonth - 1
          && day.getTimeInMillis() <= System.currentTimeMillis();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      TextView tv = (TextView) convertView;
      float density = mContext.getResources().getDisplayMetrics().density;
      if (tv == null) {
        tv =
            new androidx.appcompat.widget.AppCompatTextView(mContext) {
              private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
              
              @Override
              protected void onDraw(Canvas canvas) {
                DailyGame.Day day = DailyGame.Day.forCalendar((Calendar) getTag());
                if (day == null || day.getMonth() != mMonth || day.getYear() != mYear) {
                  return;
                }

                float centerX = getWidth() / 2f;
                float centerY = getHeight() / 2f;
                float radius = Math.min(getWidth(), getHeight()) / 2f - PADDING_DP * density;

                // Background circle for solved games
                if (mCompletedOnDayDates.contains(day)) {
                  mPaint.setStyle(Paint.Style.FILL);
                  mPaint.setColor(ContextCompat.getColor(mContext, R.color.daily_accent));
                  canvas.drawCircle(centerX, centerY, radius, mPaint);
                } else if (mCompletedLateDates.contains(day)) {
                  mPaint.setStyle(Paint.Style.FILL);
                  int color = ContextCompat.getColor(mContext, R.color.daily_accent);
                  mPaint.setColor(updateAlpha(color, 128));
                  canvas.drawCircle(centerX, centerY, radius, mPaint);
                } else if (mProgressMap.containsKey(day)) {
                  float progress = mProgressMap.get(day);
                  mPaint.setStyle(Paint.Style.FILL);
                  int color = ContextCompat.getColor(mContext, R.color.daily_accent);
                  if (!day.equals(mToday)) {
                    color = updateAlpha(color, 128);
                  }
                  mPaint.setColor(color);
                  RectF rectF =
                      new RectF(
                          centerX - radius, centerY - radius, centerX + radius, centerY + radius);
                  canvas.drawArc(rectF, -90, 360 * progress, true, mPaint);
                }

                // Selection indicator (outline)
                if (day.equals(mSelectedDate)) {
                  mPaint.setStyle(Paint.Style.STROKE);
                  mPaint.setStrokeWidth(2 * density);
                  mPaint.setColor(ContextCompat.getColor(mContext, R.color.color_text_primary));
                  canvas.drawCircle(centerX, centerY, radius + density, mPaint);
                }

                super.onDraw(canvas);
              }
            };
        tv.setLayoutParams(new GridView.LayoutParams((int) (48 * density), (int) (48 * density)));
        tv.setGravity(Gravity.CENTER);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          TypedValue outValue = new TypedValue();
          mContext
                  .getTheme()
                  .resolveAttribute(android.R.attr.colorControlHighlight, outValue, true);

          Drawable mask = new ShapeDrawable(new OvalShape());;
          RippleDrawable ripple =
                  new RippleDrawable(
                          ColorStateList.valueOf(outValue.data),
                          null, // content
                          mask);

          int insetPx = (int) (PADDING_DP * density);
          tv.setForeground(new InsetDrawable(ripple, insetPx, insetPx, insetPx, insetPx));
        }
      }

      Calendar calendar = mDays.get(position);
      tv.setTag(calendar);
      DailyGame.Day day = DailyGame.Day.forCalendar(calendar);

      if (calendar.get(Calendar.MONTH) != mMonth - 1 || calendar.get(Calendar.YEAR) != mYear) {
        tv.setText("");
      } else {
        String text = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        if (mHintDates.contains(day)) {
          text += "*";
        }
        tv.setText(text);

        if (day.equals(mToday)) {
          tv.setTypeface(null, Typeface.BOLD);
          tv.setPaintFlags(tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        } else {
          tv.setTypeface(null, Typeface.NORMAL);
          tv.setPaintFlags(tv.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
        }

        if (day.compareTo(mToday) > 0) {
          tv.setTextColor(mTextSecondaryColor);
        } else {
          tv.setTextColor(mTextPrimaryColor);
        }
      }

      return tv;
    }

    private static int updateAlpha(int color, int alpha) {
      return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }
  }
}
