package com.antsapps.triples;

import java.util.Collections;
import java.util.Comparator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.Game.CardsRemainingGameComparator;
import com.antsapps.triples.backend.Game.DateGameComparator;
import com.antsapps.triples.backend.Game.TimeElapsedGameComparator;
import com.antsapps.triples.backend.OnStateChangedListener;

public abstract class GameListFragment extends SherlockListFragment implements
    OnStateChangedListener {

  protected Application mApplication;
  protected ArrayAdapter<Game> mAdapter;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    setHasOptionsMenu(true);
    // TODO Auto-generated method stub
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    return inflater.inflate(R.layout.game_list_fragment, null);
  }

  /** Called when the activity is first created. */
  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mApplication = Application.getInstance(getSherlockActivity());

    mAdapter = createArrayAdapter();
    setListAdapter(mAdapter);
    ((TextView) getView().findViewById(android.R.id.empty))
        .setText(getEmptyText());

    ListView lv = getListView();
    lv.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position,
          long id) {
        Game game = (Game) parent.getItemAtPosition(position);
        if (game != null) {
          Intent intent = new Intent(view.getContext(), GameActivity.class);
          intent.putExtra(Game.ID_TAG, game.getId());
          startActivity(intent);
        }
      }
    });

    final Vibrator vibrator = (Vibrator) getSherlockActivity()
        .getSystemService(Context.VIBRATOR_SERVICE);

    lv.setOnItemLongClickListener(new OnItemLongClickListener() {
      @Override
      public boolean onItemLongClick(AdapterView<?> parent, View view,
          final int position, long id) {
        vibrator.vibrate(100);
        AlertDialog alert = createDeleteAlertDialog((Game) parent
            .getItemAtPosition(position));
        alert.show();
        return false;
      }
    });
  }

  @Override
  public void onStart() {
    super.onStart();
    mApplication.addOnStateChangedListener(this);
  }

  @Override
  public void onStop() {
    super.onStop();
    mApplication.removeOnStateChangedListener(this);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.fragment_list, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    switch (item.getItemId()) {
      case R.id.sort:
        AlertDialog alert = createSortAlertDialog();
        alert.show();
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  protected abstract String getEmptyText();

  protected abstract ArrayAdapter<Game> createArrayAdapter();

  private AlertDialog createDeleteAlertDialog(final Game game) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
    builder.setCancelable(true);
    builder.setTitle(R.string.delete);
    builder.setInverseBackgroundForced(true);
    builder.setPositiveButton(
        R.string.yes,
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            mApplication.deleteGame(game);
            dialog.dismiss();
          }
        });
    builder.setNegativeButton(
        R.string.no,
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }
        });
    AlertDialog alert = builder.create();
    return alert;
  }

  private AlertDialog createSortAlertDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
    builder.setCancelable(true);
    builder.setTitle(R.string.sort_by);
    builder.setInverseBackgroundForced(true);
    builder.setSingleChoiceItems(new CharSequence[] { "Date", "Time Elapsed",
        "Cards Remaining" }, 0, new OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
      }
    });
    builder.setPositiveButton(
        R.string.ascending,
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            int selectedItemPosition = ((AlertDialog) dialog)
                .getListView().getCheckedItemPosition();
            Comparator<Game> comparator = getComparator(selectedItemPosition);
            mAdapter.sort(comparator);
            dialog.dismiss();
          }
        });
    builder.setNegativeButton(
        R.string.descending,
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            int selectedItemPosition = ((AlertDialog) dialog)
                .getListView().getCheckedItemPosition();
            Comparator<Game> comparator = getComparator(selectedItemPosition);
            mAdapter.sort(Collections.reverseOrder(comparator));
            dialog.dismiss();
          }
        });
    AlertDialog alert = builder.create();
    return alert;
  }

  @Override
  public void onStateChanged() {
    updateDataSet();
  }

  protected abstract void updateDataSet();

  private Comparator<Game> getComparator(int which) {
    Log.i("GLF", "which  = " + which);
    Comparator<Game> comparator = null;
    switch (which) {
      case 0:
        comparator = new DateGameComparator();
        break;
      case 1:
        comparator = new TimeElapsedGameComparator();
        break;
      case 2:
        comparator = new CardsRemainingGameComparator();
        break;
    }
    return comparator;
  }
}