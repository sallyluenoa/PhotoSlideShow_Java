package com.example.photoslideshow.activity;

import android.Manifest;
import android.accounts.Account;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
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

    private static final int ACV_REQ_CODE_SIGN_IN = 1;
    private static final int ACV_REQ_CODE_PERMISSION = 2;

    private static final int DLG_ID_FAILED_SIGN_IN = 1;
    private static final int DLG_ID_FAILED_PERMISSION = 2;

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
                givePermissions();
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
        Log.d(TAG, "onActivityResult: " + requestCode);

        switch (requestCode) {
            case ACV_REQ_CODE_SIGN_IN:
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
                    showSignInFailedDialog();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: " + requestCode);

        switch (requestCode) {
            case ACV_REQ_CODE_PERMISSION:
                boolean permission = true;
                for (int i=0; i<grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        permission = false;
                        break;
                    }
                }
                if (permission) signIn();
                else showPermissionsFailedDialog();
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
        showSignInFailedDialog();
    }

    @Override
    public void onPositiveClick(int id) {
        switch (id) {
            case DLG_ID_FAILED_SIGN_IN:
                signIn();
                break;
            case DLG_ID_FAILED_PERMISSION:
                givePermissions();
                break;
            default:
                break;
        }
    }

    @Override
    public void onNegativeClick(int id) {
        switch (id) {
            case DLG_ID_FAILED_SIGN_IN:
            case DLG_ID_FAILED_PERMISSION:
                finish();
                break;
            default:
                break;
        }
    }

    private void givePermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.d(TAG, "No need to ask users. Skip giving permissions.");
            signIn();
            return;
        }
        if(isPermitted(Manifest.permission.READ_EXTERNAL_STORAGE) &&
            isPermitted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Log.d(TAG, "Already gave. Skip giving permissions.");
            signIn();
            return;
        }
        Log.d(TAG, "Need to ask users.");
        ActivityCompat.requestPermissions(this,
                new String[] {
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                },
                ACV_REQ_CODE_PERMISSION);
    }

    private void signIn() {
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
        startActivityForResult(signInIntent, ACV_REQ_CODE_SIGN_IN);
    }

    private void showPermissionsFailedDialog() {
        DialogFragment fragment = ConfirmDialogFragment.newInstance(DLG_ID_FAILED_PERMISSION,
                R.string.failed_permissions_dialog_title, R.string.failed_permissions_dialog_message,
                R.string.retry, R.string.close);
        fragment.show(getSupportFragmentManager(), TAG);
    }

    private void showSignInFailedDialog() {
        findViewById(R.id.progress_layout).setVisibility(View.GONE);

        DialogFragment fragment = ConfirmDialogFragment.newInstance
                (DLG_ID_FAILED_SIGN_IN,
                R.string.failed_sign_in_dialog_title, R.string.failed_sign_in_dialog_message,
                R.string.retry, R.string.close);
        fragment.show(getSupportFragmentManager(), TAG);
    }

    private boolean isPermitted(String permission) {
        return (ContextCompat.checkSelfPermission(getApplicationContext(), permission)
                == PackageManager.PERMISSION_GRANTED);
    }
}
