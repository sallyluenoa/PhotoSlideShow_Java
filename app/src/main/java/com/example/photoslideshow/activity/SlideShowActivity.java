package com.example.photoslideshow.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.photoslideshow.R;
import com.example.photoslideshow.fragment.ListDialogFragment;
import com.example.photoslideshow.task.GetTokenAsyncTask;
import com.example.photoslideshow.utils.PhotosApiUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.List;

public class SlideShowActivity extends AppCompatActivity
    implements  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        GetTokenAsyncTask.ICallback, ListDialogFragment.OnClickListener {

    private static final String TAG = SlideShowActivity.class.getSimpleName();

    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";

    private static final int DIALOG_SELECT_ALBUM = 1;

    private static final String SCOPE_PHOTO_READONLY = "https://www.googleapis.com/auth/photoslibrary.readonly";
    private GoogleApiClient mGoogleApiClient;

    private String mAccountName;
    private String mEmail;

    private String mToken = null;
    private List<PhotosApiUtils.AlbumData> mAlbumList = null;
    private List<PhotosApiUtils.MediaItemData> mItemList = null;

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

        GetTokenAsyncTask task = new GetTokenAsyncTask(getApplicationContext(), mEmail, new String[]{SCOPE_PHOTO_READONLY}, this);
        task.execute();
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
        mToken = token;
        getSharedAlbumList();
    }

    @Override
    public void failedAccessToken() {
        Log.d(TAG, "failedAccessToken");
    }

    @Override
    public void onItemClick(int id, int which) {
        switch (id) {
            case DIALOG_SELECT_ALBUM:
                getMediaItemList(mAlbumList.get(which).id);
                break;
            default:
                break;
        }
    }

    private void getSharedAlbumList() {
        if (mToken == null) {
            Log.w(TAG, "Token must not be null.");
            return;
        }
        findViewById(R.id.progress_layout).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.progress_textView)).setText(R.string.getting_albums_from_google_photo);
        mAlbumList = PhotosApiUtils.getSharedAlbumList(mToken);
        findViewById(R.id.progress_layout).setVisibility(View.GONE);
        showAlbumListDialog();
    }

    private void showAlbumListDialog() {
        if (mAlbumList == null) {
            Log.w(TAG, "Album list must not be null.");
            return;
        }
        ArrayList<String> StrList = new ArrayList<>();
        for (int i=0; i<mAlbumList.size(); i++) {
            StrList.add(mAlbumList.get(i).title);
        }
        ListDialogFragment fragment = ListDialogFragment.newInstance(DIALOG_SELECT_ALBUM, R.string.select_album_dialog_title, StrList);
        fragment.show(getSupportFragmentManager(), TAG);
    }

    private void getMediaItemList(String albumId) {
        if (mToken == null) {
            Log.w(TAG, "Token must not be null.");
            return;
        }
        findViewById(R.id.progress_layout).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.progress_textView)).setText(R.string.getting_media_items_from_google_photo);
        List<PhotosApiUtils.MediaItemData> list = PhotosApiUtils.getMediaItemList(mToken, albumId);
        findViewById(R.id.progress_layout).setVisibility(View.GONE);
        Log.d(TAG, "OK!");
    }

    private void setImageView(Bitmap bitmap) {
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageBitmap(bitmap);
    }

}
