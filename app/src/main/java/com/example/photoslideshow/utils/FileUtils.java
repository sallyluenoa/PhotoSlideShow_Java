package com.example.photoslideshow.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.example.photoslideshow.serialize.MediaItemData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    private static final String TAG = FileUtils.class.getSimpleName();

    private static final String FORMAT_FILE_PATH = "%s/%s";

    public static List<String> getFilePathList(String dirPath) {
        File[] files = new File(dirPath).listFiles();
        List<String> list = new ArrayList<String>();
        for(File file : files){
            list.add(file.getPath());
        }
        return list;
    }

    public static boolean mkdir(String dirPath) {
        File dir = new File(dirPath);
        if (dir.exists()) {
            Log.d(TAG, "Already existed dir: " + dir.getPath());
            return true;
        }
        if (dir.mkdir()) {
            Log.d(TAG, "Succeeded to make dir: " + dir.getPath());
            return true;
        } else {
            Log.d(TAG, "Failed to make dir: " + dir.getPath());
            return false;
        }
    }

    public static String getAppDir(Context context) {
        return String.format(FORMAT_FILE_PATH,
                Environment.getExternalStorageDirectory(),
                context.getResources().getString(com.example.photoslideshow.R.string.dir_name));
    }

    public static String getTypeDir(Context context, MediaItemData.MediaType type) {
        return String.format(FORMAT_FILE_PATH, getAppDir(context), type.toString());
    }

    public static String getFilePath(Context context, String fileName, MediaItemData.MediaType type) {
        return String.format(FORMAT_FILE_PATH, getTypeDir(context, type), fileName);
    }

}
