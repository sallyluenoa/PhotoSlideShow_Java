package com.example.photoslideshow.list;

import com.example.photoslideshow.serialize.AlbumData;

import java.util.ArrayList;

public class AlbumList extends ArrayList<AlbumData> {

    public AlbumData findFromId(String id) {
        for (AlbumData data : this) {
            if (data.getId().equals(id)) return data;
        }
        return null;
    }

    public AlbumData findFromTitle(String title) {
        for (AlbumData data : this) {
            if (data.getTitle().equals(title)) return data;
        }
        return null;
    }

    public ArrayList<String> getTitleList() {
        ArrayList<String> titles = new ArrayList<>();
        for (AlbumData data : this) {
            titles.add(data.getTitle());
        }
        return titles;
    }
}
