package com.mchat.recinos.Util;

import android.content.Context;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.security.auth.x500.X500Principal;

import static android.security.keystore.KeyProperties.KEY_ALGORITHM_RSA;

public class Encryption {
    public static KeyStore getKeyProvider(){
        KeyStore provider = null;
        try{
            provider = KeyStore.getInstance(CONSTANTS.KEYSTORE_PROVIDER);
        }catch(KeyStoreException e){
            e.printStackTrace();
        }
        return provider;
    }
    public static Key getKey(CONSTANTS.KEY key_type){
        Key key = null;
        try {
            KeyStore provider = KeyStore.getInstance(CONSTANTS.KEYSTORE_PROVIDER);
            provider.load(null, null);
            KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) provider.getEntry(CONSTANTS.DEFAULT_KEY_ALIAS, null);
            key = key_type == CONSTANTS.KEY.PUBLIC? entry.getCertificate().getPublicKey() : entry.getPrivateKey();
        }catch(NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException | CertificateException | IOException e){
            e.printStackTrace();
        }
        return key;
    }
    public static boolean generateUserKeys(Context context){
        boolean success = true;
        try {
            //Calendar objects to set the valid period of the keys
            Calendar notBefore = Calendar.getInstance();
            Calendar notAfter = Calendar.getInstance();
            notAfter.add(Calendar.YEAR, 1);
            //If SDK version is greater than SDK 23
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                        //The alias for the keys
                        CONSTANTS.DEFAULT_KEY_ALIAS,
                        KeyProperties.PURPOSE_DECRYPT | KeyProperties.PURPOSE_ENCRYPT)
                        //  RSA/ECB/PKCS1Padding
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                        .setKeySize(2048)
                        // *** Replaced: setStartDate
                        .setKeyValidityStart(notBefore.getTime())
                        // *** Replaced: setEndDate
                        .setKeyValidityEnd(notAfter.getTime())
                        // *** Replaced: setSubject
                        .setCertificateSubject(new X500Principal("CN=test"))
                        // *** Replaced: setSerialNumber
                        .setCertificateSerialNumber(BigInteger.ONE)
                        .build();
                //Once generated the keys are safely stored on the provider (AndroidKeyStore)
                KeyPairGenerator keyGen = KeyPairGenerator.getInstance(KEY_ALGORITHM_RSA, CONSTANTS.KEYSTORE_PROVIDER);
                keyGen.initialize(spec);
                keyGen.generateKeyPair();
            }
            //If SDK version is below 23
            else {
                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec
                        .Builder(context)
                        .setAlias(CONSTANTS.DEFAULT_KEY_ALIAS)
                        .setSubject(new X500Principal("CN=Your Company ," +
                                " O=Your Organization" +
                                " C=Your Coountry"))
                        .setKeySize(2048)
                        .setSerialNumber(BigInteger.ONE)
                        .setStartDate(notBefore.getTime())
                        .setEndDate(notAfter.getTime())
                        .build();
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", CONSTANTS.KEYSTORE_PROVIDER);
                generator.initialize(spec);
                generator.generateKeyPair();
            }
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
            success =false;
        }
        return success;
    }
    public static PublicKey publicKeyFromBytes(byte[] key){
        PublicKey pubKey = null;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            //Turn bytes back to Public Key
            pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(key));
        }catch(Exception e){
            e.printStackTrace();
        }
        return pubKey;
    }
    //TODO instead of directly using private key to encrypt string use a symmetrical key method. Encrypt the string with symmetric key and then encrypt the symmetric key with private key
    //Maybe use the same symmetric key per session or using a time limit.
    public static String encryptMessage(String txt, PublicKey pubKey){
        String encoded = "";
        byte[] encrypted = null;
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING"); //or try with "RSA"
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            encrypted = cipher.doFinal(txt.getBytes());
            encoded = Base64.encodeToString(encrypted, Base64.DEFAULT);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return encoded;
    }
    public static String decryptMessage(String encrypted){
        String decoded="";
        byte[] decrypted = null;
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, getKey(CONSTANTS.KEY.PRIVATE));
            decrypted = cipher.doFinal(encrypted.getBytes());
            decoded = Base64.encodeToString(decrypted, Base64.DEFAULT);
        }catch(Exception e){
            e.printStackTrace();
        }
        return decoded;
    }
}
