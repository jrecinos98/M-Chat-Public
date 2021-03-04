package com.mchat.recinos.Backend.Entities.Messages;


import com.mchat.recinos.Util.Constants;

import java.io.Serializable;
import Protobuf.ProtoMessage;

public class TextMessage extends Message implements Serializable {

    public TextMessage(int id, int owner_cid, int type, long timeStamp, boolean seen, boolean belongsToUser, String data ){
        super(id, owner_cid,type, timeStamp, belongsToUser, seen, data);
    }
    public TextMessage(int cid, String text, long timeStamp, boolean belongsToUser){
        super(cid, text, text,timeStamp, Constants.MESSAGE_DATA_TYPES.TEXT, belongsToUser);
    }
    public TextMessage(Message message){
        super(message);
    }
    public TextMessage(String text, long timeStamp, boolean belongToUser){
        super(text, text, timeStamp, Constants.MESSAGE_DATA_TYPES.TEXT, belongToUser);
    }
    public ProtoMessage.Payload toProtoBufPayload(){
        ProtoMessage.Payload payload = ProtoMessage.Payload.newBuilder()
                .setOpCode(Constants.MESSAGE_DATA_TYPES.TEXT)
                .setText(getData())
                .build();
        return payload;
    }

}
