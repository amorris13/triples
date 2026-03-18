package com.antsapps.triples;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.DailyGame;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.util.CsvExportable;
import com.antsapps.triples.util.CsvUtil;
import com.antsapps.triples.util.ShareUtil;
import com.google.android.material.button.MaterialButton;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class DailyStatisticsFragment extends Fragment implements CsvExportable {

  private Application mApplication;
  private DailyStatisticsViewModel mViewModel;
  private List<DailyGame> mDailyGames;
  private ViewPager2 mPager;
  private MonthPagerAdapter mPagerAdapter;
  private ViewSwitcher mMonthSwitcher;
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
  private int mLastPosition = -1;

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    mApplication = Application.getInstance(getActivity());
    mViewModel = new ViewModelProvider(this).get(DailyStatisticsViewModel.class);
    mViewModel.init(mApplication);
    View view = inflater.inflate(R.layout.daily_stats_fragment, container, false);

    mMonthSwitcher = view.findViewById(R.id.month_title_switcher);
    mMonthSwitcher.setFactory(
        () -> {
          MaterialButton btn =
              new MaterialButton(
                  new ContextThemeWrapper(
                      getActivity(),
                      com.google.android.material.R.style.Widget_Material3_Button_TextButton),
                  null,
                  com.google.android.material.R.attr.borderlessButtonStyle);
          btn.setGravity(Gravity.CENTER);
          btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
          btn.setTypeface(null, Typeface.BOLD);
          btn.setTextColor(ContextCompat.getColor(getActivity(), R.color.selector_month_nav));
          btn.setOnClickListener(
              v -> {
                mSelectedDay = DailyGame.Day.forToday();
                mPager.setCurrentItem(mPagerAdapter.getPositionForDay(mSelectedDay), true);
                refreshVisibleCalendars();
                updateDetailSection();
              });
          return btn;
        });

    mPager = view.findViewById(R.id.calendar_pager);
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

    mSelectedDay = DailyGame.Day.forToday();

    mPagerAdapter = new MonthPagerAdapter();
    mPager.setAdapter(mPagerAdapter);
    mPager.setOffscreenPageLimit(1);
    mLastPosition = mPagerAdapter.getPositionForDay(mSelectedDay);
    mPager.setCurrentItem(mLastPosition, false);
    mPager.registerOnPageChangeCallback(
        new ViewPager2.OnPageChangeCallback() {
          @Override
          public void onPageSelected(int position) {
            updateCalendarHeader(position);
            mLastPosition = position;
          }
        });

    view.findViewById(R.id.prev_month)
        .setOnClickListener(
            v -> {
              mPager.setCurrentItem(mPager.getCurrentItem() - 1, true);
            });

    mNextMonthBtn.setOnClickListener(
        v -> {
          mPager.setCurrentItem(mPager.getCurrentItem() + 1, true);
        });

    mDailyGames = new ArrayList<>();

    mViewModel
        .getDailyGames()
        .observe(
            getViewLifecycleOwner(),
            games -> {
              mDailyGames = games;
              refreshVisibleCalendars();
              updateDetailSection();
            });

    mViewModel
        .getDailyStatistics()
        .observe(
            getViewLifecycleOwner(),
            stats -> {
              mCurrentStreakTv.setText(String.valueOf(stats.currentStreak));
              mLongestStreakTv.setText(String.valueOf(stats.longestStreak));
              mTotalSolvedTv.setText(String.valueOf(stats.totalGamesCompleted));
            });

    updateCalendarHeader(mLastPosition);
    updateDetailSection();

    return view;
  }

  private void refreshVisibleCalendars() {
    View internalRecyclerView = mPager.getChildAt(0);
    if (internalRecyclerView instanceof RecyclerView rv) {
      for (int i = 0; i < rv.getChildCount(); i++) {
        View child = rv.getChildAt(i);
        RecyclerView.ViewHolder holder = rv.getChildViewHolder(child);
        if (holder instanceof MonthPagerAdapter.MonthViewHolder monthViewHolder) {
          RecyclerView grid = monthViewHolder.grid;
          if (grid != null && grid.getAdapter() instanceof CalendarAdapter adapter) {
            adapter.updateData(mDailyGames);
          }
        }
      }
    }
  }

  private void updateCalendarHeader(int position) {
    if (mLastPosition != -1 && position != mLastPosition) {
      boolean movingRight = position > mLastPosition;
      mMonthSwitcher.setInAnimation(createSlideAnim(true, movingRight));
      mMonthSwitcher.setOutAnimation(createSlideAnim(false, movingRight));
    } else {
      mMonthSwitcher.setInAnimation(null);
      mMonthSwitcher.setOutAnimation(null);
    }

    Calendar month = mPagerAdapter.getMonthAt(position);
    SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    ((Button) mMonthSwitcher.getNextView()).setText(sdf.format(month.getTime()));
    mMonthSwitcher.showNext();

    Calendar now = Application.getTimeProvider().getCalendar();
    boolean isCurrentMonth =
        month.get(Calendar.YEAR) == now.get(Calendar.YEAR)
            && month.get(Calendar.MONTH) == now.get(Calendar.MONTH);

    mNextMonthBtn.setEnabled(!isCurrentMonth);
  }

  private Animation createSlideAnim(boolean appear, boolean slideLeft) {
    float fromX = appear ? (slideLeft ? 1.0f : -1.0f) : 0;
    float toX = appear ? 0 : (slideLeft ? -1.0f : 1.0f);

    AnimationSet set = new AnimationSet(true);
    set.addAnimation(
        new TranslateAnimation(
            Animation.RELATIVE_TO_PARENT,
            fromX,
            Animation.RELATIVE_TO_PARENT,
            toX,
            Animation.RELATIVE_TO_PARENT,
            0,
            Animation.RELATIVE_TO_PARENT,
            0));
    set.addAnimation(new AlphaAnimation(appear ? 0.0f : 1.0f, appear ? 1.0f : 0.0f));
    set.setDuration(200);
    return set;
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
        getActivity(), "daily_statistics.csv", CsvUtil.getDailyCsvContent(mDailyGames));
  }

  private static class DayViewHolder extends RecyclerView.ViewHolder {
    public DayViewHolder(@NonNull View itemView) {
      super(itemView);
    }
  }

  private class CalendarAdapter extends RecyclerView.Adapter<DayViewHolder> {
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
    private final Map<DailyGame.Day, Float> mProgressMap;

    CalendarAdapter(Context context, Calendar month, List<DailyGame> allGames) {
      mContext = context;
      mTextPrimaryColor = ContextCompat.getColor(mContext, R.color.color_text_primary);
      mTextSecondaryColor = ContextCompat.getColor(mContext, R.color.color_text_secondary);

      mMonth = month.get(Calendar.MONTH) + 1;
      mYear = month.get(Calendar.YEAR);
      mToday = DailyGame.Day.forToday();

      mDays = new ArrayList<>();
      Calendar cal = (Calendar) month.clone();
      cal.set(Calendar.DAY_OF_MONTH, 1);
      cal.set(Calendar.HOUR_OF_DAY, 0);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.SECOND, 0);
      cal.set(Calendar.MILLISECOND, 0);
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
      updateDataInternal(allGames);
    }

    void updateData(List<DailyGame> allGames) {
      updateDataInternal(allGames);
      notifyDataSetChanged();
    }

    private void updateDataInternal(List<DailyGame> allGames) {
      mCompletedOnDayDates.clear();
      mCompletedLateDates.clear();
      mHintDates.clear();
      mProgressMap.clear();
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

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      float density = mContext.getResources().getDisplayMetrics().density;

      FrameLayout container = new FrameLayout(mContext);
      container.setLayoutParams(
          new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (48 * density)));

      TextView tv =
          new androidx.appcompat.widget.AppCompatTextView(mContext) {
            private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

            @Override
            protected void onDraw(Canvas canvas) {
              Object tag = getTag();
              if (!(tag instanceof Calendar)) return;
              DailyGame.Day day = DailyGame.Day.forCalendar((Calendar) tag);
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
              if (day.equals(mSelectedDay)) {
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(2 * density);
                mPaint.setColor(ContextCompat.getColor(mContext, R.color.color_text_primary));
                canvas.drawCircle(centerX, centerY, radius + density, mPaint);
              }

              super.onDraw(canvas);
            }
          };

      FrameLayout.LayoutParams tvParams =
          new FrameLayout.LayoutParams((int) (48 * density), (int) (48 * density));
      tvParams.gravity = Gravity.CENTER;
      tv.setLayoutParams(tvParams);
      tv.setGravity(Gravity.CENTER);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        TypedValue outValue = new TypedValue();
        mContext.getTheme().resolveAttribute(android.R.attr.colorControlHighlight, outValue, true);

        Drawable mask = new ShapeDrawable(new OvalShape());
        RippleDrawable ripple =
            new RippleDrawable(
                ColorStateList.valueOf(outValue.data),
                null, // content
                mask);

        int insetPx = (int) (PADDING_DP * density);
        tv.setForeground(new InsetDrawable(ripple, insetPx, insetPx, insetPx, insetPx));
      }

      //      container.addView(tv);
      return new DayViewHolder(tv);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
      //      FrameLayout container = (FrameLayout) holder.itemView;
      //      TextView tv = (TextView) container.getChildAt(0);
      TextView tv = (TextView) holder.itemView;
      Calendar calendar = mDays.get(position);
      tv.setTag(calendar);
      DailyGame.Day day = DailyGame.Day.forCalendar(calendar);

      boolean enabled = isEnabled(position);
      tv.setEnabled(enabled);
      tv.setClickable(enabled);
      tv.setFocusable(enabled);
      if (enabled) {
        tv.setOnClickListener(
            v -> {
              mSelectedDay = DailyGame.Day.forCalendar(calendar);
              refreshVisibleCalendars();
              updateDetailSection();
            });
      }

      if (calendar.get(Calendar.MONTH) != mMonth - 1 || calendar.get(Calendar.YEAR) != mYear) {
        tv.setText("");
        tv.setOnClickListener(null);
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
    }

    @Override
    public int getItemCount() {
      return mDays.size();
    }

    public boolean isEnabled(int position) {
      Calendar day = mDays.get(position);
      return day.get(Calendar.YEAR) == mYear
          && day.get(Calendar.MONTH) == mMonth - 1
          && day.getTimeInMillis() <= Application.getTimeProvider().currentTimeMillis();
    }

    private int updateAlpha(int color, int alpha) {
      return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }
  }

  private class MonthPagerAdapter extends RecyclerView.Adapter<MonthPagerAdapter.MonthViewHolder> {
    private final Calendar mStartMonth;
    private final int mCount;

    MonthPagerAdapter() {
      Calendar earliest = Application.getTimeProvider().getCalendar();
      earliest.set(1970, Calendar.JANUARY, 1);
      for (DailyGame game : mApplication.getDailyGames()) {
        if (game.getGameDay().getCalendar().before(earliest)) {
          earliest = game.getGameDay().getCalendar();
        }
      }
      mStartMonth = earliest;
      mStartMonth.set(Calendar.DAY_OF_MONTH, 1);
      mStartMonth.set(Calendar.HOUR_OF_DAY, 0);
      mStartMonth.set(Calendar.MINUTE, 0);
      mStartMonth.set(Calendar.SECOND, 0);
      mStartMonth.set(Calendar.MILLISECOND, 0);

      Calendar now = Application.getTimeProvider().getCalendar();
      int yearDiff = now.get(Calendar.YEAR) - mStartMonth.get(Calendar.YEAR);
      int monthDiff = now.get(Calendar.MONTH) - mStartMonth.get(Calendar.MONTH);
      mCount = yearDiff * 12 + monthDiff + 1;
    }

    @NonNull
    @Override
    public MonthViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view =
          LayoutInflater.from(getActivity())
              .inflate(R.layout.daily_stats_month_page, parent, false);
      return new MonthViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MonthViewHolder holder, int position) {
      Calendar month = (Calendar) mStartMonth.clone();
      month.add(Calendar.MONTH, position);
      holder.bind(month);
    }

    @Override
    public int getItemCount() {
      return mCount;
    }

    Calendar getMonthAt(int position) {
      Calendar month = (Calendar) mStartMonth.clone();
      month.add(Calendar.MONTH, position);
      return month;
    }

    int getPositionForDay(DailyGame.Day day) {
      Calendar cal = day.getCalendar();
      int yearDiff = cal.get(Calendar.YEAR) - mStartMonth.get(Calendar.YEAR);
      int monthDiff = cal.get(Calendar.MONTH) - mStartMonth.get(Calendar.MONTH);
      return yearDiff * 12 + monthDiff;
    }

    class MonthViewHolder extends RecyclerView.ViewHolder {
      RecyclerView grid;

      MonthViewHolder(View itemView) {
        super(itemView);
        grid = itemView.findViewById(R.id.month_grid);
        grid.setLayoutManager(new GridLayoutManager(getActivity(), 7));
        grid.addItemDecoration(
            new RecyclerView.ItemDecoration() {
              @Override
              public void getItemOffsets(
                  @NonNull Rect outRect,
                  @NonNull View view,
                  @NonNull RecyclerView parent,
                  @NonNull RecyclerView.State state) {
                int spacing = (int) (4 * getActivity().getResources().getDisplayMetrics().density);
                outRect.left = spacing / 2;
                outRect.right = spacing / 2;
                outRect.top = spacing / 2;
                outRect.bottom = spacing / 2;
              }
            });
      }

      void bind(Calendar month) {
        grid.setAdapter(new CalendarAdapter(getActivity(), month, mDailyGames));
      }
    }
  }
}
