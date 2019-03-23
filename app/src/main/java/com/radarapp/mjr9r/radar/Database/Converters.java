package com.radarapp.mjr9r.radar.Database;

import android.arch.persistence.room.TypeConverter;
import android.net.Uri;

import com.radarapp.mjr9r.radar.model.Filter;

import java.util.Date;
import java.util.UUID;

public class Converters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static UUID fromString(String string) {
        return string.equals("") ? null : UUID.fromString(string);
    }

    @TypeConverter
    public static String fromUUID(UUID uuid) {
        return uuid == null ? null : uuid.toString();
    }

    @TypeConverter
    public static Filter fromName(String string) {
        return string.equals("") ? null : Filter.valueOf(string);
    }

    @TypeConverter
    public static String fromFilter(Filter filter) {
        return filter == null ? null : filter.getName();
    }

    @TypeConverter
    public static String fromUri(Uri uri) {
        return uri == null ? null : uri.toString();
    }

    @TypeConverter
    public static Uri UriFromString(String string) {
        return string.equals("") ? null : Uri.parse(string);
    }
}
