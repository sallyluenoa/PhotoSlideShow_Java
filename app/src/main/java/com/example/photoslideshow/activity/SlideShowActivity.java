package com.example.photoslideshow.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
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
import com.example.photoslideshow.serialize.MediaItemData;
import com.example.photoslideshow.task.GetAccessTokenAsyncTask;
import com.example.photoslideshow.task.GetMediaItemListAsyncTask;
import com.example.photoslideshow.task.GetSharedAlbumListAsyncTask;
import com.example.photoslideshow.utils.BitmapUtils;
import com.example.photoslideshow.utils.GoogleApiUtils;
import com.example.photoslideshow.utils.PreferenceUtils;

public class SlideShowActivity extends AppCompatActivity
    implements  ListDialogFragment.OnClickListener,
        GetAccessTokenAsyncTask.ICallback, GetSharedAlbumListAsyncTask.ICallback, GetMediaItemListAsyncTask.ICallback {

    private static final String TAG = SlideShowActivity.class.getSimpleName();

    public static final String KEY_EMAIL = "email";

    private static final int DLG_ID_SELECT_ALBUM = 1;

    private final Handler mHandler = new Handler();

    private String mToken = null;
    private String mSelectedAlbumId = null;

    private AlbumList mAlbumList = null;
    private MediaItemList mMediaItemList = null;

    private DownloadFilesManager mDownloadFilesManager = null;
    private Bitmap mBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_show);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSelectedAlbumId = PreferenceUtils.getSelectedAlbumId(getApplicationContext());
        mAlbumList = PreferenceUtils.getAlbumList(getApplicationContext());
        mMediaItemList = PreferenceUtils.getRandMediaItemList(getApplicationContext());

        if (PreferenceUtils.isUpdateNeeded(getApplicationContext())) {
            String email = null;
            Intent intent = getIntent();
            if (intent != null) {
                email = intent.getStringExtra(KEY_EMAIL);
            }
            startGetAccessToken(email);
        } else {
            startDownloadFiles();
        }
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
                mSelectedAlbumId = mAlbumList.get(which).getId();
                PreferenceUtils.putSelectedAlbumId(getApplicationContext(), mSelectedAlbumId);
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
            PreferenceUtils.putAlbumList(getApplicationContext(), mAlbumList);
            startGetMediaItemList();
        } else {
            Log.d(TAG, "Failed to get Album list.");
        }
    }

    @Override
    public void onMediaItemListResult(MediaItemList list) {
        if (list != null) {
            Log.d(TAG, "Succeeded to update MediaItem list.");
            PreferenceUtils.putAllMediaItemList(getApplicationContext(), list);
        } else {
            Log.d(TAG, "No need to update MediaItem list.");
            list = PreferenceUtils.getAllMediaItemList(getApplicationContext());
        }
        mMediaItemList = list.makeRandMediaItemList(MediaItemData.MediaType.PHOTO, 10);
        PreferenceUtils.updateExpiredTime(getApplicationContext(), 1);
        PreferenceUtils.putRandMediaItemList(getApplicationContext(), mMediaItemList);
        startDownloadFiles();
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
        if (isNotSelectedAlbumId()) return;

        showProgress(R.string.getting_media_items_from_google_photo);
        GetMediaItemListAsyncTask.start(getApplicationContext(), mToken, mSelectedAlbumId, mAlbumList, this);
    }

    private void startDownloadFiles() {
        if (isNullMediaItemList()) return;

        showProgress(R.string.downloading_files_from_google_photo);
        mDownloadFilesManager = new DownloadFilesManager(getApplicationContext(), mMediaItemList);
        if (mDownloadFilesManager.start()) {
            // 3秒後に表示開始.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkImageAvailable(0);
                }
            }, 3000);
        } else {
            Log.d(TAG, "Failed to start download files manager.");
        }
    }

    private void checkImageAvailable(int index) {
        final int showIndex = (index < mDownloadFilesManager.getFileCount() ? index : 0);
        Log.d(TAG, "Try to show index: " + showIndex);

        if (showIndex < mDownloadFilesManager.getDownloadedFileCount()) {
            // ダウンロード処理が完了している.
            if (mDownloadFilesManager.isDownloadedIndex(showIndex)) {
                // ダウンロード済、表示して次の表示は10秒後に設定.
                hideProgress();
                Log.d(TAG, "Show image.");
                showImage(mDownloadFilesManager.getFilePath(showIndex));
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        checkImageAvailable(showIndex+1);
                    }
                }, 10000);
            } else {
                // ダウンロードに失敗しているので次を確認する.
                Log.d(TAG, "Failed download. Go next image.");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        checkImageAvailable(showIndex+1);
                    }
                });
            }
        } else {
            // ダウンロード処理が未完了、1秒後に再確認.
            Log.d(TAG, "Not completed download yet. Wait a moment...");
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkImageAvailable(showIndex);
                }
            }, 1000);
        }
    }

    private void showImage(String filePath) {
        Bitmap bitmap = BitmapUtils.getBitmap(filePath);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageBitmap(bitmap);

        if (mBitmap != null && !mBitmap.isRecycled()) mBitmap.recycle();
        mBitmap = bitmap;
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

    private boolean isNotSelectedAlbumId() {
        if (mSelectedAlbumId == null || mAlbumList.findFromId(mSelectedAlbumId) == null) {
            Log.d(TAG, "Album is not selected. Show album list dialog.");
            showAlbumListDialog();
            return true;
        }
        return false;
    }

}
