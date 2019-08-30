package com.example.photoslideshow.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.photoslideshow.BuildConfig;
import com.example.photoslideshow.R;
import com.example.photoslideshow.fragment.ConfirmDialogFragment;
import com.example.photoslideshow.utils.GoogleApiUtils;
import com.example.photoslideshow.utils.PreferenceUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ConfirmDialogFragment.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int ACV_REQ_CODE_SIGN_IN = 1;
    private static final int ACV_REQ_CODE_PERMISSION = 2;

    private static final int DLG_ID_FAILED_SIGN_IN = 1;
    private static final int DLG_ID_FAILED_PERMISSION = 2;

    private GoogleApiClient mGoogleApiClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView versionView = findViewById(R.id.version_text_view);
        versionView.setText(getString(R.string.app_version, BuildConfig.VERSION_NAME));

        new Handler().postDelayed(() -> givePermissions(), 2000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: " + requestCode);

        switch (requestCode) {
            case ACV_REQ_CODE_SIGN_IN:
                if (GoogleApiUtils.getSignInAccountFromResultIntent(data) != null) {
                    Log.d(TAG, "SignIn succeeded.");
                    startActivity(new Intent(getApplicationContext(), SlideShowActivity.class));
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
        if (GoogleApiUtils.getCurrentSignInAccount(getApplicationContext()) != null) {
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

        if (mGoogleApiClient == null) {
            String[] scopes = { GoogleApiUtils.SCOPE_PHOTO_READONLY };
            mGoogleApiClient = GoogleApiUtils.initSignInApiClient(this, this, this, scopes);
        }
        GoogleApiUtils.startSignInActivity(this, mGoogleApiClient, ACV_REQ_CODE_SIGN_IN);
    }

    private void showPermissionsFailedDialog() {
        DialogFragment fragment = ConfirmDialogFragment.newInstance(DLG_ID_FAILED_PERMISSION,
                R.string.failed_permissions_dialog_title, R.string.failed_permissions_dialog_message,
                R.string.retry, R.string.close, TAG);
        fragment.setCancelable(false);
        fragment.show(getSupportFragmentManager(), ConfirmDialogFragment.TAG);
    }

    private void showSignInFailedDialog() {
        findViewById(R.id.progress_layout).setVisibility(View.GONE);

        DialogFragment fragment = ConfirmDialogFragment.newInstance(DLG_ID_FAILED_SIGN_IN,
                R.string.failed_sign_in_dialog_title, R.string.failed_sign_in_dialog_message,
                R.string.retry, R.string.close, TAG);
        fragment.setCancelable(false);
        fragment.show(getSupportFragmentManager(), ConfirmDialogFragment.TAG);
    }

    private boolean isPermitted(String permission) {
        return (ContextCompat.checkSelfPermission(getApplicationContext(), permission)
                == PackageManager.PERMISSION_GRANTED);
    }
}
