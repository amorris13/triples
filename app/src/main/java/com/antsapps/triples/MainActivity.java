package com.antsapps.triples;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends BaseTriplesActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();
                if (itemId == R.id.nav_classic) {
                    selectedFragment = new ClassicGamesFragment();
                } else if (itemId == R.id.nav_arcade) {
                    selectedFragment = new ArcadeGamesFragment();
                } else if (itemId == R.id.nav_tutorial) {
                    selectedFragment = new TutorialFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                    return true;
                }
                return false;
            }
        });

        // Set default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ClassicGamesFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.game_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.signout).setVisible(isSignedIn());
        // Remove the "new_game" item from the options menu since we have it in the fragments
        menu.findItem(R.id.new_game).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.help) {
            Intent helpIntent = new Intent(getBaseContext(), HelpActivity.class);
            startActivity(helpIntent);
            return true;
        } else if (itemId == R.id.settings) {
            Intent settingsIntent = new Intent(getBaseContext(), SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        } else if (itemId == R.id.signout) {
            signOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSignInSucceeded() {
        super.onSignInSucceeded();
        invalidateOptionsMenu();
    }

    @Override
    public void onSignOut() {
        super.onSignOut();
        invalidateOptionsMenu();
    }
}
