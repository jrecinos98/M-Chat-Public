package com.mchat.recinos.Backend.Client;

import android.util.Log;

import com.mchat.recinos.Util.IO;
import com.mchat.recinos.Util.Util;

import java.io.OutputStream;
import java.util.Calendar;

import Protobuf.ProtoMessage;


class WriteToServerRunnable implements  Runnable{
    private ProtoMessage.Message message;
    private OutputStream output;

    /**
     * Runnable used to send a message to the server
     * @param message The message to be sent
     * @param output The output stream to write to the server
     */
    WriteToServerRunnable(ProtoMessage.Message message, OutputStream output){
        this.message = message;
        this.output = output;
    }
    private byte[] generateMessageBytes(){
        //TODO Consider this
        //String packet = destID + message.toString();
        //packet.getBytes(StandardCharsets.UTF_8);
        return message.toByteArray();
    }

    public void run(){
        byte[] msg_bytes = generateMessageBytes();
        Log.d("MSG_SEND", "Size of message: "+ msg_bytes.length);
        long beforeTime = Calendar.getInstance().getTimeInMillis();
        IO.writeToStream(output, msg_bytes);
        long afterTime = Calendar.getInstance().getTimeInMillis();
        Log.d("MSG_SEND", "Before: " + Util.formatDate(beforeTime) + " After: " + Util.formatDate(afterTime));

    }
}