package com.example.photoslideshow.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.util.List;

public class PreferenceUtils {

    public static final String TAG = PreferenceUtils.class.getSimpleName();

    private static final String FILE_NAME = "preference";

    private static final String KEY_ACCOUNT_NAME = "account_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ALBUM_LIST = "album_list";
    private static final String KEY_PHOTO_LIST = "photo_list";
    private static final String KEY_VIDEO_LIST = "video_list";

    public static void putAccountInfo(Context context, String accountName, String email) {
        SharedPreferences pref = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(KEY_ACCOUNT_NAME, accountName);
        editor.putString(KEY_EMAIL, email);
        editor.commit();
    }

    public static String getAccountName(Context context) {
        SharedPreferences pref = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_ACCOUNT_NAME, null);
    }

    public static String getEmail(Context context) {
        SharedPreferences pref = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_EMAIL, null);
    }

    public static boolean hasAccountInfo(Context context) {
        SharedPreferences pref = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        String name = pref.getString(KEY_ACCOUNT_NAME, null);
        String email = pref.getString(KEY_EMAIL, null);
        return (name != null && email != null);
    }

    public static void putAlbumList(Context context, List<PhotosApiUtils.AlbumData> list) {
        SharedPreferences pref = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        Gson gson = new Gson();
        editor.putString(KEY_ALBUM_LIST, gson.toJson(list));
        editor.commit();
    }

    public static String getAlbumList(Context context) {
        SharedPreferences pref = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        String json = pref.getString(KEY_ALBUM_LIST, null);
        if (json != null) {
            Gson gson = new Gson();
            return gson.fromJson(json, new TypeToken<List<PhotosApiUtils.AlbumData>>(){}.getType());
        } else {
            return null;
        }
    }

    public static void putMediaItemLists(Context context, List<List<PhotosApiUtils.MediaItemData>> lists) {
        SharedPreferences pref = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        Gson gson = new Gson();
        editor.putString(KEY_PHOTO_LIST, gson.toJson(lists.get(0)));
        editor.putString(KEY_VIDEO_LIST, gson.toJson(lists.get(1)));
        editor.commit();
    }

    public static List<PhotosApiUtils.MediaItemData> getPhotoList(Context context) {
        SharedPreferences pref = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        String json = pref.getString(KEY_PHOTO_LIST, null);
        if (json != null) {
            Gson gson = new Gson();
            return gson.fromJson(json, new TypeToken<List<PhotosApiUtils.MediaItemData>>(){}.getType());
        } else {
            return null;
        }
    }

    public static List<PhotosApiUtils.MediaItemData> getVideoList(Context context) {
        SharedPreferences pref = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        String json = pref.getString(KEY_VIDEO_LIST, null);
        if (json != null) {
            Gson gson = new Gson();
            return gson.fromJson(json, new TypeToken<List<PhotosApiUtils.MediaItemData>>(){}.getType());
        } else {
            return null;
        }
    }
}
