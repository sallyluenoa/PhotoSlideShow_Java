package com.example.photoslideshow.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
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
import com.example.photoslideshow.serialize.MediaItemData;
import com.example.photoslideshow.task.GetAccessTokenAsyncTask;
import com.example.photoslideshow.task.GetMediaItemListAsyncTask;
import com.example.photoslideshow.task.GetSharedAlbumListAsyncTask;
import com.example.photoslideshow.utils.FileUtils;
import com.example.photoslideshow.utils.GoogleApiUtils;
import com.example.photoslideshow.utils.PreferenceUtils;

import java.io.File;

public class SlideShowActivity extends AppCompatActivity
    implements  ListDialogFragment.OnClickListener,
        GetAccessTokenAsyncTask.ICallback, GetSharedAlbumListAsyncTask.ICallback, GetMediaItemListAsyncTask.ICallback,
        FileUtils.DownloadCallback {

    private static final String TAG = SlideShowActivity.class.getSimpleName();

    public static final String KEY_EMAIL = "email";

    private static final int DLG_ID_SELECT_ALBUM = 1;

    private String mToken = null;
    private AlbumList mAlbumList = null;
    private MediaItemList mMediaItemList = null;

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
                String title = mAlbumList.getTitleList().get(which);
                startGetMediaItemList(title);
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
        } else {
            Log.d(TAG, "Failed to get MediaItem list.");
        }
    }

    @Override
    public void onSucceedDownload(File filePath) {
        Log.d(TAG, "onSucceedDownload");
        Bitmap bitmap = FileUtils.getBitmap(filePath.getPath());
        setImageView(bitmap);
    }

    @Override
    public void onFailedDownload() {
        Log.d(TAG, "onFailedDownload");
    }

    private void startGetAccessToken(String email) {
        if (email == null) {
            email = PreferenceUtils.getEmail(getApplicationContext());
        }
        showProgress(R.string.getting_access_token_from_google_api);
        GetAccessTokenAsyncTask.start(getApplicationContext(), email, GoogleApiUtils.SCOPE_PHOTO_READONLY, this);
    }

    private void startGetSharedAlbumList() {
        if (mToken == null) {
            Log.d(TAG, "Access token is null. Try to get it again.");
            startGetAccessToken(null);
            return;
        }
        showProgress(R.string.getting_albums_from_google_photo);
        GetSharedAlbumListAsyncTask.start(mToken, this);
    }

    private void startGetMediaItemList(String title) {
        if (mToken == null) {
            Log.d(TAG, "Access token is null. Try to get it again.");
            startGetAccessToken(null);
            return;
        }
        if (mAlbumList == null) {
            Log.d(TAG, "Album list is null. Try to get it again.");
            startGetSharedAlbumList();
            return;
        }
        showProgress(R.string.getting_media_items_from_google_photo);
        GetMediaItemListAsyncTask.start(mToken, mAlbumList.findFromTitle(title), this);
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

    private void downloadFileFromUrl(int index) {
        if (mMediaItemList == null) {
            Log.w(TAG, "MediaItem list must not be null.");
            return;
        }
        MediaItemData item = mMediaItemList.get(index);
        String dirPath = Environment.getExternalStorageDirectory() + "/" + getString(R.string.dir_name);
        if (FileUtils.generateDirectory(dirPath)) {
            String filePath = dirPath + "/" + item.getFileName();
            FileUtils.downloadFileFromUrl(item.getBaseUrl(), filePath, this);
        } else {
            Log.w(TAG, "Failed.");
        }
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

}
