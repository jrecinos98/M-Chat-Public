package com.mchat.recinos;

import static org.junit.Assert.*;


import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.firebase.firestore.Blob;
import com.mchat.recinos.Backend.Entities.Contact;
import com.mchat.recinos.Backend.Entities.User;
import com.mchat.recinos.Util.Constants;
import com.mchat.recinos.Util.Util;

import java.util.Date;
import java.util.Map;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class UtilUnitTests {
    //private static final String FAKE_STRING = "HELLO_WORLD";

    private static Map<String, Object> public_data;
    private static final String username = "APES_STRONG";
    private static final byte[] byteKey=  "apestogetherstrong".getBytes(Charsets.UTF_8);

    @Test
    public void testGetExtension(){
        String [] extensions = {"jpg","jpg", "png", "mp4", "java"};
        String [] files = {
                Util.getExtension("doge.jpg"),
                Util.getExtension("ApesTogetherStronk.png.jpg"),
                Util.getExtension("elon.musk.rocks.png"),
                Util.getExtension(".gmeToTheMoon..mp4"),
                Util.getExtension("MakeLife.java")
        };
        assertArrayEquals(extensions, files);
    }
    @Test
    public void testFormatDate(){
        //Time is expected to be in milliseconds.
        long newYears2012_h24_m0_s0 = (new Date(Long.parseLong("1356940800") * 1000)).getTime();
        String expectedNewYears2012 = "Mon 12/31/2012 24:00:00.0000 PST";

        long epoch1970_h12_m0_s0 =  (new Date(Long.parseLong("72000") * 1000)).getTime();
        String expectedEpoch1970 = "Thu 01/01/1970 12:00:00.0000 PST";

        assertEquals(expectedNewYears2012, Util.formatDate(newYears2012_h24_m0_s0));
        assertEquals(expectedEpoch1970, Util.formatDate(epoch1970_h12_m0_s0));
    }
    @Test
    public void testMakeUserObj(){
        Map<String, Object> usr = Util.makeUserObj(username);
        assertEquals(username, usr.get(Constants.USERS_ENTRY.USERNAME));
        assertEquals(1, usr.size());
    }
    @Test
    public void testMakePublicData(){
        String uid= "00000000";
        String name = "Elon Tusk";
        String email = "Ape@gmail.com";
        Blob key = Blob.fromBytes(byteKey);
        String photoURL = "WSBApes.com";

        public_data = Util.makePublicUserData(uid, name,email,byteKey, photoURL);
        assertEquals(uid, public_data.get(Constants.PUBLIC_DATA_ENTRY.UID));
        assertEquals(name, public_data.get(Constants.PUBLIC_DATA_ENTRY.NAME));
        assertEquals(email, public_data.get(Constants.PUBLIC_DATA_ENTRY.EMAIL));
        assertEquals(key, public_data.get(Constants.PUBLIC_DATA_ENTRY.KEY));
        assertEquals(photoURL, public_data.get(Constants.PUBLIC_DATA_ENTRY.PHOTO_URL));
        assertEquals(5, public_data.size());

    }
    //TODO these can go on Contact test.
    @Test
    public void testCreateFriendUser(){
        if(public_data == null)
            //Initialize all the Map fields.
            testMakePublicData();
        //Test whether user constructed from fields will be equivalent.
        User user = new User(
                (String) public_data.get(Constants.PUBLIC_DATA_ENTRY.UID),
                (String) public_data.get(Constants.PUBLIC_DATA_ENTRY.NAME),
                username,
                (String) public_data.get(Constants.PUBLIC_DATA_ENTRY.PHOTO_URL)
        );
        Contact actual = Util.createFriendUser(public_data, username);
        assertEquals(user, actual.getUser());
        assertEquals(new String(byteKey), actual.getPublic_key());
    }
    @Test
    public void testGenerateFileNameWithExtension(){

    }



}