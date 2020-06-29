package com.mchat.recinos.Backend.Client;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import com.mchat.recinos.Backend.CloudDatabase;
import com.mchat.recinos.Backend.Entities.ImageMessage;
import com.mchat.recinos.Backend.Entities.Message;
import com.mchat.recinos.Backend.Entities.User;
import com.mchat.recinos.Util.CONSTANTS;
import com.mchat.recinos.Util.IO;
import com.mchat.recinos.Util.Util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import Protobuf.ProtoMessage;

//TODO on server send ACK back for every message received. On client wait for the ack (for some time).
// The write thread will send messages and add them to nonAckedMessage list. The listen thread will
// receive messages and check the type. If it is ACK type then the id field is checked and the message is removed from the list.
public class Client {
    public final static int CONNECTION_TIME_OUT = 5000; //ms or 5 seconds
    public static int CURRENT_CHAT;

    private Connection connection;
    private Socket connectionSocket;
    private Context context;
    private User primaryUser;
    private final Map<Integer, ProtoMessage.Message> unAckedMessages;

    private Thread initConnectionThread;
    private Thread readThread;
    private ServerListenRunnable readRunnable;
    private Thread writeThread;

    /**
     * Constructor for client class
     * @param application Reference to the application context
     * @param user The logged in user
     */
    public Client(Application application, User user){
        connection = new Connection();
        primaryUser = user;
        context = application;
         /* This Map will have each call synchronized (atomic). Combinations of multiple calls however is NOT atomic.
            Needs higher level synchronization such as synchronize blocks.  */
        unAckedMessages = Collections.synchronizedMap(new HashMap<>());
    }
    /***
     * @return user ID
     */
    public String getUserID(){
        return primaryUser.getUserID();
    }
    /***
     * @return user object
     */
    public User getPrimaryUser() {return primaryUser;}

    /**
     * @return True if connected. False if connection not established or is in progress
     */
    public boolean isConnected(){
        if(connectionSocket == null || connectionInProgress()){
            //TODO check that it is not in the process of connecting
            return false;
        }
        return connectionSocket.isConnected();
    }

    /**
     * @return True if the connection has been initiated but has not completed.
     */
    public boolean connectionInProgress(){
        return connection.status.equals(Connection.STATUS.IN_PROGRESS);
    }
    /***
     * Client reference passed so the IP and PORT can be provided
     */
    public void initClient(){
        CloudDatabase.getInstance().setUpServerConnection(this);
    }

    /***
     * Called by the CloudDatabase once the IP and PORT of the server have been fetched
     * @param ip Server's IP
     * @param port Server's PORT
     */
    public void connectToServer(String ip, int port){
        if(connection.status == Connection.STATUS.OFF || connection.status == Connection.STATUS.IN_PROGRESS){
            connection.setInfo(ip, port);
            connection.status = Connection.STATUS.ON;
        }
        initConnectionThread= new Thread(new InitConnectionThread());
        initConnectionThread.start();
    }

    /**
     * @param destID The UID of the destination
     * @param msg Message object containing all the relevant information
     */
    public void sendMessage(String destID, Message msg){
        //Log.d("CLIENT", "MSG: " + msg.getData());
        ProtoMessage.Message newMessage = Util.makeProtoMessage(destID, msg);

        //Add message to the list of unacked messages
        unAckedMessages.put(msg.getId(), newMessage);
        writeThread = new Thread(new WriteToServerRunnable(newMessage, connection.output));
        writeThread.start();
    }

    /**
     * Disconnects the client from the server and ends all running threads
     */
    public void disconnect(){
        try {
            connectionSocket.close();
            if(readRunnable != null) {
                //End the reading thread. To avoid having memory leaks.
                readRunnable.interrupt();
            }
        }catch(IOException e){
            Log.d("CLIENT", e.toString());
        }
    }

    /**
     * Runnable that establishes connection with the server
     */
    class InitConnectionThread implements Runnable{
        public void run() {
            try {
                connectionSocket = new Socket();
                connectionSocket.connect(new InetSocketAddress(connection.IP, connection.PORT), CONNECTION_TIME_OUT);
                connection.output = connectionSocket.getOutputStream();
                connection.input = connectionSocket.getInputStream();
                authenticate(0);
            }
            //IOException covers timeout exception
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        /**
         * Sends current users userID to server after connection is established
         */
        private void authenticate(int attempts){
            ProtoMessage.Authentication authMessage = ProtoMessage.Authentication.newBuilder()
                    .setType(CONSTANTS.PROTO_MESSAGE_DATA_TYPES.AUTH)
                    .setSenderUid(CloudDatabase.getInstance().getUID())
                    .build();
            IO.writeToStream(connection.output, authMessage.toByteArray());
            //Log.d("CLIENT", "Sent UserID");

            //Read Ack from server.
            ProtoMessage.Ack ackMessage = readAck();
            if(ackMessage != null && ackMessage.getType() == 0){
                Log.d("CLIENT", "GOT AUTH ACK");
                readRunnable = new ServerListenRunnable(context, connection, unAckedMessages);
                readThread = new Thread(readRunnable);
                readThread.start();
            }
            else{
                Log.d("CLIENT", "AUTH ACK NOT RECVD");
                if(attempts < 5)
                    //Increment the count and attempt again
                    authenticate(++attempts);
            }
        }

        /**
         * Interpret the ACK sent by the server
         * @return THe ACK Message
         */
        private ProtoMessage.Ack readAck(){
            ProtoMessage.Ack message = null;
            try {
                int msg_size = ByteBuffer.wrap(IO.readFromStream(connection.input, 4)).getInt();
                message = ProtoMessage.Ack.parseFrom(IO.readFromStream(connection.input, msg_size));
            }catch(IOException e){
                e.printStackTrace();
            }
            return message;
        }
    }
}
