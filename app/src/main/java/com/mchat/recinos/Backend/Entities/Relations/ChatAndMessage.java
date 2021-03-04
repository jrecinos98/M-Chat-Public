package com.mchat.recinos.Backend.Entities.Relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.mchat.recinos.Backend.Entities.Chat;
import com.mchat.recinos.Backend.Entities.Messages.Message;

public class ChatAndMessage {
    @Embedded
    public Chat chat;
    @Relation(
            parentColumn = "cid",
            entityColumn = "cid"
    )
    public Message message;
}
