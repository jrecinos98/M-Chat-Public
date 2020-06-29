package com.mchat.recinos.Backend.DAOs;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

import com.mchat.recinos.Backend.Entities.Latest;

@Dao
public interface LatestDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLatestMessage(Latest latest);
}
