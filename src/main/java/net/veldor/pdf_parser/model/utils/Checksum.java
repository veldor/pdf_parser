package net.veldor.pdf_parser.model.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;

public class Checksum {

    public static String checksum(File input) {
        InputStream in = null;
        try {
            in = Files.newInputStream(input.toPath());
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] block = new byte[4096];
            int length;
            while ((length = in.read(block)) > 0) {
                digest.update(block, 0, length);
            }
            byte[] b = digest.digest();
            StringBuilder result = new StringBuilder();

            for (byte value : b) {
                result.append(Integer.toString((value & 0xff) + 0x100, 16).substring(1));
            }
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if(in != null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("something wrong with md5...");
        return null;
    }
}
