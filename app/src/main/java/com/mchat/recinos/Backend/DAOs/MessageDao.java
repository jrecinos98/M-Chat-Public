package com.mchat.recinos.Backend.DAOs;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mchat.recinos.Backend.Entities.Messages.Message;

import java.util.List;

@Dao
public interface MessageDao {

    @Query("SELECT * FROM messages")
    LiveData<List<Message>> getAll();

    @Query("SELECT * FROM messages WHERE messages.owner_cid = :cid ORDER BY time_stamp ASC")
    LiveData<List<Message>> getTimeSortedChatMessages(int cid);

    @Query("SELECT * FROM messages WHERE id = :id")
    Message getMessageByID(long id);

    @Query("SELECT * FROM messages WHERE id IN (:messages)")
    LiveData<List<Message>> loadAllByIds(int[] messages);

    //If there is a conflict (same message being added again) then abort transaction
    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insertAll(Message... messages);

    @Insert
    long insert(Message message);

    @Delete
    void delete(Message user);

}
