package com.example.photoslideshow.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.photoslideshow.serialize.AlbumData;
import com.example.photoslideshow.serialize.MediaItemData;
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
    private static final String KEY_MEDIA_ITEM_LIST = "media_item_list";

    public static void deleteAll(Context context) {
        SharedPreferences pref = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(KEY_ACCOUNT_NAME);
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_ALBUM_LIST);
        editor.remove(KEY_MEDIA_ITEM_LIST);
        editor.commit();
    }

    public static void putAccountInfo(Context context, String accountName, String email) {
        SharedPreferences pref = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(KEY_ACCOUNT_NAME, accountName);
        editor.putString(KEY_EMAIL, email);
        editor.commit();
    }

    public static boolean hasAccountInfo(Context context) {
        SharedPreferences pref = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        String name = pref.getString(KEY_ACCOUNT_NAME, null);
        String email = pref.getString(KEY_EMAIL, null);
        return (name != null && email != null);
    }

    public static String getAccountName(Context context) {
        SharedPreferences pref = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_ACCOUNT_NAME, null);
    }

    public static String getEmail(Context context) {
        SharedPreferences pref = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_EMAIL, null);
    }

    public static void putAlbumList(Context context, List<AlbumData> list) {
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
            return gson.fromJson(json, new TypeToken<List<AlbumData>>(){}.getType());
        } else {
            return null;
        }
    }

    public static void putMediaItemList(Context context, List<MediaItemData> list) {
        SharedPreferences pref = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        Gson gson = new Gson();
        editor.putString(KEY_MEDIA_ITEM_LIST, gson.toJson(list));
        editor.commit();
    }

    public static List<MediaItemData> getMediaItemList(Context context) {
        SharedPreferences pref = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        String json = pref.getString(KEY_MEDIA_ITEM_LIST, null);
        if (json != null) {
            Gson gson = new Gson();
            return gson.fromJson(json, new TypeToken<List<MediaItemData>>(){}.getType());
        } else {
            return null;
        }
    }

}
