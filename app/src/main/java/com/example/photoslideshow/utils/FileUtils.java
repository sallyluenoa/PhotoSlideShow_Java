package com.example.photoslideshow.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.photoslideshow.task.DownloadFileAsyncTask;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public interface DownloadCallback {
        public void onSucceedDownload(File filePath);
        public void onFailedDownload();
    }

    private static final String TAG = FileUtils.class.getSimpleName();

    public static void downloadFileFromUrl(String inputUrl, String outputFilePath, final DownloadCallback callback) {
        File outputFile = new File(outputFilePath);
        if (outputFile.exists()) {
            Log.d(TAG, "Already existed file: " + outputFilePath);
            if (callback != null) {
                callback.onSucceedDownload(outputFile);
            }
            return;
        }
        try {
            DownloadFileAsyncTask task = new DownloadFileAsyncTask(inputUrl, outputFile, new DownloadFileAsyncTask.ICallback() {

                @Override
                public void succeedDownloadFile(File filePath) {
                    if (callback != null) {
                        callback.onSucceedDownload(filePath);
                    }
                }
                @Override
                public void failedDownloadFile() {
                    if (callback != null) {
                        callback.onFailedDownload();
                    }
                }
            });
            task.execute();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            if (callback != null) {
                callback.onFailedDownload();
            }
        }
    }

    public static List<String> getFilePathList(String dirPath) {
        File[] files = new File(dirPath).listFiles();
        List<String> list = new ArrayList<String>();
        for(File file : files){
            list.add(file.getPath());
        }
        return list;
    }

    public static boolean generateDirectory(String dirPath) {
        File dir = new File(dirPath);
        if (dir.exists()) {
            Log.d(TAG, "Already existed dir: " + dir.getPath());
            return true;
        } else {
            if (dir.mkdir()) {
                Log.d(TAG, "Succeeded to make dir: " + dir.getPath());
                return true;
            } else {
                Log.d(TAG, "Failed to make dir: " + dir.getPath());
                return false;
            }
        }
    }

    public static Bitmap getBitmap(String filePath) {
        return BitmapFactory.decodeFile(filePath);
    }

    public static void isBitmapAvailable(String fileName) {

    }

}
