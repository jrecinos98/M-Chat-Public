package com.mchat.recinos.Backend;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.mchat.recinos.Backend.DAOs.ChatDao;
import com.mchat.recinos.Backend.DAOs.MessageDao;
import com.mchat.recinos.Backend.Entities.Chat;
import com.mchat.recinos.Backend.Entities.Messages.Message;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Chat.class, Message.class}, version = 1, exportSchema = false)
public abstract class LocalDatabase extends RoomDatabase {
    //One DAO per table in database
    public abstract ChatDao chatDao();
    public abstract MessageDao messageDao();

    private static volatile LocalDatabase INSTANCE;

    private static final int NUMBER_OF_DB_THREADS = 4;
    //Used whenever a write request is to be made to the database. Need to call execute on it and pass a lambda func (runnable)
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_DB_THREADS);

    private static final int NUMBER_OF_IO_THREADS = 4;
    //Used whenever a write request is to be made to the database. Need to call execute on it and pass a lambda func (runnable)
    public static final ExecutorService IO = Executors.newFixedThreadPool(NUMBER_OF_IO_THREADS);


    //TODO create different databases for different users. Change the name to have different databases.
    //TODO add support library to encrypt the database. An option compatible with room is SQLCipher
    public static LocalDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (LocalDatabase.class) {
                //Check again if Instance is null. The synchronized does not prevent other thread from queueing up to init database.
                if (INSTANCE == null) {
                    //Log.d("LOCAL_DATABASE", "Initializing database: "+ CloudDatabase.getInstance().getUID());
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            //For every user there will be a distinct database name
                            LocalDatabase.class, CloudDatabase.getInstance().getUID())
                            .addCallback(sRoomDatabaseCallback)
                            //TODO eventually disable main thread queries as all db operations should be async.
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
    public static void reset(){
        INSTANCE = null;
    }
    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
        /*
            // If you want to keep data through app restarts,
            // comment out the following block
            databaseWriteExecutor.execute(() -> {
                // Populate the database in the background.
                // If you want to start with more words, just add them.
                WordDao dao = INSTANCE.wordDao();
                dao.deleteAll();

                Word word = new Word("Hello");
                dao.insert(word);
                word = new Word("World");
                dao.insert(word);
            });
        */
        }
        //Look online to see what other methods can be overwritten

    };
}
