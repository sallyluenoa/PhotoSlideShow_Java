package com.example.photoslideshow.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.photoslideshow.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.UserCredentials;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.PhotosLibrarySettings;
import com.google.photos.library.v1.internal.InternalPhotosLibraryClient;
import com.google.photos.library.v1.proto.Album;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String SCOPE_PHOTO_READONLY = "https://www.googleapis.com/auth/photoslibrary.readonly";
    private static final int REQUEST_SIGN_IN = 1;

    private static final String WEB_CLIENT_ID = "676512921990-m725pb5a4bno8grfg5rm1rl2j3tp84b4.apps.googleusercontent.com";
    private static final String WEB_CLIENT_SECRET = "c-3fg5JcmEWTR0FGccR3Z6ch";

    private GoogleApiClient mGoogleApiClient;

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

        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setScopes(gso.getScopeArray());
        signInButton.setOnClickListener(this);

//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

//        showAccountChooser();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Log.d(TAG, "Action Setting!");
//            getPhotoLists();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_SIGN_IN:
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

                TextView textView = (TextView)findViewById(R.id.result_text_view);
                textView.setVisibility(View.VISIBLE);
                if (result.isSuccess()) {
                    GoogleSignInAccount gsia = result.getSignInAccount();
                    Log.d(TAG, "SignIn succeeded.");
                    textView.setText(String.format("ID:%s\nEmail:%s\nDName:%s\nGName:%s\nFName:%s",
                            gsia.getId(), gsia.getEmail(), gsia.getDisplayName(), gsia.getGivenName(), gsia.getFamilyName()));
                } else {
                    Log.d(TAG, "SignIn failed.");
                    textView.setText("SignIn failed. Status: " + result.getStatus().getStatusMessage() + ", Code: " + requestCode);
                }

                findViewById(R.id.sign_in_button).setVisibility(View.GONE);
                findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    private void showAccountChooser() {

//        if (mCredential == null) {
//            Collection<String> scopes = Arrays.asList(SCOPE_PHOTO_READONLY);
//            mCredential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), scopes);
//        }
//        startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_CHOOSER);
    }

    private void showFailedDialog() {
        new AlertDialog.Builder(getApplicationContext())
                .setTitle("Account Error")
                .setMessage("Failed to get account.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();
    }

    private void getPhotoLists() {
        Log.d(TAG, "getPhotoLists()");

        UserCredentials credentials = UserCredentials.newBuilder()
                .setClientId(WEB_CLIENT_ID)
                .setClientSecret(WEB_CLIENT_SECRET)
                .build();

        try {
            PhotosLibrarySettings photosLibrarySettings = PhotosLibrarySettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();

            PhotosLibraryClient photosLibraryClient =
                    PhotosLibraryClient.initialize(photosLibrarySettings);

            InternalPhotosLibraryClient.ListAlbumsPagedResponse response = photosLibraryClient.listAlbums();

            for (Album album : response.iterateAll()) {
                String id = album.getId();
                String title = album.getTitle();
                String productUrl = album.getProductUrl();
                String coverPhotoBaseUrl = album.getCoverPhotoBaseUrl();
                String coverPhotoMediaItemId = album.getCoverPhotoMediaItemId();
                boolean isWritable = album.getIsWriteable();
                long mediaItemsCount = album.getMediaItemsCount();

                Log.d(TAG, String.format("ID: %s\ntitle: %s\nproductURL: %s\nCoverPhotoBaseURL: %s\n" +
                                "CoverPhotoMediaItemID: %s\nisWritable: %b\nMediaItemsCount: %d",
                        id, title, productUrl, coverPhotoBaseUrl, coverPhotoMediaItemId, isWritable, mediaItemsCount));
            }

            photosLibraryClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }



    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        TextView textView = (TextView)findViewById(R.id.result_text_view);
        textView.setVisibility(View.VISIBLE);
        textView.setText("Connection Failed. ErrorMessage: " + connectionResult.getErrorMessage());

        findViewById(R.id.sign_in_button).setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, REQUEST_SIGN_IN);
                break;
            default:
                break;
        }
    }
}
