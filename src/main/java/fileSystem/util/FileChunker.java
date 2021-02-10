package fileSystem.util;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;

/**
 * Utility functions involving the chunking of a file
 */
public class FileChunker {
    //Could be stored in some general config file both the Client and the Controller have access to
    public static final int CHUNK_SIZE = 65536;  // 1024 * 64

    public static int getChunkNumber(String file) {

        long chunkNumber = 0;
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            long fileLength = raf.length();
            chunkNumber = fileLength / CHUNK_SIZE + fileLength % CHUNK_SIZE != 0 ? 1 : 0;


        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return (int) chunkNumber;
    }


    public static long getFileSize(String localFilePath) {
        return new File(localFilePath).length();
    }

    public static String getChunkHash(byte[] chunkData)  {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return byteArray2Hex(md.digest(chunkData));

    }

    private static String byteArray2Hex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

}

