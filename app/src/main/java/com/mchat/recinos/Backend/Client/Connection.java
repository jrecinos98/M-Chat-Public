package com.mchat.recinos.Backend.Client;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Struct to hold server data
 */
class Connection{
    String IP;
    int PORT;
    OutputStream output;
    InputStream input;
    STATUS status;
    public enum STATUS{
        OFF,
        IN_PROGRESS,
        ON
    }
    Connection(){
        IP ="";
        PORT = 0;
        status = STATUS.OFF;
    }
    Connection(String ip, int port){
        IP = ip;
        PORT = port;
        status = STATUS.OFF;
    }
    boolean isOpen(){
        return status == STATUS.IN_PROGRESS || status == STATUS.ON;
    }
    void shutOff(){
        status = STATUS.OFF;
    }
    void setInfo(String ip, int port){
        IP = ip;
        PORT = port;
    }
}