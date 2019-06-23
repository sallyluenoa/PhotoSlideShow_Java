package com.example.photoslideshow.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

    public static final String getDateString(long time) {
        return DATE_FORMAT.format(new Date(time));
    }
}
