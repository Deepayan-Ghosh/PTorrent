package com.torrent.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class TorrentUtils {
    public static InheritableThreadLocal<Integer> t = new InheritableThreadLocal<>() {
        @Override
        protected Integer childValue(Integer s) {
            return s;
        }
    };
    public static int getTransactionId32() {
        return new Random().nextInt();
    }

    public static byte[] getRandomBytes(int nBytes) {
        byte[] randomBytes = new byte[nBytes];
        new Random().nextBytes(randomBytes);
        return randomBytes;
    }

    public static byte[] getSHA1(byte[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            md.reset();
            md.update(input);
            byte[] messageDigest = md.digest();
            return messageDigest;
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }

}
