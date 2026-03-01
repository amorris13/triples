package com.antsapps.triples;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.games.Games;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public abstract class BaseTriplesActivity extends AppCompatActivity
    implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

  public interface OnSignInListener {
    void onSignInStateChanged(boolean signedInAndConnected);
  }

  private static final String TAG = "SignInActivity";
  private static final int RC_SIGN_IN = 9001;

  protected FirebaseAnalytics mFirebaseAnalytics;
  private FirebaseAuth mFirebaseAuth;
  protected GoogleApiClient mGoogleApiClient;
  protected GoogleSignInAccount mGoogleSignInAccount;

  @Nullable private OnSignInListener mSignInListener;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Configure sign-in to request the user's ID, email address, and basic
    // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
    GoogleSignInOptions gso =
        new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .build();

    // Build a GoogleApiClient with access to the Google Sign-In API and the
    // options specified by gso.
    mGoogleApiClient =
        new GoogleApiClient.Builder(this)
            .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .addApi(Games.API)
            .addConnectionCallbacks(this)
            .build();

    mFirebaseAuth = FirebaseAuth.getInstance();
    mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
  }

  @Override
  public void onStart() {
    super.onStart();

    OptionalPendingResult<GoogleSignInResult> opr =
        Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
    if (opr.isDone()) {
      // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
      // and the GoogleSignInResult will be available instantly.
      Log.d(TAG, "Got cached sign-in. isConnected: " + mGoogleApiClient.isConnected());
      GoogleSignInResult result = opr.get();
      handleSignInResult(result);
    } else {
      // If the user has not previously signed in on this device or the sign-in has expired,
      // this asynchronous branch will attempt to sign in the user silently.  Cross-device
      // single sign-on will occur in this branch.
      opr.setResultCallback(
          new ResultCallback<GoogleSignInResult>() {
            @Override
            public void onResult(GoogleSignInResult googleSignInResult) {
              handleSignInResult(googleSignInResult);
            }
          });
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
    if (requestCode == RC_SIGN_IN) {
      GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
      Log.d(TAG, "onActivityResult. isConnected: " + mGoogleApiClient.isConnected());
      handleSignInResult(result);
    }
  }

  private void handleSignInResult(GoogleSignInResult result) {
    Log.d(
        TAG,
        "handleSignInResult:"
            + result.isSuccess()
            + " isConnected: "
            + mGoogleApiClient.isConnected());
    if (result.isSuccess()) {
      mGoogleSignInAccount = result.getSignInAccount();
      firebaseAuthWithGoogle(mGoogleSignInAccount);
      mFirebaseAnalytics.logEvent(AnalyticsConstants.Event.SIGN_IN, null);
      onSignInSucceeded();
    } else {
      onSignInFailed();
    }
  }

  private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
    Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

    AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
    mFirebaseAuth
        .signInWithCredential(credential)
        .addOnCompleteListener(
            this,
            new OnCompleteListener<AuthResult>() {
              @Override
              public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                  // Sign in success, update UI with the signed-in user's information
                  Log.d(TAG, "signInWithCredential:success");
                  FirebaseUser user = mFirebaseAuth.getCurrentUser();
                } else {
                  // If sign in fails, display a message to the user.
                  Log.w(TAG, "signInWithCredential:failure", task.getException());
                  Toast.makeText(
                          BaseTriplesActivity.this, "Authentication failed.", Toast.LENGTH_SHORT)
                      .show();
                }
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
  }

  public void signIn() {
    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
    startActivityForResult(signInIntent, RC_SIGN_IN);
  }

  public boolean isSignedIn() {
    return mGoogleApiClient != null
        && mGoogleApiClient.isConnected()
        && mGoogleApiClient.hasConnectedApi(Games.API);
  }

  protected void signOut() {
    mFirebaseAuth.signOut();
    Games.signOut(mGoogleApiClient);
    mFirebaseAnalytics.logEvent(AnalyticsConstants.Event.SIGN_OUT, null);
    Auth.GoogleSignInApi.signOut(mGoogleApiClient)
        .setResultCallback(
            new ResultCallback<Status>() {
              @Override
              public void onResult(Status status) {
                onSignOut();
              }
            });
  }

  protected void onSignOut() {
    if (mSignInListener != null) {
      mSignInListener.onSignInStateChanged(isSignedIn());
    }
  }

  @Override
  public void onConnected(@Nullable Bundle bundle) {
    if (mSignInListener != null) {
      mSignInListener.onSignInStateChanged(isSignedIn());
    }
  }

  @Override
  public void onConnectionSuspended(int i) {
    if (mSignInListener != null) {
      mSignInListener.onSignInStateChanged(isSignedIn());
    }
  }

  @Override
  public void onConnectionFailed(ConnectionResult connectionResult) {
    // An unresolvable error has occurred and Google APIs (including Sign-In) will not
    // be available.
    Log.d(TAG, "onConnectionFailed:" + connectionResult);
    if (mSignInListener != null) {
      mSignInListener.onSignInStateChanged(isSignedIn());
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
  }

  public GoogleApiClient getApiClient() {
    return mGoogleApiClient;
  }
}
