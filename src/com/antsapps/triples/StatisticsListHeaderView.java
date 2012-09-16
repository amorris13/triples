package com.antsapps.triples;

import java.util.Map;

import android.content.Context;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.graphics.drawable.shapes.Shape;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.GameProperty;
import com.antsapps.triples.backend.ReversableComparator;
import com.google.common.collect.Maps;

public class StatisticsListHeaderView extends FrameLayout {

  private static final Shape DOWN_SHAPE;
  private static final Shape UP_SHAPE;
  private static final Shape UNSELECTED_SHAPE;

  static {
    int size = 50;
    Path path = new Path();
    path.moveTo(size / 2, size / 2);
    path.lineTo(0, 0);
    path.lineTo(size, 0);
    path.close();
    DOWN_SHAPE = new PathShape(path, size, size);

    path = new Path();
    path.reset();
    path.moveTo(size / 2, size / 2);
    path.lineTo(0, size);
    path.lineTo(size, size);
    path.close();
    UP_SHAPE = new PathShape(path, size, size);

    path = new Path();
    path.moveTo(size / 2, size / 2);
    path.lineTo(0, 0);
    path.lineTo(size, 0);
    path.lineTo(size / 2, size / 2);
    path.lineTo(0, size);
    path.lineTo(size, size);
    path.close();
    UNSELECTED_SHAPE = new PathShape(path, size, size);
  }

  private static final int LEFT = 0;
  private static final int RIGHT = 1;

  private class ComparatorChangeOnClickListener implements OnClickListener {

    private final ReversableComparator<Game> mComparator;

    private ComparatorChangeOnClickListener(ReversableComparator<Game> comparator) {
      mComparator = comparator;
    }

    @Override
    public void onClick(View v) {
      if (mComparator == mCurrentComparator) {
        mComparator.reverse();
      }

      setComparator(mComparator);
    }
  }

  private OnComparatorChangeListener<Game> mComparatorChangeListener;
  private final Map<TextView, ReversableComparator<Game>> mComparatorsMap = Maps
      .newHashMap();
  private final Map<TextView, Integer> mPositionsMap = Maps.newHashMap();
  private ReversableComparator<Game> mCurrentComparator;

  public StatisticsListHeaderView(Context context) {
    this(context, null);
  }

  public StatisticsListHeaderView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public StatisticsListHeaderView(Context context,
      AttributeSet attrs,
      int defStyle) {
    super(context, attrs, defStyle);

    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
        Context.LAYOUT_INFLATER_SERVICE);
    View v = inflater.inflate(R.layout.stats_list_header, this);

    TextView dateHeader = (TextView) v.findViewById(R.id.date_header);
    initHeader(dateHeader, GameProperty.DATE, RIGHT);

    TextView timeHeader = (TextView) v.findViewById(R.id.time_header);
    initHeader(timeHeader, GameProperty.TIME_ELAPSED, LEFT);

    setComparator(mComparatorsMap.get(timeHeader));
  }

  private void initHeader(TextView header, GameProperty property, int position) {
    ReversableComparator<Game> reversableComparator = property
        .createReversableComparator();
    mComparatorsMap.put(header, reversableComparator);
    mPositionsMap.put(header, position);
    header.setCompoundDrawablePadding(5);
    header.setOnClickListener(new ComparatorChangeOnClickListener(
        reversableComparator));
  }

  public void setOnComparatorChangeListener(
      OnComparatorChangeListener<Game> listener) {
    mComparatorChangeListener = listener;
    mComparatorChangeListener.onComparatorChange(mCurrentComparator);
  }

  private void styleDrawable(ShapeDrawable dr) {
    dr
        .getPaint().setColor(
            getResources().getColor(android.R.color.darker_gray));
    dr.getPaint().setStyle(Style.FILL_AND_STROKE);
  }

  private void updateDrawableForViews() {
    for (TextView view : mComparatorsMap.keySet()) {
      updateDrawableForView(view);
    }
  }

  private void updateDrawableForView(TextView view) {
    if (mComparatorsMap.get(view) == mCurrentComparator) {
      setDrawableForView(view, createDrawableFromShape(mComparatorsMap
          .get(view).isAscending() ? UP_SHAPE : DOWN_SHAPE));
    } else {
      setDrawableForView(view, createDrawableFromShape(UNSELECTED_SHAPE));
    }
  }

  private void setDrawableForView(TextView view, Drawable dr) {
    switch (mPositionsMap.get(view)) {
      case LEFT:
        view.setCompoundDrawables(dr, null, null, null);
        break;
      case RIGHT:
        view.setCompoundDrawables(null, null, dr, null);
        break;
    }
  }

  private ShapeDrawable createDrawableFromShape(Shape shape) {
    ShapeDrawable dr = new ShapeDrawable(shape);
    styleDrawable(dr);
    int sizePx = getResources()
        .getDimensionPixelSize(R.dimen.heading_text_size);
    dr.setBounds(
        (int) (sizePx * 0.1),
        (int) (sizePx * 0.1),
        (int) (sizePx * 0.9),
        (int) (sizePx * 0.9));
    return dr;
  }

  private void setComparator(ReversableComparator<Game> comparator) {
    mCurrentComparator = comparator;
    if (mComparatorChangeListener != null) {
      mComparatorChangeListener.onComparatorChange(comparator);
    }
    updateDrawableForViews();
  }
}
