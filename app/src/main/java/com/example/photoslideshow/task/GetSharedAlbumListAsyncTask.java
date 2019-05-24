package com.example.photoslideshow.task;

import android.os.AsyncTask;
import android.util.Log;

import com.example.photoslideshow.list.AlbumList;
import com.example.photoslideshow.serialize.AlbumData;
import com.example.photoslideshow.utils.GoogleApiUtils;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.internal.InternalPhotosLibraryClient;
import com.google.photos.library.v1.proto.Album;

import java.io.IOException;

public class GetSharedAlbumListAsyncTask extends AsyncTask<Void, Void, AlbumList> {

    public interface ICallback {
        public void onAlbumListResult(AlbumList list);
    }

    private static final String TAG = GetSharedAlbumListAsyncTask.class.getSimpleName();

    private final String mToken;
    private final ICallback mCallback;

    public static void start(String token, ICallback callback) {
        GetSharedAlbumListAsyncTask task = new GetSharedAlbumListAsyncTask(token, callback);
        task.execute();
    }

    private GetSharedAlbumListAsyncTask(String token, ICallback callback) {
        super();
        mToken = token;
        mCallback = callback;
    }

    @Override
    protected AlbumList doInBackground(Void... voids) {
        Log.d(TAG, "Try to get Album list.");
        PhotosLibraryClient client = null;
        try {
            client = GoogleApiUtils.initPhotosLibraryClient(mToken);
            InternalPhotosLibraryClient.ListSharedAlbumsPagedResponse response = client.listSharedAlbums();
            AlbumList list = new AlbumList();
            for (Album album : response.iterateAll()) {
                list.add(new AlbumData(album.getId(), album.getTitle(), album.getProductUrl(), album.getMediaItemsCount()));
            }
            Log.d(TAG, "AlbumData list size: " + list.size());
            return list;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (client != null) client.close();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }

    @Override
    protected void onPostExecute(AlbumList list) {
        if (mCallback != null) {
            mCallback.onAlbumListResult(list);
        }
    }
}
