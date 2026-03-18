package com.antsapps.triples.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.antsapps.triples.R;

public class ListTabFragment extends Fragment {

  public static ListTabFragment newInstance() {
    return new ListTabFragment();
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.stats_list_tab, container, false);

    if (getParentFragment() instanceof BaseStatisticsFragment parent) {
      FrameLayout headerContainer = view.findViewById(R.id.list_header_container);
      BaseStatisticsListHeaderView headerView = parent.createStatisticsListHeaderView();
      headerView.setAccentColor(parent.getAccentColor());
      headerView.setOnComparatorChangeListener(parent);
      headerContainer.addView(headerView);

      ListView listView = view.findViewById(android.R.id.list);
      listView.setAdapter(parent.mAdapter);
    }

    return view;
  }
}
