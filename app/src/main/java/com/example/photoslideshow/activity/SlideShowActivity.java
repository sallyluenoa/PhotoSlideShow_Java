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
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.photoslideshow.R;
import com.example.photoslideshow.fragment.AlertDialogFragment;
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
    implements AlertDialogFragment.OnClickListener, ListDialogFragment.OnClickListener,
        GetAccessTokenAsyncTask.ICallback, GetSharedAlbumListAsyncTask.ICallback, GetMediaItemListAsyncTask.ICallback {

    private static final String TAG = SlideShowActivity.class.getSimpleName();

    public static final String KEY_EMAIL = "email";

    private static final int DLG_ID_FAILED_SHOW_IMAGE = 0;
    private static final int DLG_ID_SELECT_ALBUM = 1;

    private static final int CHANGE_IMAGE_INTERVAL_SECS = 10;
    private static final int SHOW_IMAGE_FILES_MAX_COUNT = 30;
    private static final int SHOW_IMAGE_FILES_MIN_COUNT = 5;
    private static final int EXPIRED_TIME_HOURS = 24;

    private final Handler mHandler = new Handler();

    private String mToken = null;
    private String mSelectedAlbumId = null;

    private AlbumList mAlbumList = null;
    private MediaItemList mMediaItemList = null;

    private DownloadFilesManager mDownloadFilesManager = null;
    private Bitmap mBitmap = null;

    private boolean mIsGettingProcess = false;
    private boolean mIsActivityForeground = false;
    private int mShowIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_slide_show);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mIsActivityForeground = true;
        if (!mIsGettingProcess) {
            if (PreferenceUtils.isUpdateNeeded(getApplicationContext())) {
                startGetAccessToken();
            } else {
                startShowLocalMediaItemList(true);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        mIsActivityForeground = false;
        mHandler.removeCallbacksAndMessages(null);
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
    public void onClick(int id) {
        switch (id) {
            case DLG_ID_FAILED_SHOW_IMAGE:
                finish();
                break;
            default:
                break;
        }
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
            startShowLocalMediaItemList(false);
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
            startShowLocalMediaItemList(false);
        }
    }

    @Override
    public void onMediaItemListResult(MediaItemList list) {
        if (list != null) {
            Log.d(TAG, "Succeeded to update MediaItem list.");
            PreferenceUtils.putAllMediaItemList(getApplicationContext(), list);
            mMediaItemList = list.makeRandMediaItemList(MediaItemData.MediaType.PHOTO, SHOW_IMAGE_FILES_MAX_COUNT);
            PreferenceUtils.updateExpiredTime(getApplicationContext(), EXPIRED_TIME_HOURS);
            PreferenceUtils.putRandMediaItemList(getApplicationContext(), mMediaItemList);
            startDownloadFiles();
        } else {
            Log.d(TAG, "Failed to update MediaItem list.");
            startShowLocalMediaItemList(false);
        }
    }

    private void startGetAccessToken() {
        String email = null;
        Intent intent = getIntent();
        if (intent != null) {
            email = intent.getStringExtra(KEY_EMAIL);
        }
        if (email == null) {
            email = PreferenceUtils.getEmail(getApplicationContext());
        }
        mIsGettingProcess = true;
        showProgress(R.string.getting_access_token_from_google_api);
        GetAccessTokenAsyncTask.start(getApplicationContext(), email, GoogleApiUtils.SCOPE_PHOTO_READONLY, this);
    }

    private void startGetSharedAlbumList() {
        if (isNullAccessToken()) return;

        mIsGettingProcess = true;
        showProgress(R.string.getting_albums_from_google_photo);
        GetSharedAlbumListAsyncTask.start(mToken, this);
    }

    private void startGetMediaItemList() {
        if (isNullAccessToken()) return;
        if (isNullAlbumList()) return;
        if (isNotSelectedAlbumId()) return;

        mIsGettingProcess = true;
        showProgress(R.string.getting_media_items_from_google_photo);
        GetMediaItemListAsyncTask.start(getApplicationContext(), mToken, mSelectedAlbumId, mAlbumList, this);
    }

    private void startDownloadFiles() {
        if (isNullMediaItemList()) return;

        mToken = null;
        mIsGettingProcess = false;
        showProgress(R.string.downloading_files_from_google_photo);
        mDownloadFilesManager = new DownloadFilesManager(getApplicationContext(), mMediaItemList);
        if (mDownloadFilesManager.start()) {
            if (mIsActivityForeground) {
                // 1秒後に表示開始.
                Log.d(TAG, "Show downloaded image files 1 sec later.");
                mHandler.postDelayed(() -> checkImageAvailableFromDownloadManager(0), 1000);
            } else {
                Log.d(TAG, "Activity is background.");
            }
        } else {
            Log.d(TAG, "Failed to start download files manager.");
            startShowLocalMediaItemList(false);
        }
    }

    private void checkImageAvailableFromDownloadManager(int index) {
        mShowIndex = (index < mDownloadFilesManager.getFileCount() ? index : 0);
        Log.d(TAG, "Try to show index: " + mShowIndex);

        if (mShowIndex < mDownloadFilesManager.getDownloadedFileCount()) {
            // ダウンロード処理が完了している.
            if (mDownloadFilesManager.isDownloadedIndex(mShowIndex)) {
                // ダウンロード済、表示して次の表示は10秒後に設定.
                hideProgress();
                Log.d(TAG, "Show image.");
                showImage(mDownloadFilesManager.getFilePath(mShowIndex));
                mHandler.postDelayed(() -> checkImageAvailableFromDownloadManager(mShowIndex + 1),
                        1000 * CHANGE_IMAGE_INTERVAL_SECS);
            } else {
                // ダウンロードに失敗しているので次を確認する.
                Log.d(TAG, "Failed download. Go next image.");
                mHandler.post(() -> checkImageAvailableFromDownloadManager(mShowIndex + 1));
            }
        } else {
            // ダウンロード処理が未完了、1秒後に再確認.
            Log.d(TAG, "Not completed download yet. Wait a moment...");
            mHandler.postDelayed(() -> checkImageAvailableFromDownloadManager(mShowIndex), 1000);
        }
    }

    private void startShowLocalMediaItemList(boolean retry) {
        mToken = null;
        mIsGettingProcess = false;
        mMediaItemList = PreferenceUtils.getRandMediaItemList(getApplicationContext());
        if (mMediaItemList == null) {
            if (retry) {
                Log.d(TAG, "MediaItem list is null. Try to get files from Server.");
                startGetAccessToken();
            } else {
                Log.d(TAG, "MediaItem list is null. Show failed dialog.");
                showFailedDialog();
            }
            return;
        }

        int count = mMediaItemList.getDownloadedFilesCount(getApplicationContext());
        if (count >= SHOW_IMAGE_FILES_MIN_COUNT) {
            Log.d(TAG, "Show local image files. Downloaded files count: " + count);
            checkImageAvailableFromMediaItemList(mShowIndex);
        } else {
            if (retry) {
                Log.d(TAG, "There are a few image files. Try to get files from Server.");
                startGetAccessToken();
            } else {
                Log.d(TAG, "There are a few image files. Show failed dialog.");
                showFailedDialog();
            }
        }
    }

    private void checkImageAvailableFromMediaItemList(int index) {
        mShowIndex = (index < mMediaItemList.size() ? index : 0);
        Log.d(TAG, "Try to show index: " + mShowIndex);

        MediaItemData data = mMediaItemList.get(mShowIndex);
        if (data.isDownloadedFile(getApplicationContext())) {
            // ダウンロード済、表示して次の表示は10秒後に設定.
            Log.d(TAG, "Show image.");
            hideProgress();
            showImage(data.getFilePath(getApplicationContext()));
            mHandler.postDelayed(() -> checkImageAvailableFromMediaItemList(mShowIndex + 1),
                    1000 * CHANGE_IMAGE_INTERVAL_SECS);
        } else {
            // ダウンロードに失敗しているので次を確認する.
            Log.d(TAG, "Failed download. Go next image.");
            mHandler.post(() -> checkImageAvailableFromMediaItemList(mShowIndex + 1));
        }
    }

    private void showImage(String filePath) {
        Bitmap bitmap = BitmapUtils.getBitmap(filePath);
        ImageView imageView = findViewById(R.id.imageView);
        imageView.setImageBitmap(bitmap);

        if (mBitmap != null && !mBitmap.isRecycled()) mBitmap.recycle();
        mBitmap = bitmap;
    }

    private void showAlbumListDialog() {
        if (isNullAlbumList()) return;

        mIsGettingProcess = true;
        hideProgress();
        ListDialogFragment fragment = ListDialogFragment.newInstance(DLG_ID_SELECT_ALBUM,
                R.string.select_album_dialog_title, mAlbumList.getTitleList(), TAG);
        fragment.setCancelable(false);
        fragment.show(getSupportFragmentManager(), ListDialogFragment.TAG);
    }

    private void showFailedDialog() {
        mIsGettingProcess = true;
        hideProgress();
        AlertDialogFragment fragment = AlertDialogFragment.newInstance(DLG_ID_FAILED_SHOW_IMAGE,
                R.string.failed_show_images_dialog_title, R.string.failed_show_images_dialog_message,
                R.string.ok, TAG);
        fragment.setCancelable(false);
        fragment.show(getSupportFragmentManager(), AlertDialogFragment.TAG);
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
            startGetAccessToken();
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
        mSelectedAlbumId = PreferenceUtils.getSelectedAlbumId(getApplicationContext());
        if (mSelectedAlbumId == null || mAlbumList.findFromId(mSelectedAlbumId) == null) {
            Log.d(TAG, "Album is not selected. Show album list dialog.");
            showAlbumListDialog();
            return true;
        }
        return false;
    }

}
