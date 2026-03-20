package com.antsapps.triples.views;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import androidx.test.core.app.ApplicationProvider;
import com.antsapps.triples.BaseRobolectricTest;
import com.antsapps.triples.R;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class TripleExplanationViewTest extends BaseRobolectricTest {

  @Test
  public void testInitialState_isCorrect() {
    Context context = ApplicationProvider.getApplicationContext();
    TripleExplanationView view = new TripleExplanationView(context);

    ViewGroup table = view.findViewById(R.id.explanation_table);
    assertThat(table).isNotNull();

    // Row 0 is header.
    // Row 1, 2, 3 are card rows.
    for (int i = 1; i <= 3; i++) {
      ViewGroup row = (ViewGroup) table.getChildAt(i);
      // First child is SingleScaledCardView
      assertThat(row.getChildAt(0).getVisibility()).isEqualTo(View.INVISIBLE);
      // Next 4 are PropertyIllustrationViews
      for (int j = 1; j <= 4; j++) {
        assertThat(row.getChildAt(j).getVisibility()).isEqualTo(View.INVISIBLE);
      }
    }
  }
}
