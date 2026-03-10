package com.antsapps.triples;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;
import com.antsapps.triples.backend.Application;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.games.PlayGamesSdk;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public abstract class BaseTriplesActivity extends AppCompatActivity {

  public interface OnSignInListener {
    void onSignInStateChanged(boolean signedInAndConnected);
  }

  private static final String TAG = "SignInActivity";
  private static final int RC_SIGN_IN = 9001;

  protected FirebaseAnalytics mFirebaseAnalytics;
  private FirebaseAuth mFirebaseAuth;
  protected boolean mIsSignedIn = false;

  @Nullable private OnSignInListener mSignInListener;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    applyTheme();
    super.onCreate(savedInstanceState);
    PlayGamesSdk.initialize(this);

    mFirebaseAuth = FirebaseAuth.getInstance();
    mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
  }

  private void applyTheme() {
    String theme =
        PreferenceManager.getDefaultSharedPreferences(this)
            .getString(getString(R.string.pref_theme), "system");
    switch (theme) {
      case "light":
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        break;
      case "dark":
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        break;
      case "system":
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        break;
    }
  }

  @Override
  public void onStart() {
    super.onStart();
  }

  @Override
  protected void onResume() {
    super.onResume();
    signInSilently();
  }

  private void signInSilently() {
    PlayGames.getGamesSignInClient(this)
        .isAuthenticated()
        .addOnCompleteListener(
            task -> {
              boolean isAuthenticated = (task.isSuccessful() && task.getResult().isAuthenticated());
              if (isAuthenticated) {
                Log.d(TAG, "signInSilently: success");
                mIsSignedIn = true;
                mFirebaseAnalytics.logEvent(AnalyticsConstants.Event.SIGN_IN, null);
                onSignInSucceeded();
                // We still want to sign into Firebase if possible.
                // PGS v2 doesn't give us the ID token directly for Firebase.
                // For now, we prioritize PGS v2 migration.
                fetchTokenAndSignInToFirebase();
              } else {
                Log.d(TAG, "signInSilently: failure");
                mIsSignedIn = false;
                onSignInFailed();
              }
            });
  }

  private void fetchTokenAndSignInToFirebase() {
    GoogleSignInOptions gso =
        new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestIdToken(getString(R.string.default_web_client_id))
            .build();
    GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
    googleSignInClient
        .silentSignIn()
        .addOnCompleteListener(
            this,
            task -> {
              if (task.isSuccessful()) {
                GoogleSignInAccount account = task.getResult();
                firebaseAuthWithGoogle(account);
              }
            });
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == RC_SIGN_IN) {
      Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
      if (task.isSuccessful()) {
        firebaseAuthWithGoogle(task.getResult());
        signInSilently();
      }
    }
  }

  private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
    Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

    AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
    mFirebaseAuth
        .signInWithCredential(credential)
        .addOnCompleteListener(
            this,
            task -> {
              if (task.isSuccessful()) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "signInWithCredential:success");
                FirebaseUser user = mFirebaseAuth.getCurrentUser();
                onSignInSucceeded();
              } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "signInWithCredential:failure", task.getException());
                Toast.makeText(
                        BaseTriplesActivity.this, "Authentication failed.", Toast.LENGTH_SHORT)
                    .show();
              }
            });
  }

  public void setSignInListener(OnSignInListener listener) {
    mSignInListener = listener;
  }

  public void onSignInFailed() {
    if (mSignInListener != null) {
      mSignInListener.onSignInStateChanged(isSignedIn());
    }
  }

  public void onSignInSucceeded() {
    if (mSignInListener != null) {
      mSignInListener.onSignInStateChanged(isSignedIn());
    }
    Application application = Application.getInstance(this);
    AchievementManager.syncAchievements(this, application);
    CloudSaveManager.syncWithCloud(this, application);
  }

  public void signIn() {
    PlayGames.getGamesSignInClient(this)
        .signIn()
        .addOnCompleteListener(
            task -> {
              if (task.isSuccessful() && task.getResult().isAuthenticated()) {
                mIsSignedIn = true;
                onSignInSucceeded();
              } else {
                mIsSignedIn = false;
                onSignInFailed();
              }
            });
  }

  public boolean isSignedIn() {
    return mIsSignedIn || mFirebaseAuth.getCurrentUser() != null;
  }

  protected void signOut() {
    // Note: PGS v2 does not support programmatic sign out.
    // We can sign out from Firebase.
    mFirebaseAuth.signOut();
    mFirebaseAnalytics.logEvent(AnalyticsConstants.Event.SIGN_OUT, null);
    mIsSignedIn = false;
    onSignOut();
  }

  @Nullable
  public String getSignedInUserInfo() {
    FirebaseUser user = mFirebaseAuth.getCurrentUser();
    if (user != null) {
      if (user.getEmail() != null && !user.getEmail().isEmpty()) {
        return user.getEmail();
      }
      if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
        return user.getDisplayName();
      }
    }
    return null;
  }

  protected void onSignOut() {
    if (mSignInListener != null) {
      mSignInListener.onSignInStateChanged(isSignedIn());
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
  }
}
