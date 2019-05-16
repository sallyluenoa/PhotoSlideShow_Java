package com.example.photoslideshow.serialize;

import java.io.Serializable;

public class AlbumData implements Serializable {

    private final String id;
    private final String title;
    private final String productUrl;
    private final long mediaItemCount;

    public AlbumData(String id, String title, String productUrl, long mediaItemCount) {
        this.id = id;
        this.title = title;
        this.productUrl = productUrl;
        this.mediaItemCount = mediaItemCount;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getProductUrl() {
        return productUrl;
    }

    public long getMediaItemCount() {
        return mediaItemCount;
    }
}
