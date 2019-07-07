package com.example.photoslideshow.serialize;

import android.content.Context;

import com.example.photoslideshow.utils.BitmapUtils;
import com.example.photoslideshow.utils.FileUtils;
import com.google.photos.library.v1.proto.MediaItem;
import com.google.photos.library.v1.proto.MediaMetadata;
import com.google.protobuf.Timestamp;

import java.io.File;
import java.io.Serializable;

import androidx.annotation.NonNull;

public class MediaItemData implements Serializable {

    public enum MediaType {
        PHOTO,
        VIDEO,
        OTHER,
        ;

        @Override
        public String toString() {
            return name().toLowerCase();
        }

        public static MediaType convertFromMetadataCase(MediaMetadata.MetadataCase metadataCase) {
            switch (metadataCase) {
                case PHOTO: return MediaType.PHOTO;
                case VIDEO: return MediaType.VIDEO;
                default: return MediaType.OTHER;
            }
        }
    }

    private final String id;
    private final String fileName;
    private final String productUrl;
    private final String baseUrl;
    private final long timeMillis;
    private final long width;
    private final long height;
    private final MediaType mediaType;

    public MediaItemData(@NonNull MediaItem item, @NonNull MediaMetadata metadata) {
        this(item.getId(), item.getFilename(), item.getProductUrl(), item.getBaseUrl(),
                metadata.getWidth(), metadata.getHeight(), metadata.getCreationTime(), metadata.getMetadataCase());
    }

    public MediaItemData(String id, String fileName, String productUrl, String baseUrl,
                         long width, long height, Timestamp timestamp, MediaMetadata.MetadataCase metadataCase) {
        this.id = id;
        this.fileName = fileName;
        this.productUrl = productUrl;
        this.baseUrl = baseUrl;
        this.width = width;
        this.height = height;
        this.timeMillis = timestamp.getSeconds() * 1000;
        this.mediaType = MediaType.convertFromMetadataCase(metadataCase);
    }

    public String getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getProductUrl() {
        return productUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public long getWidth() {
        return width;
    }

    public long getHeight() {
        return height;
    }

    public long getTimeMillis() {
        return timeMillis;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public String getDownloadUrl(long expWidth, long expHeight) {
        float scale = BitmapUtils.getEffectiveScale(width, height, expWidth, expHeight);
        return String.format("%s=w%d-h%d", baseUrl, (long)(width * scale), (long)(height * scale));
    }

    public String getFilePath(Context context) {
        return FileUtils.getFilePath(context, fileName, mediaType);
    }

    public boolean isDownloadedFile(Context context) {
        return new File(getFilePath(context)).exists();
    }
}
