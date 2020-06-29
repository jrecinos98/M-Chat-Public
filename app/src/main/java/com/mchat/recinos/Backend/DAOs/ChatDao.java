package com.mchat.recinos.Backend.DAOs;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mchat.recinos.Backend.Entities.Chat;

import java.util.List;

//TODO RawQueries annotation may be useful
@Dao
public interface ChatDao {
    /***
     * @return the live data object that will be monitored for changes.
     */
    @Query(" SELECT * from chat ORDER BY last_update DESC")
    LiveData<List<Chat>> getTimeOrderedChats();

    /**
     * @return List chats ordered by contact name.
     */
    @Query("SELECT * from chat ORDER BY contact_name")
    LiveData<List<Chat>> getNameOrderedChats();

    @Query("SELECT * FROM chat WHERE chat.uid = :uid ")
    Chat getChat(String uid);

    @Query("SELECT * FROM chat")
    List<Chat> getAll();

    @Query("SELECT * FROM chat WHERE cid IN (:cids)")
    List<Chat> getAllByIds(int[] cids);

    @Query("SELECT chat.cid FROM chat WHERE chat.uid = :uid")
    int getChatID(String uid);

    String query = "SELECT * FROM chat WHERE chat.uid = :uid";
    @Query (value = query)
    List<Chat> chatExists(String uid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Chat chat);

    //If there is a conflict (same chat being added again) then abort transaction
    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insertAll(Chat... chats);

    @Query("UPDATE chat SET preview = :preview, last_update = :time, unread_count = :unread WHERE uid = :uid")
    void updatePreview(String uid, String preview, long time, int unread);

    @Query("DELETE FROM chat WHERE cid = :cid")
    void delete(int cid);

    @Delete
    void delete(Chat chat);

    @Query("DELETE FROM chat")
    void deleteAll();

}
