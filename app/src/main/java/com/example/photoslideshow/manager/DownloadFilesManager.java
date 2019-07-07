package com.example.photoslideshow.manager;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.example.photoslideshow.list.MediaItemList;
import com.example.photoslideshow.serialize.MediaItemData;
import com.example.photoslideshow.task.DownloadFileAsyncTask;
import com.example.photoslideshow.utils.FileUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class DownloadFilesManager implements DownloadFileAsyncTask.ICallback {

    private static final String TAG = DownloadFilesManager.class.getSimpleName();

    private final Handler mHandler = new Handler();

    private final Context mContext;
    private final MediaItemList mMediaItemList;

    // 複数のスレッドから呼び出される可能性のある変数.
    // 値の変更時や public method からの値取得には排他処理 (synchronized) を適用すること.
    private int mIndex;
    private List<Integer> mIgnoredIndexes;
    private boolean mIsRunning;
    private boolean mIsCompleted;

    public DownloadFilesManager(Context context, @NonNull MediaItemList list) {
        mContext = context;
        mMediaItemList = list;

        mIndex = 0;
        mIgnoredIndexes = new ArrayList<>();
        mIsRunning = false;
        mIsCompleted = false;
    }

    @Override
    public void onDownloadFileResult(File file) {
        if (file != null) {
            Log.d(TAG, "Succeeded to download file: " + file.getPath());
        } else {
            Log.d(TAG, "Failed to download file. Ignored index: " + mIndex);
            addIgnoredIndexes();
        }
        incrementIndex();
        runDownloadFiles();
    }

    /**
     * 複数ファイルダウンロード処理を開始する.
     * ファイル保存先のディレクトリ生成に失敗した場合はfalseを返し何もしない.
     */
    public boolean start() {
        if (!initMkdirs()) {
            Log.w(TAG, "Failed to init mkdirs.");
            return false;
        }
        initValues();
        runDownloadFiles();
        return true;
    }

    /**
     * ダウンロードするファイル数を取得する.
     */
    public int getFileCount() {
        return mMediaItemList.size();
    }

    /**
     * ダウンロードチェック済のファイル数を取得する.
     */
    public int getDownloadedFileCount() {
        synchronized (this) {
            return mIsCompleted ? getFileCount() : mIndex;
        }
    }

    /**
     * 指定されたインデックスのダウンロードファイルパスを取得する.
     */
    public String getFilePath(int index) {
        return mMediaItemList.get(index).getFilePath(mContext);
    }
    
    /**
     * 指定されたインデックスのダウンロード処理が完了しているか確認する.
     * ダウンロード処理に成功している場合のみ true を返し、
     * 失敗している場合やまだ行われていない場合は false を返す.
     */
    public boolean isDownloadedIndex(int index) {
        synchronized (this) {
            return (index < mIndex && !mIgnoredIndexes.contains(index));
        }
    }

    /**
     * ダウンロード処理中か確認する.
     */
    public boolean isRunning() {
        synchronized (this) {
            return mIsRunning;
        }
    }

    /**
     * ダウンロード処理が完了したか確認する.
     */
    public boolean isCompleted() {
        synchronized (this) {
            return mIsCompleted;
        }
    }

    /**
     * ファイル保存先のディレクトリ生成.
     */
    private boolean initMkdirs() {
        if (!FileUtils.mkdir(FileUtils.getAppDir(mContext))) return false;
        for (MediaItemData.MediaType type : MediaItemData.MediaType.values()) {
            if (type == MediaItemData.MediaType.OTHER) continue;
            if (!FileUtils.mkdir(FileUtils.getTypeDir(mContext, type))) return false;
        }
        return true;
    }

    /**
     * 変数の初期化.
     */
    private void initValues() {
        synchronized (this) {
            mIndex = 0;
            mIgnoredIndexes.clear();
            mIsRunning = true;
            mIsCompleted = false;
        }
    }

    /**
     * ファイルダウンロードのメイン処理. 非同期処理で行う.
     */
    private void runDownloadFiles() {
        mHandler.post(() -> {
            if (!hasDownloadOccasion()) {
                Log.d(TAG, "Finished to download all files.");
                completedDownloadOccasion();
                return;
            }

            try {
                MediaItemData data = mMediaItemList.get(mIndex);
                DownloadFileAsyncTask.start(
                        data.getDownloadUrl(500, 1000),
                        FileUtils.getFilePath(mContext, data.getFileName(), data.getMediaType()),
                        DownloadFilesManager.this);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                addIgnoredIndexes();
                incrementIndex();
                runDownloadFiles();
            }
        });
    }

    /**
     * ダウンロード処理する必要のあるインデックスを検索する.
     * 処理の必要性がある場合に true をし、すべて完了した場合は false を返す.
     */
    private boolean hasDownloadOccasion() {
        while (mIndex < mMediaItemList.size()) {
            MediaItemData data = mMediaItemList.get(mIndex);
            File file = new File(FileUtils.getFilePath(mContext, data.getFileName(), data.getMediaType()));
            if (!file.exists()) return true;
            incrementIndex();
        }
        return false;
    }

    /**
     * すべてのダウンロード処理完了.
     */
    private void completedDownloadOccasion() {
        synchronized (this) {
            mIsRunning = false;
            mIsCompleted = true;
        }
    }

    /**
     * インデックスのインクリメント.
     */
    private void incrementIndex() {
        synchronized (this) {
            mIndex++;
        }
    }

    /**
     * ダウンロード失敗時に無視リストに追加.
     */
    private void addIgnoredIndexes() {
        synchronized (this) {
            mIgnoredIndexes.add(mIndex);
        }
    }

}
