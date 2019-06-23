package com.example.photoslideshow.list;

import com.example.photoslideshow.serialize.MediaItemData;

import java.util.ArrayList;
import java.util.Collections;

public class MediaItemList extends ArrayList<MediaItemData> {

    public MediaItemData findFromId(String id) {
        for (MediaItemData data : this) {
            if (data.getId().equals(id)) return data;
        }
        return null;
    }

    public MediaItemList getSubListFromMediaType(MediaItemData.MediaType type) {
        MediaItemList subList = new MediaItemList();
        for (MediaItemData data : this) {
            if (data.getMediaType() == type) subList.add(data);
        }
        return subList;
    }

    public MediaItemList makeRandMediaItemList(MediaItemData.MediaType type, int size) {
        MediaItemList subList = getSubListFromMediaType(type);
        Collections.shuffle(subList);
        return (subList.size() > size ? (MediaItemList) subList.subList(0, size) : subList);
    }

}
