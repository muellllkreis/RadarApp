package com.radarapp.mjr9r.radar.Database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.radarapp.mjr9r.radar.model.DropMessage;

import java.util.List;

@Dao
public interface MessageDao {
    @Query("SELECT * FROM dropmessage")
    List<DropMessage> getAll();

    @Query("SELECT * FROM dropmessage WHERE dmId IN (:dmIds)")
    List<DropMessage> loadAllByIds(String[] dmIds);

//    @Query("SELECT * FROM dropmessage WHERE filter LIKE :filter)
//    User findByFilter(String filter, String last);

    @Insert
    void insertAll(DropMessage... messages);

    @Delete
    void delete(DropMessage dropMessage);
}
