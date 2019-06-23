package com.example.photoslideshow.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.photoslideshow.list.AlbumList;
import com.example.photoslideshow.list.MediaItemList;
import com.example.photoslideshow.serialize.AlbumData;
import com.example.photoslideshow.serialize.MediaItemData;
import com.example.photoslideshow.utils.GoogleApiUtils;
import com.example.photoslideshow.utils.PreferenceUtils;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.internal.InternalPhotosLibraryClient;
import com.google.photos.library.v1.proto.MediaItem;
import com.google.photos.library.v1.proto.SearchMediaItemsRequest;

import java.io.IOException;

public class GetMediaItemListAsyncTask extends AsyncTask<Void, Void, MediaItemList> {

    public interface ICallback {
        public void onMediaItemListResult(MediaItemList list);
    }

    private static final String TAG = GetMediaItemListAsyncTask.class.getSimpleName();

    private final Context mContext;
    private final String mToken;
    private final String mSelectedAlbumId;
    private final AlbumList mAlbumList;
    private final ICallback mCallback;

    public static void start(Context context, String token, String selectedAlbumId,
                             AlbumList albumList, ICallback callback) {
        GetMediaItemListAsyncTask task = new GetMediaItemListAsyncTask(context, token, selectedAlbumId, albumList, callback);
        task.execute();
    }

    private GetMediaItemListAsyncTask(Context context, String token, String selectedAlbumId,
                                      AlbumList albumList, ICallback callback) {
        super();
        mContext = context;
        mToken = token;
        mSelectedAlbumId = selectedAlbumId;
        mAlbumList = albumList;
        mCallback = callback;
    }

    @Override
    protected MediaItemList doInBackground(Void... voids) {
        final AlbumData album = mAlbumList.findFromId(mSelectedAlbumId);
        if (album.getMediaItemCount() <= getOldMediaItemCount(mContext, album) &&
            PreferenceUtils.getAllMediaItemList(mContext) != null) {
            Log.d(TAG, "No need to update MediaItem list.");
            return null;
        }

        Log.d(TAG, "Try to get MediaItem list.");
        PhotosLibraryClient client = null;
        try {
            client = GoogleApiUtils.initPhotosLibraryClient(mToken);
            SearchMediaItemsRequest request = SearchMediaItemsRequest.newBuilder()
                    .setAlbumId(album.getId())
                    .setPageSize(100)
                    .build();
            InternalPhotosLibraryClient.SearchMediaItemsPagedResponse response = client.searchMediaItems(request);

            long indexMediaItemCount = 0;
            long expectedListSize = 100;
            MediaItemList list = new MediaItemList();

            for (InternalPhotosLibraryClient.SearchMediaItemsPage page : response.iteratePages()) {
                Log.d(TAG, "page count:" + page.getPageElementCount());

                if (indexMediaItemCount + page.getPageElementCount() < album.getMediaItemCount() - expectedListSize) {
                    indexMediaItemCount += page.getPageElementCount();
                    Log.d(TAG, "Skipped. current index sum=" + indexMediaItemCount);
                    continue;
                }

                for (MediaItem item : page.iterateAll()) {
                    if (indexMediaItemCount >= album.getMediaItemCount() - expectedListSize) {
                        if (item.hasMediaMetadata()) {
                            list.add(0, new MediaItemData(item, item.getMediaMetadata()));
                        } else {
                            Log.d(TAG, "No MediaMetaData. index=" + indexMediaItemCount);
                        }
                    }
                    indexMediaItemCount++;
                }
            }

            Log.d(TAG, String.format("List size:%d", list.size()));
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
    protected void onPostExecute(MediaItemList list) {
        if (mCallback != null) {
            mCallback.onMediaItemListResult(list);
        }
    }

    private static long getOldMediaItemCount(Context context, AlbumData album) {
        AlbumList oldAlbumList = PreferenceUtils.getAlbumList(context);
        if (oldAlbumList == null) return 0;
        AlbumData oldAlbum = oldAlbumList.findFromId(album.getId());
        if (oldAlbum == null) return 0;
        return oldAlbum.getMediaItemCount();
    }
}
