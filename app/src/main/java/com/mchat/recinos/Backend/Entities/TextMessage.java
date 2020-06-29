package com.mchat.recinos.Backend.Entities;


import androidx.room.Entity;
import androidx.room.Ignore;

import com.mchat.recinos.Util.CONSTANTS;

import java.io.Serializable;
import Protobuf.ProtoMessage;

public class TextMessage extends Message implements Serializable {

    public TextMessage(int id, int owner_cid, int type, long timeStamp, boolean seen, boolean belongsToUser, String data ){
        super(id, owner_cid,type, timeStamp, belongsToUser, seen, data);
    }
    public TextMessage(int cid, String text, long timeStamp, boolean belongsToUser){
        super(cid, text, text,timeStamp, CONSTANTS.MESSAGE_DATA_TYPES.TEXT, belongsToUser);
    }
    public TextMessage(Message message){
        super(message);
    }
    public TextMessage(String text, long timeStamp, boolean belongToUser){
        super(text, text, timeStamp, CONSTANTS.MESSAGE_DATA_TYPES.TEXT, belongToUser);
    }
    public ProtoMessage.Payload toProtoBufPayload(){
        ProtoMessage.Payload payload = ProtoMessage.Payload.newBuilder()
                .setOpCode(CONSTANTS.MESSAGE_DATA_TYPES.TEXT)
                .setText(getData())
                .build();
        return payload;
    }

}
