package com.example.photoslideshow.activity;

import android.accounts.Account;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.photoslideshow.R;
import com.example.photoslideshow.task.GetTokenAsyncTask;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;

public class SlideShowActivity extends AppCompatActivity
    implements  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        GetTokenAsyncTask.ICallback {

    private static final String TAG = SlideShowActivity.class.getSimpleName();

    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";

    private static final String SCOPE_PHOTO_READONLY = "https://www.googleapis.com/auth/photoslibrary.readonly";
    private GoogleApiClient mGoogleApiClient;

    private String mAccountName;
    private String mEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_show);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        if (intent != null) {
            mAccountName = intent.getStringExtra(KEY_NAME);
            mEmail = intent.getStringExtra(KEY_EMAIL);
        }

//        Scope scopePhotoReadonly = new Scope(SCOPE_PHOTO_READONLY);
//
//        GoogleSignInOptions gso = new GoogleSignInOptions
//                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestScopes(scopePhotoReadonly)
//                .requestEmail()
//                .build();
//
//        mGoogleApiClient = new GoogleApiClient
//                .Builder(getApplicationContext())
//                .addConnectionCallbacks(this)
//                .enableAutoManage(this, this)
//                .addScope(scopePhotoReadonly)
//                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
//                .build();

        GetTokenAsyncTask task = new GetTokenAsyncTask(getApplicationContext(), mEmail, new String[]{ SCOPE_PHOTO_READONLY }, this);
        task.execute();
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart()");
        super.onStart();

//        if (!mGoogleApiClient.isConnected()) {
//            Log.d(TAG, "try connect");
//            mGoogleApiClient.connect();
//        }
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop()");
        super.onStop();

//        if (mGoogleApiClient.isConnected()) {
//            Log.d(TAG, "isConnected() try disconnect");
//            mGoogleApiClient.disconnect();
//        }
//        if (mGoogleApiClient.isConnecting()) {
//            Log.d(TAG, "isConnecting() try disconnect");
//            mGoogleApiClient.disconnect();
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_slide_show, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Log.d(TAG, "Action Setting!");
                return true;
            case R.id.action_sign_out:
                Log.d(TAG, "Action Sign Out!");
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
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
    }

    @Override
    public void succeedAccessToken(String token) {
        Log.d(TAG, "succeedAccessToken");
    }

    @Override
    public void failedAccessToken() {
        Log.d(TAG, "failedAccessToken");
    }
}
