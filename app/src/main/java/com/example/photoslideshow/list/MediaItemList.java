package com.example.photoslideshow.list;

import com.example.photoslideshow.serialize.MediaItemData;

import java.util.ArrayList;

public class MediaItemList extends ArrayList<MediaItemData> {

    public MediaItemData findFromId(String id) {
        for (MediaItemData data : this) {
            if (data.getId().equals(id)) return data;
        }
        return null;
    }

}
