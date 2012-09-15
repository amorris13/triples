package com.antsapps.triples;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import android.content.Context;
import android.graphics.Color;
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

  private class ComparatorChangeOnClickListener implements OnClickListener {

    private Comparator<Game> mComparator;

    private ComparatorChangeOnClickListener(Comparator<Game> comparator) {
      mComparator = comparator;
    }

    @Override
    public void onClick(View v) {
      TextView textView = (TextView) v;
      if (textView == mHeaderInUse) {
        mComparator = Collections.reverseOrder(mComparator);
        mAscendingMap.put(textView, !mAscendingMap.get(textView));
      }

      mHeaderInUse = textView;
      if (mComparatorChangeListener != null) {
        mComparatorChangeListener.onComparatorChange(mComparator);
      }
      updateDrawableForViews();
    }
  }

  private OnComparatorChangeListener<Game> mComparatorChangeListener;
  private final Map<TextView, Boolean> mAscendingMap = Maps.newHashMap();
  private final TextView mDateHeader;
  private final TextView mTimeHeader;
  private TextView mHeaderInUse;

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

    mDateHeader = (TextView) v.findViewById(R.id.date_header);
    mDateHeader.setOnClickListener(new ComparatorChangeOnClickListener(
        new Game.DateGameComparator()));
    initHeader(mDateHeader);

    mTimeHeader = (TextView) v.findViewById(R.id.time_header);
    mTimeHeader.setOnClickListener(new ComparatorChangeOnClickListener(
        new Game.TimeElapsedGameComparator()));
    initHeader(mTimeHeader);
  }

  private void initHeader(TextView header) {
    header.setCompoundDrawablePadding(5);
    setDrawableForView(header, createDrawableFromShape(UNSELECTED_SHAPE));
    mAscendingMap.put(header, true);
  }

  public void setOnComparatorChangeListener(
      OnComparatorChangeListener<Game> listener) {
    mComparatorChangeListener = listener;
  }

  private void styleDrawable(ShapeDrawable dr) {
    dr.getPaint().setColor(Color.GRAY);
    dr.getPaint().setStyle(Style.FILL_AND_STROKE);
  }

  private void updateDrawableForViews() {
    updateDrawableForView(mDateHeader);
    updateDrawableForView(mTimeHeader);
  }

  private void updateDrawableForView(TextView view) {
    if (view == mHeaderInUse) {
      setDrawableForView(
          view,
          createDrawableFromShape(mAscendingMap.get(view) ? UP_SHAPE
              : DOWN_SHAPE));
    } else {
      setDrawableForView(view, createDrawableFromShape(UNSELECTED_SHAPE));
    }
  }

  private void setDrawableForView(TextView view, Drawable dr) {
    if (view == mDateHeader) {
      view.setCompoundDrawables(null, null, dr, null);
    } else if (view == mTimeHeader) {
      view.setCompoundDrawables(dr, null, null, null);
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
}
