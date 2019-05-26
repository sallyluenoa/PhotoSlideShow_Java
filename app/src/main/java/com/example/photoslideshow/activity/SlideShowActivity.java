package com.example.photoslideshow.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
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
import com.example.photoslideshow.list.AlbumList;
import com.example.photoslideshow.list.MediaItemList;
import com.example.photoslideshow.manager.DownloadFilesManager;
import com.example.photoslideshow.task.GetAccessTokenAsyncTask;
import com.example.photoslideshow.task.GetMediaItemListAsyncTask;
import com.example.photoslideshow.task.GetSharedAlbumListAsyncTask;
import com.example.photoslideshow.utils.GoogleApiUtils;
import com.example.photoslideshow.utils.PreferenceUtils;

public class SlideShowActivity extends AppCompatActivity
    implements  ListDialogFragment.OnClickListener,
        GetAccessTokenAsyncTask.ICallback, GetSharedAlbumListAsyncTask.ICallback, GetMediaItemListAsyncTask.ICallback {

    private static final String TAG = SlideShowActivity.class.getSimpleName();

    public static final String KEY_EMAIL = "email";

    private static final int DLG_ID_SELECT_ALBUM = 1;

    private String mToken = null;
    private AlbumList mAlbumList = null;
    private MediaItemList mMediaItemList = null;

    private DownloadFilesManager mDownloadFilesManager = null;

    private int mSelectedAlbumIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_show);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String email = null;
        Intent intent = getIntent();
        if (intent != null) {
            email = intent.getStringExtra(KEY_EMAIL);
        }
        startGetAccessToken(email);
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
    public void onItemClick(int id, int which) {
        switch (id) {
            case DLG_ID_SELECT_ALBUM:
                mSelectedAlbumIndex = which;
                startGetMediaItemList();
                break;
            default:
                break;
        }
    }

    @Override
    public void onAccessTokenResult(String token) {
        if (token != null) {
            Log.d(TAG, "Succeed to get access token.");
            mToken = token;
            startGetSharedAlbumList();
        } else {
            Log.d(TAG, "Failed to get access token.");
        }
    }

    @Override
    public void onAlbumListResult(AlbumList list) {
        if (list != null) {
            Log.d(TAG, "Succeeded to get Album list.");
            mAlbumList = list;
            showAlbumListDialog();
        } else {
            Log.d(TAG, "Failed to get Album list.");
        }
    }

    @Override
    public void onMediaItemListResult(MediaItemList list) {
        if (list != null) {
            Log.d(TAG, "Succeeded to get MediaItem list.");
            startDownloadFiles();
        } else {
            Log.d(TAG, "Failed to get MediaItem list.");
        }
    }

    private void startGetAccessToken(String email) {
        if (email == null) {
            email = PreferenceUtils.getEmail(getApplicationContext());
        }
        showProgress(R.string.getting_access_token_from_google_api);
        GetAccessTokenAsyncTask.start(getApplicationContext(), email, GoogleApiUtils.SCOPE_PHOTO_READONLY, this);
    }

    private void startGetSharedAlbumList() {
        if (isNullAccessToken()) return;

        showProgress(R.string.getting_albums_from_google_photo);
        GetSharedAlbumListAsyncTask.start(mToken, this);
    }

    private void startGetMediaItemList() {
        if (isNullAccessToken()) return;
        if (isNullAlbumList()) return;
        if (isNotSelectedAlbumIndex()) return;

        showProgress(R.string.getting_media_items_from_google_photo);
        GetMediaItemListAsyncTask.start(mToken, mAlbumList.get(mSelectedAlbumIndex), this);
    }

    private void startDownloadFiles() {
        if (isNullMediaItemList()) return;

        mDownloadFilesManager = new DownloadFilesManager(getApplicationContext(), mMediaItemList);
        mDownloadFilesManager.start();
    }

    private void showAlbumListDialog() {
        if (mAlbumList == null) {
            Log.d(TAG, "Album list is null. Try to get it again.");
            startGetSharedAlbumList();
            return;
        }
        hideProgress();
        ListDialogFragment fragment = ListDialogFragment.newInstance(DLG_ID_SELECT_ALBUM,
                R.string.select_album_dialog_title, mAlbumList.getTitleList(), TAG);
        fragment.setCancelable(false);
        fragment.show(getSupportFragmentManager(), ListDialogFragment.TAG);
    }

    private void setImageView(Bitmap bitmap) {
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageBitmap(bitmap);
    }

    private void showProgress(int textId) {
        findViewById(R.id.progress_layout).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.progress_textView)).setText(textId);
    }

    private void hideProgress() {
        findViewById(R.id.progress_layout).setVisibility(View.GONE);
    }

    private boolean isNullAccessToken() {
        if (mToken == null) {
            Log.d(TAG, "Access token is null. Try to get it again.");
            startGetAccessToken(null);
            return true;
        }
        return false;
    }

    private boolean isNullAlbumList() {
        if (mAlbumList == null) {
            Log.d(TAG, "Album list is null. Try to get it again.");
            startGetSharedAlbumList();
            return true;
        }
        return false;
    }

    private boolean isNullMediaItemList() {
        if (mMediaItemList == null) {
            Log.d(TAG, "MediaItem list is null. Try to get it again.");
            startGetMediaItemList();
            return true;
        }
        return false;
    }

    private boolean isNotSelectedAlbumIndex() {
        if (mSelectedAlbumIndex < 0) {
            Log.d(TAG, "Album is not selected. Show album list dialog.");
            showAlbumListDialog();
            return true;
        }
        return false;
    }

}
