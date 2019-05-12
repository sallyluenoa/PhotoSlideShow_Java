package com.example.photoslideshow.utils;

import android.util.Log;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.OAuth2Credentials;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.PhotosLibrarySettings;
import com.google.photos.library.v1.internal.InternalPhotosLibraryClient;
import com.google.photos.library.v1.proto.Album;
import com.google.photos.library.v1.proto.DateFilter;
import com.google.photos.library.v1.proto.DateRange;
import com.google.photos.library.v1.proto.Filters;
import com.google.photos.library.v1.proto.MediaItem;
import com.google.photos.library.v1.proto.MediaMetadata;
import com.google.photos.library.v1.proto.MediaTypeFilter;
import com.google.photos.library.v1.proto.SearchMediaItemsRequest;
import com.google.type.Date;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PhotosApiUtils {

    private static final String TAG = PhotosApiUtils.class.getSimpleName();

    public static List<AlbumData> getSharedAlbumList(String token) {
        try {
            PhotosLibraryClient client = init(token);
            InternalPhotosLibraryClient.ListSharedAlbumsPagedResponse response = client.listSharedAlbums();
            List<AlbumData> list = new ArrayList<>();
            for (Album album : response.iterateAll()) {
                list.add(new AlbumData(album.getId(), album.getTitle()));
            }
            client.close();
            Log.d(TAG, "AlbumData list size: " + list.size());
            return list;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<List<MediaItemData>> getMediaItemLists(String token, String albumId) {
        try {
            PhotosLibraryClient client = init(token);
            SearchMediaItemsRequest request = SearchMediaItemsRequest.newBuilder()
                    .setAlbumId(albumId)
//                    .setFilters(getFilters())
                    .setPageSize(100)
                    .build();
            InternalPhotosLibraryClient.SearchMediaItemsPagedResponse response = client.searchMediaItems(request);

//            List<MediaItemData> list = new ArrayList<>();
//            for (InternalPhotosLibraryClient.SearchMediaItemsPage page : response.iteratePages()) {
//                for (MediaItem item : page.iterateAll()) {
//                    list.add(0, new MediaItemData(item.getId(), item.getFilename(), item.getBaseUrl(), item.getProductUrl()));
//                }
//            }

            InternalPhotosLibraryClient.SearchMediaItemsPage prevPage = null;
            InternalPhotosLibraryClient.SearchMediaItemsPage currentPage = null;
            for (InternalPhotosLibraryClient.SearchMediaItemsPage page : response.iteratePages()) {
                Log.d(TAG, "page: " + page.toString());
                if (!page.hasNextPage()) {
                    currentPage = page;
                    break;
                }
                prevPage = page;
            }

            List<MediaItemData> photoList = new ArrayList<>();
            List<MediaItemData> videoList = new ArrayList<>();
            if (prevPage != null) {
                Log.d(TAG, "prevPage.size=" + prevPage.getPageElementCount());
                for (MediaItem item : prevPage.iterateAll()) {
                    if (item.hasMediaMetadata()) {
                        if (item.getMediaMetadata().hasPhoto()) {
                            photoList.add(0, new MediaItemData(item.getId(), item.getFilename(), item.getBaseUrl(), item.getProductUrl()));
                        } else if (item.getMediaMetadata().hasVideo()) {
                            videoList.add(0, new MediaItemData(item.getId(), item.getFilename(), item.getBaseUrl(), item.getProductUrl()));
                        }
                    }
                }
            }
            if (currentPage != null) {
                Log.d(TAG, "currentPage.size=" + currentPage.getPageElementCount());
                for (MediaItem item : currentPage.iterateAll()) {
                    if (item.hasMediaMetadata()) {
                        if (item.getMediaMetadata().hasPhoto()) {
                            photoList.add(0, new MediaItemData(item.getId(), item.getFilename(), item.getBaseUrl(), item.getProductUrl()));
                        } else if (item.getMediaMetadata().hasVideo()) {
                            videoList.add(0, new MediaItemData(item.getId(), item.getFilename(), item.getBaseUrl(), item.getProductUrl()));
                        }
                    }
                }
            }
            client.close();
            Log.d(TAG, String.format("PhotoList size:%d, VideoList size:%d", photoList.size(), videoList.size()));
            List<List<MediaItemData>> lists = new ArrayList<List<MediaItemData>>(){};
            lists.add(photoList);
            lists.add(videoList);
            return lists;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Filters getFilters() {
        return Filters.newBuilder()
                .setDateFilter(getDateFiler(Calendar.MONTH, 3))
                .setMediaTypeFilter(getMediaTypeFilter(MediaTypeFilter.MediaType.PHOTO))
                .build();
    }

    private static DateFilter getDateFiler(int calenderField, int amount) {
        Calendar calendar = Calendar.getInstance();
        Log.d(TAG, String.format("%4d/%2d/%2d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE)));
        Date endDate = Date.newBuilder()
                .setDay(calendar.get(Calendar.DATE))
                .setMonth(calendar.get(Calendar.MONTH))
                .setYear(calendar.get(Calendar.YEAR))
                .build();

        calendar.add(calenderField, -amount);
        Log.d(TAG, String.format("%4d/%2d/%2d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE)));
        Date startDate = Date.newBuilder()
                .setDay(calendar.get(Calendar.DATE))
                .setMonth(calendar.get(Calendar.MONTH))
                .setYear(calendar.get(Calendar.YEAR))
                .build();
        DateRange dateRange = DateRange.newBuilder()
                .setStartDate(startDate)
                .setEndDate(endDate)
                .build();

        return DateFilter.newBuilder()
                .addRanges(dateRange)
                .build();
    }

    private static MediaTypeFilter getMediaTypeFilter(MediaTypeFilter.MediaType type) {
        return MediaTypeFilter.newBuilder()
                .addMediaTypes(type)
                .build();
    }

    private static PhotosLibraryClient init(String token) throws IOException {
        OAuth2Credentials credentials = OAuth2Credentials.create(new AccessToken(token, null));

        PhotosLibrarySettings photosLibrarySettings =
                PhotosLibrarySettings.newBuilder()
                        .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                        .build();

        return PhotosLibraryClient.initialize(photosLibrarySettings);
    }

    public static class AlbumData {
        public final String id;
        public final String title;

        public AlbumData(String id, String title) {
            this.id = id;
            this.title = title;
        }
    }

    public static class MediaItemData {
        public final String id;
        public final String fileName;
        public final String baseUrl;
        public final String productUrl;

        public MediaItemData(String id, String fileName, String baseUrl, String productUrl) {
            this.id = id;
            this.fileName = fileName;
            this.baseUrl = baseUrl;
            this.productUrl = productUrl;
        }
    }
}
