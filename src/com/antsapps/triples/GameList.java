package com.antsapps.triples;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.Game;
import com.google.common.collect.Lists;

public class GameList extends SherlockListActivity {
  private Application application;

  private List<Game> mGames = null;
  private TableArrayAdapter mAdapter;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.game_list);
    application = Application.setInstance(getBaseContext());

    mGames = Lists.reverse(application.getGames());
    mAdapter = new TableArrayAdapter(this, R.layout.game_list_item, mGames);
    setListAdapter(mAdapter);

    ListView lv = getListView();
    lv.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position,
          long id) {
        Game game = mGames.get(position);
        Intent intent = new Intent(view.getContext(), GameActivity.class);
        intent.putExtra(Game.ID_TAG, game.getId());
        startActivity(intent);
      }
    });

    lv.setOnItemLongClickListener(new OnItemLongClickListener() {
      @Override
      public boolean onItemLongClick(AdapterView<?> parent, View view,
          final int position, long id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(GameList.this);
        builder.setCancelable(true);
        builder.setTitle("Delete?");
        builder.setInverseBackgroundForced(true);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            Game game = mGames.get(position);
            application.deleteGame(game);
            dialog.dismiss();
          }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }
        });
        AlertDialog alert = builder.create();
        alert.show();
        return false;
      }
    });
  }

  @Override
  public void onStart() {
    super.onStart();
    TableArrayAdapter adapter = (TableArrayAdapter) getListView().getAdapter();
    adapter.notifyDataSetChanged();
  }

  @Override
  protected void onResume() {
    super.onResume();
    TableArrayAdapter adapter = (TableArrayAdapter) getListView().getAdapter();
    adapter.notifyDataSetChanged();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getSupportMenuInflater();
    inflater.inflate(R.menu.game_list, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    switch (item.getItemId()) {
      case R.id.new_game:
        Intent intent = new Intent(getBaseContext(), Game.class);
        startActivity(intent);
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private class TableArrayAdapter extends ArrayAdapter<Game> {

    private final List<Game> games;

    public TableArrayAdapter(Context context,
        int textViewResourceId,
        List<Game> games) {
      super(context, textViewResourceId, games);
      this.games = games;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View v = convertView;
      if (v == null) {
        LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = vi.inflate(R.layout.game_list_item, null);
      }

      Game m = games.get(position);
      if (m != null) {
        //TODO: Fill in the game list item.
      }

      return v;

    }

    private void setBold(View v, int id) {
      ((TextView) v.findViewById(id)).setTypeface(Typeface.DEFAULT,
          Typeface.BOLD);
    }

    private void setNormal(View v, int id) {
      ((TextView) v.findViewById(id)).setTypeface(Typeface.DEFAULT,
          Typeface.NORMAL);
    }
  }
}