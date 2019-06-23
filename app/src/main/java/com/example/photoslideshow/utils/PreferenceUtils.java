package com.example.photoslideshow.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.photoslideshow.list.AlbumList;
import com.example.photoslideshow.list.MediaItemList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class PreferenceUtils {

    public static final String TAG = PreferenceUtils.class.getSimpleName();

    private static final String FILE_NAME = "preference";

    private static final String KEY_ACCOUNT_NAME = "account_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ALBUM_LIST = "album_list";
    private static final String KEY_ALL_MEDIA_ITEM_LIST = "all_media_item_list";
    private static final String KEY_RAND_MEDIA_ITEM_LIST = "rand_media_item_list";
    private static final String KEY_SELECTED_ALBUM_ID = "selected_album_id";
    private static final String KEY_EXPIRED_TIME = "expired_time";

    public static void deleteAll(Context context) {
        SharedPreferences.Editor editor = getPreferencesEditor(context);
        editor.remove(KEY_ACCOUNT_NAME);
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_ALBUM_LIST);
        editor.remove(KEY_ALL_MEDIA_ITEM_LIST);
        editor.remove(KEY_RAND_MEDIA_ITEM_LIST);
        editor.commit();
    }

    public static void putAccountInfo(Context context, String accountName, String email) {
        SharedPreferences.Editor editor = getPreferencesEditor(context);
        editor.putString(KEY_ACCOUNT_NAME, accountName);
        editor.putString(KEY_EMAIL, email);
        editor.commit();
    }

    public static boolean hasAccountInfo(Context context) {
        SharedPreferences pref = getPreferences(context);
        return (pref.contains(KEY_ACCOUNT_NAME) && pref.contains(KEY_EMAIL));
    }

    public static String getAccountName(Context context) {
        return getPreferences(context).getString(KEY_ACCOUNT_NAME, null);
    }

    public static String getEmail(Context context) {
        return getPreferences(context).getString(KEY_EMAIL, null);
    }

    public static void updateExpiredTime(Context context, long hours) {
        SharedPreferences.Editor editor = getPreferencesEditor(context);
        long expired = System.currentTimeMillis() + hours * 60 * 60 * 1000;
        editor.putLong(KEY_EXPIRED_TIME, expired);
        editor.commit();
    }

    public static boolean isUpdateNeeded(Context context) {
        long expired = getPreferences(context).getLong(KEY_EXPIRED_TIME, 0);
        return System.currentTimeMillis() > expired;
    }

    public static void putAlbumList(Context context, AlbumList list) {
        SharedPreferences.Editor editor = getPreferencesEditor(context);
        Gson gson = new Gson();
        editor.putString(KEY_ALBUM_LIST, gson.toJson(list));
        editor.commit();
    }

    public static AlbumList getAlbumList(Context context) {
        SharedPreferences pref = getPreferences(context);
        String json = pref.getString(KEY_ALBUM_LIST, null);
        if (json != null) {
            Gson gson = new Gson();
            return gson.fromJson(json, new TypeToken<AlbumList>(){}.getType());
        } else {
            return null;
        }
    }

    public static void putAllMediaItemList(Context context, MediaItemList list) {
        SharedPreferences.Editor editor = getPreferencesEditor(context);
        Gson gson = new Gson();
        editor.putString(KEY_ALL_MEDIA_ITEM_LIST, gson.toJson(list));
        editor.commit();
    }

    public static MediaItemList getAllMediaItemList(Context context) {
        SharedPreferences pref = getPreferences(context);
        String json = pref.getString(KEY_ALL_MEDIA_ITEM_LIST, null);
        if (json != null) {
            Gson gson = new Gson();
            return gson.fromJson(json, new TypeToken<MediaItemList>(){}.getType());
        } else {
            return null;
        }
    }

    public static void putRandMediaItemList(Context context, MediaItemList list) {
        SharedPreferences.Editor editor = getPreferencesEditor(context);
        Gson gson = new Gson();
        editor.putString(KEY_RAND_MEDIA_ITEM_LIST, gson.toJson(list));
        editor.commit();
    }

    public static MediaItemList getRandMediaItemList(Context context) {
        SharedPreferences pref = getPreferences(context);
        String json = pref.getString(KEY_RAND_MEDIA_ITEM_LIST, null);
        if (json != null) {
            Gson gson = new Gson();
            return gson.fromJson(json, new TypeToken<MediaItemList>(){}.getType());
        } else {
            return null;
        }
    }

    public static void putSelectedAlbumId(Context context, String selectedAlbumId) {
        SharedPreferences.Editor editor = getPreferencesEditor(context);
        editor.putString(KEY_SELECTED_ALBUM_ID, selectedAlbumId);
        editor.commit();
    }

    public static String getSelectedAlbumId(Context context) {
        return getPreferences(context).getString(KEY_SELECTED_ALBUM_ID, null);
    }

    public static boolean hasSelectedAlbumId(Context context) {
        return getPreferences(context).contains(KEY_SELECTED_ALBUM_ID);
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    private static SharedPreferences.Editor getPreferencesEditor(Context context) {
        return getPreferences(context).edit();
    }
}
