package com.example.photoslideshow.task;

import android.os.AsyncTask;
import android.util.Log;

import com.example.photoslideshow.list.MediaItemList;
import com.example.photoslideshow.serialize.AlbumData;
import com.example.photoslideshow.serialize.MediaItemData;
import com.example.photoslideshow.utils.GoogleApiUtils;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.internal.InternalPhotosLibraryClient;
import com.google.photos.library.v1.proto.MediaItem;
import com.google.photos.library.v1.proto.MediaMetadata;
import com.google.photos.library.v1.proto.SearchMediaItemsRequest;

import java.io.IOException;

public class GetMediaItemListAsyncTask extends AsyncTask<Void, Void, MediaItemList> {

    public interface ICallback {
        public void onMediaItemListResult(MediaItemList list);
    }

    private static final String TAG = GetMediaItemListAsyncTask.class.getSimpleName();

    private final String mToken;
    private final AlbumData mAlbum;
    private final ICallback mCallback;

    public static void start(String token, AlbumData album, ICallback callback) {
        GetMediaItemListAsyncTask task = new GetMediaItemListAsyncTask(token, album, callback);
        task.execute();
    }

    private GetMediaItemListAsyncTask(String token, AlbumData album, ICallback callback) {
        super();
        mToken = token;
        mAlbum = album;
        mCallback = callback;
    }

    @Override
    protected MediaItemList doInBackground(Void... voids) {
        Log.d(TAG, "Try to get MediaItem list.");
        PhotosLibraryClient client = null;
        try {
            client = GoogleApiUtils.initPhotosLibraryClient(mToken);
            SearchMediaItemsRequest request = SearchMediaItemsRequest.newBuilder()
                    .setAlbumId(mAlbum.getId())
                    .setPageSize(100)
                    .build();
            InternalPhotosLibraryClient.SearchMediaItemsPagedResponse response = client.searchMediaItems(request);

            int pCount=0, vCount=0, nCount=0;
            MediaItemList list = new MediaItemList();
            for (InternalPhotosLibraryClient.SearchMediaItemsPage page : response.iteratePages()) {
                Log.d(TAG, "page count:" + page.getPageElementCount());
                for (MediaItem item : page.iterateAll()) {
                    if (item.hasMediaMetadata()) {
                        MediaMetadata metadata = item.getMediaMetadata();
                        if (metadata.hasPhoto()) {
                            list.add(0, new MediaItemData(item.getId(), item.getFilename(), item.getProductUrl(), item.getBaseUrl(),
                                    metadata.getWidth(), metadata.getHeight(), metadata.getCreationTime(), MediaItemData.MediaType.PHOTO));
                            pCount++;
                        } else if (metadata.hasVideo()) {
                            list.add(0, new MediaItemData(item.getId(), item.getFilename(), item.getProductUrl(), item.getBaseUrl(),
                                    metadata.getWidth(), metadata.getHeight(), metadata.getCreationTime(), MediaItemData.MediaType.VIDEO));
                            vCount++;
                        } else {
                            nCount++;
                        }

                    }
                }
            }
            Log.d(TAG, String.format("pCount:%d, vCount:%d, nCount:%d", pCount, vCount, nCount));
            client.close();
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
}
