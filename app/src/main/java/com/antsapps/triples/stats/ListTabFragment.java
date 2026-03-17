package com.antsapps.triples.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.antsapps.triples.R;
import com.antsapps.triples.backend.Game;

public class ListTabFragment extends Fragment {

  private BaseStatisticsListHeaderView mHeaderView;
  private ArrayAdapter<Game> mAdapter;
  private OnComparatorChangeListener<Game> mComparatorChangeListener;

  public static ListTabFragment newInstance() {
    return new ListTabFragment();
  }

  public void setHeaderView(BaseStatisticsListHeaderView headerView) {
    mHeaderView = headerView;
  }

  public void setAdapter(ArrayAdapter<Game> adapter) {
    mAdapter = adapter;
  }

  public void setOnComparatorChangeListener(OnComparatorChangeListener<Game> listener) {
    mComparatorChangeListener = listener;
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.stats_list_tab, container, false);

    FrameLayout headerContainer = view.findViewById(R.id.list_header_container);
    if (mHeaderView != null) {
      if (mHeaderView.getParent() != null) {
        ((ViewGroup) mHeaderView.getParent()).removeView(mHeaderView);
      }
      headerContainer.addView(mHeaderView);
      mHeaderView.setOnComparatorChangeListener(mComparatorChangeListener);
    }

    ListView listView = view.findViewById(android.R.id.list);
    listView.setAdapter(mAdapter);

    return view;
  }
}
