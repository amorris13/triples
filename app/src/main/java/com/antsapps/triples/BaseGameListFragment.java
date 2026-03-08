package com.antsapps.triples;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.ListFragment;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.OnStateChangedListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public abstract class BaseGameListFragment extends ListFragment implements OnStateChangedListener {

  protected Application mApplication;
  protected ArrayAdapter<Game> mAdapter;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    setHasOptionsMenu(true);
    super.onCreate(savedInstanceState);
  }

  /** Called when the activity is first created. */
  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    mApplication = Application.getInstance(getActivity());

    mAdapter = createArrayAdapter();
    setListAdapter(mAdapter);

    final Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

    getListView()
        .setOnItemLongClickListener(
            new OnItemLongClickListener() {
              @Override
              public boolean onItemLongClick(
                  AdapterView<?> parent, View view, final int position, long id) {
                vibrator.vibrate(50);
                createDeleteAlertDialog((Game) parent.getItemAtPosition(position)).show();
                return true;
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
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
    builder.setCancelable(true);
    builder.setTitle(R.string.delete);
    builder.setPositiveButton(
        R.string.yes,
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            deleteGame(game);
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

  protected abstract void deleteGame(Game game);

  @Override
  public void onStateChanged() {
    updateDataSet();
  }

  protected abstract void updateDataSet();
}
