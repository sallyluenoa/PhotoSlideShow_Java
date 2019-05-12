package com.example.photoslideshow.activity;

import android.accounts.Account;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.photoslideshow.BuildConfig;
import com.example.photoslideshow.R;
import com.example.photoslideshow.fragment.ConfirmDialogFragment;
import com.example.photoslideshow.utils.PreferenceUtils;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ConfirmDialogFragment.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String SCOPE_PHOTO_READONLY = "https://www.googleapis.com/auth/photoslibrary.readonly";
    private static final int REQUEST_SIGN_IN = 1;

    private static final int DIALOG_FAILED_SIGN_IN = 1;

    private GoogleApiClient mGoogleApiClient;

    final Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView versionView = (TextView) findViewById(R.id.version_text_view);
        versionView.setText("v" + BuildConfig.VERSION_NAME);

        Scope scopePhotoReadonly = new Scope(SCOPE_PHOTO_READONLY);

        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(scopePhotoReadonly)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient
                .Builder(getApplicationContext())
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addScope(scopePhotoReadonly)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                signIn();
            }
        }, 2000);
    }


    @Override
    public void onStart() {
        Log.d(TAG, "onStart()");
        super.onStart();

        if (!mGoogleApiClient.isConnected()) {
            Log.d(TAG, "try connect");
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mGoogleApiClient.isConnected()) {
            Log.d(TAG, "try disconnect");
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult");

        switch (requestCode) {
            case REQUEST_SIGN_IN:
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

                if (result.isSuccess()) {
                    Log.d(TAG, "SignIn succeeded.");
                    GoogleSignInAccount gsia = result.getSignInAccount();
                    String accountName = gsia.getDisplayName();
                    String email = gsia.getEmail();
                    PreferenceUtils.putAccountInfo(getApplicationContext(), accountName, email);
                    Intent slideShowIntent = new Intent(getApplicationContext(), SlideShowActivity.class);
                    slideShowIntent.putExtra(SlideShowActivity.KEY_EMAIL, email);
                    startActivity(slideShowIntent);
                    finish();
                } else {
                    Log.d(TAG, "SignIn failed.");
                    showFailedDialog();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed. errorMessage=" + connectionResult.getErrorMessage());
        showFailedDialog();
    }

    @Override
    public void onPositiveClick(int id) {
        switch (id) {
            case DIALOG_FAILED_SIGN_IN:
                signIn();
                break;
            default:
                break;
        }
    }

    @Override
    public void onNegativeClick(int id) {
        switch (id) {
            case DIALOG_FAILED_SIGN_IN:
                finish();
                break;
            default:
                break;
        }
    }

    public void signIn() {
        if (PreferenceUtils.hasAccountInfo(getApplicationContext())) {
            Log.d(TAG, "Already have account info. Skip sign in.");
            Intent slideShowIntent = new Intent(getApplicationContext(), SlideShowActivity.class);
            startActivity(slideShowIntent);
            finish();
            return;
        }

        Log.d(TAG, "Try sign in.");

        findViewById(R.id.title_text_view).setVisibility(View.GONE);
        findViewById(R.id.version_text_view).setVisibility(View.GONE);
        findViewById(R.id.progress_layout).setVisibility(View.VISIBLE);

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, REQUEST_SIGN_IN);
    }

    private void showFailedDialog() {
        findViewById(R.id.progress_layout).setVisibility(View.GONE);

        DialogFragment fragment = ConfirmDialogFragment.newInstance(DIALOG_FAILED_SIGN_IN,
                R.string.failed_sign_in_dialog_title, R.string.failed_sign_in_dialog_message,
                R.string.retry, R.string.close);
        fragment.show(getSupportFragmentManager(), TAG);
    }
}
