package com.example.photoslideshow.list;

import android.content.Context;

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

    public MediaItemList getSubListFromIndex(int fromIndex, int toIndex) {
        MediaItemList subList = new MediaItemList();
        for (int i=fromIndex; i<toIndex; i++) {
            subList.add(this.get(i));
        }
        return subList;
    }

    public MediaItemList makeRandMediaItemList(MediaItemData.MediaType type, int size) {
        MediaItemList subList = getSubListFromMediaType(type);
        Collections.shuffle(subList);
        return (subList.size() > size ? subList.getSubListFromIndex(0, size) : subList);
    }

    public int getDownloadedFilesCount(Context context) {
        int count = 0;
        for (MediaItemData data : this) {
            if (data.isDownloadedFile(context)) count++;
        }
        return count;
    }

}
