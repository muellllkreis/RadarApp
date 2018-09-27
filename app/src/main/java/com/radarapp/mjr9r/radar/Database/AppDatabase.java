package com.radarapp.mjr9r.radar.Database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.radarapp.mjr9r.radar.model.DropMessage;

@Database(entities = {DropMessage.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract MessageDao messageDao();
}
