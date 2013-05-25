package com.antsapps.triples;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.Game;
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

  /** Called when the activity is first created. */
  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mApplication = Application.getInstance(getSherlockActivity());

    mAdapter = createArrayAdapter();
    setListAdapter(mAdapter);

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
    updateDataSet();
  }

  @Override
  public void onStop() {
    super.onStop();
    mApplication.removeOnStateChangedListener(this);
  }

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
            mApplication.deleteClassicGame(game);
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

  @Override
  public void onStateChanged() {
    updateDataSet();
  }

  protected abstract void updateDataSet();
}