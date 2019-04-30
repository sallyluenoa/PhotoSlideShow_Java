package com.example.photoslideshow.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.example.photoslideshow.R;
import com.example.photoslideshow.fragment.ConfirmDialogFragment;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener, ConfirmDialogFragment.OnClickListener {

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

        Scope scopePhotoReadonly = new Scope(SCOPE_PHOTO_READONLY);

        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(scopePhotoReadonly)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient
                .Builder(getApplicationContext())
                .enableAutoManage(this, this)
                .addScope(scopePhotoReadonly)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.title_text_view).setVisibility(View.GONE);
                signIn();
            }
        }, 2000);
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
                    Intent slideShowIntent = new Intent(getApplicationContext(), SlideShowActivity.class);
//                    slideShowIntent.putExtra();
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
        Log.d(TAG, "Try sign in.");

        findViewById(R.id.title_text_view).setVisibility(View.GONE);
        findViewById(R.id.progress_layout).setVisibility(View.VISIBLE);

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, REQUEST_SIGN_IN);
    }

    private void showFailedDialog() {
        findViewById(R.id.progress_layout).setVisibility(View.GONE);

        DialogFragment fragment = ConfirmDialogFragment.newInstance(DIALOG_FAILED_SIGN_IN, "title", "message", "yes", "no");
        fragment.show(getSupportFragmentManager(), TAG);
    }
}
