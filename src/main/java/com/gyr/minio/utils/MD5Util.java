package com.gyr.minio.utils;

import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {
    public static String computeMD5(File file) {
        DigestInputStream din = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            //第一个参数是一个输入流
            din = new DigestInputStream(new BufferedInputStream(new FileInputStream(file)), md5);
            byte[] b = new byte[1024];
            while (din.read(b) != -1) ;
            byte[] digest = md5.digest();
            return DatatypeConverter.printHexBinary(digest);
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (din != null) {
                    din.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}

