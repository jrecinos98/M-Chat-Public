package com.mchat.recinos.Backend.DAOs;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.mchat.recinos.Backend.Entities.User;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM user")
    List<User> getAll();

    @Query("SELECT * FROM user WHERE uid IN (:uids)")
    List<User> loadAllByIds(int[] uids);

    @Query("SELECT * FROM user WHERE name LIKE :name ")
    User findByName(String name);

    @Insert
    void insertAll(User... users);

    @Delete
    void delete(User user);
}