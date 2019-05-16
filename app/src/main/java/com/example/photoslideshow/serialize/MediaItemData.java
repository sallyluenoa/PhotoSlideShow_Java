package com.example.photoslideshow.serialize;

import com.google.protobuf.Timestamp;

import java.io.Serializable;

public class MediaItemData implements Serializable {

    public enum MediaType {
        PHOTO,
        VIDEO,
    }

    private final String id;
    private final String fileName;
    private final String productUrl;
    private final String baseUrl;
    private final long timeMillis;
    private final long width;
    private final long height;
    private final MediaType mediaType;

    public MediaItemData(String id, String fileName, String productUrl, String baseUrl,
                         long width, long height, Timestamp timestamp, MediaType mediaType) {
        this.id = id;
        this.fileName = fileName;
        this.productUrl = productUrl;
        this.baseUrl = baseUrl;
        this.width = width;
        this.height = height;
        this.timeMillis = timestamp.getSeconds() * 1000;
        this.mediaType = mediaType;
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
}
