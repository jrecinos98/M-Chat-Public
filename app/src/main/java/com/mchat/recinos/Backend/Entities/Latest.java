package com.mchat.recinos.Backend.Entities;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity (tableName = "latest_message")
public class Latest {
    @PrimaryKey
    public int entry_id=0;
    public long id;
    public int cid;
    @Ignore
    public Latest(long id, int cid){
        this.id = id;
        this.cid = cid;
    }
    public Latest(int entry_id, long id, int cid){
        this.entry_id = entry_id;
        this.id=id;
        this.cid=cid;
    }
    public void setId(int id){this.id =id;}
    public void setCid(int cid){this.cid = cid;}
    public void setEntry_id(int entry_id){this.entry_id= entry_id;}
}
