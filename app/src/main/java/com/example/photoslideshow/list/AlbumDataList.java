package com.example.photoslideshow.list;

import com.example.photoslideshow.serialize.AlbumData;

import java.util.ArrayList;

public class AlbumDataList extends ArrayList<AlbumData> {

    public AlbumData findFromId(String id) {
        for (AlbumData data : this) {
            if (data.getId().equals(id)) return data;
        }
        return null;
    }

}
