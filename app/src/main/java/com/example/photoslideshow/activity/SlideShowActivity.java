package com.example.photoslideshow.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.photoslideshow.R;
import com.example.photoslideshow.fragment.AlertDialogFragment;
import com.example.photoslideshow.fragment.ConfirmDialogFragment;
import com.example.photoslideshow.fragment.ListDialogFragment;
import com.example.photoslideshow.fragment.MenuPreferenceFragment;
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
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SlideShowActivity extends AppCompatActivity
    implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        AlertDialogFragment.OnClickListener, ConfirmDialogFragment.OnClickListener, ListDialogFragment.OnClickListener,
        GetAccessTokenAsyncTask.ICallback, GetSharedAlbumListAsyncTask.ICallback, GetMediaItemListAsyncTask.ICallback {

    private static final String TAG = SlideShowActivity.class.getSimpleName();

    private static final int DLG_ID_FAILED_SHOW_IMAGE = 0;
    private static final int DLG_ID_SELECT_ALBUM = 1;
    private static final int DLG_ID_CONFIRM_SIGN_OUT = 2;

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

    private GoogleApiClient mGoogleApiClient = null;
    private boolean mIsRequestedSignOut = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_slide_show);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onResume() {
        super.onResume();

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
    protected void onStart() {
        super.onStart();

        if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
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
            case R.id.action_menu:
                Log.d(TAG, "Action Menu!");
                startActivity(new Intent(this, MenuActivity.class));
                return true;
            case R.id.action_license:
                Log.d(TAG, "Action License!");
                startActivity(new Intent(this, OssLicensesMenuActivity.class)
                        .putExtra("title", getString(R.string.license_list)));
                return true;
            case R.id.action_sign_out:
                Log.d(TAG, "Action Sign Out!");
                showConfirmSignOutDialog();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(@Nullable Bundle connectionHint) {
        Log.d(TAG, "onConnected");

        if (mIsRequestedSignOut) signOut();
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
    public void onPositiveClick(int id) {
        switch (id) {
            case DLG_ID_CONFIRM_SIGN_OUT:
                signOut();
                break;
            default:
                break;
        }
    }

    @Override
    public void onNegativeClick(int id) {
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
            mMediaItemList = list.makeRandMediaItemList(MediaItemData.MediaType.PHOTO,
                    MenuPreferenceFragment.getMaxCountOfShowingImages(getApplicationContext()));
            PreferenceUtils.updateExpiredTime(getApplicationContext(), EXPIRED_TIME_HOURS);
            PreferenceUtils.putRandMediaItemList(getApplicationContext(), mMediaItemList);
            startDownloadFiles();
        } else {
            Log.d(TAG, "Failed to update MediaItem list.");
            startShowLocalMediaItemList(false);
        }
    }

    private void signOut() {
        if (!isConnectedGoogleApiClient()) {
            mIsRequestedSignOut = true;
            return;
        }

        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(status1 -> {
            Log.i(TAG, String.format("Signed out. Code: %d, Message: %s",
                    status1.getStatusCode(), status1.getStatusMessage()));

            Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(status2 -> {
                Log.i(TAG, String.format("Revoked access. Code: %d, Message: %s",
                        status2.getStatusCode(), status2.getStatusMessage()));

                PreferenceUtils.clear(getApplicationContext());
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            });
        });
    }


    private void startGetAccessToken() {
        mIsGettingProcess = true;
        showProgress(R.string.getting_access_token_from_google_api);
        String email = GoogleApiUtils.getCurrentSignInAccount(getApplicationContext()).getEmail();
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
                mHandler.postDelayed(() ->
                        checkImageAvailableFromDownloadManager(0, MenuPreferenceFragment.getTimeIntervalSec(getApplicationContext())), 1000);
            } else {
                Log.d(TAG, "Activity is background.");
            }
        } else {
            Log.d(TAG, "Failed to start download files manager.");
            startShowLocalMediaItemList(false);
        }
    }

    private void checkImageAvailableFromDownloadManager(int index, final int timeIntervalSec) {
        mShowIndex = (index < mDownloadFilesManager.getFileCount() ? index : 0);
        Log.d(TAG, "Try to show index: " + mShowIndex);

        if (mShowIndex < mDownloadFilesManager.getDownloadedFileCount()) {
            // ダウンロード処理が完了している.
            if (mDownloadFilesManager.isDownloadedIndex(mShowIndex)) {
                // ダウンロード済. 指定された時間後まで画像を表示する.
                Log.d(TAG, String.format("Show image. Next image is shown %d secs later.", timeIntervalSec));
                hideProgress();
                showImage(mDownloadFilesManager.getFilePath(mShowIndex));
                mHandler.postDelayed(() -> checkImageAvailableFromDownloadManager(mShowIndex + 1, timeIntervalSec),
                        1000 * timeIntervalSec);
            } else {
                // ダウンロードに失敗しているので次を確認する.
                Log.d(TAG, "Failed download. Go next image.");
                mHandler.post(() -> checkImageAvailableFromDownloadManager(mShowIndex + 1, timeIntervalSec));
            }
        } else {
            // ダウンロード処理が未完了、1秒後に再確認.
            Log.d(TAG, "Not completed download yet. Wait a moment...");
            mHandler.postDelayed(() -> checkImageAvailableFromDownloadManager(mShowIndex, timeIntervalSec), 1000);
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
        if (count >= 2) {
            Log.d(TAG, "Show local image files. Downloaded files count: " + count);
            checkImageAvailableFromMediaItemList(mShowIndex, MenuPreferenceFragment.getTimeIntervalSec(getApplicationContext()));
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

    private void checkImageAvailableFromMediaItemList(int index, final int timeIntervalSec) {
        mShowIndex = (index < mMediaItemList.size() ? index : 0);
        Log.d(TAG, "Try to show index: " + mShowIndex);

        MediaItemData data = mMediaItemList.get(mShowIndex);
        if (data.isDownloadedFile(getApplicationContext())) {
            // ダウンロード済. 指定された時間後まで画像を表示する.
            Log.d(TAG, String.format("Show image. Next image is shown %d secs later.", timeIntervalSec));
            hideProgress();
            showImage(data.getFilePath(getApplicationContext()));
            mHandler.postDelayed(() -> checkImageAvailableFromMediaItemList(mShowIndex + 1, timeIntervalSec),
                    1000 * timeIntervalSec);
        } else {
            // ダウンロードに失敗しているので次を確認する.
            Log.d(TAG, "Failed download. Go next image.");
            mHandler.post(() -> checkImageAvailableFromMediaItemList(mShowIndex + 1, timeIntervalSec));
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

    private void showConfirmSignOutDialog() {
        ConfirmDialogFragment fragment = ConfirmDialogFragment.newInstance(DLG_ID_CONFIRM_SIGN_OUT,
                R.string.confirm_sign_out_dialog_title, R.string.confirm_sign_out_dialog_message,
                R.string.yes, R.string.no, TAG);
        fragment.setCancelable(false);
        fragment.show(getSupportFragmentManager(), ConfirmDialogFragment.TAG);
    }

    private void showProgress(int textId) {
        findViewById(R.id.progress_layout).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.progress_textView)).setText(textId);
    }

    private void hideProgress() {
        findViewById(R.id.progress_layout).setVisibility(View.GONE);
    }

    private boolean isConnectedGoogleApiClient() {
        if (mGoogleApiClient == null) {
            String[] scopes = { GoogleApiUtils.SCOPE_PHOTO_READONLY };
            mGoogleApiClient = GoogleApiUtils.initSignInApiClient(this, this, this, scopes);
        }
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
            return false;
        }
        return true;
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
