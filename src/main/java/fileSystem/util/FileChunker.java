package fileSystem.util;

import java.io.*;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

/**
 * Utility functions involving the chunking of a file
 */
public class FileChunker {
    //Could be stored in some general config file both the Client and the Controller have access to
    public static final int CHUNK_SIZE = 65536;  // 1024 * 64

    public static int getChunkNumber(Path filePath) {
        long fileLength = getFileSize(filePath);
        return (int) (fileLength / CHUNK_SIZE) + (fileLength % CHUNK_SIZE != 0 ? 1 : 0);
    }


    public static long getFileSize(Path filePath) {
        return filePath.toFile().length();
    }

    public static String getChunkHash(byte[] chunkData) {
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

    public static byte[] buildChunk(byte[] buffer, BufferedInputStream bis) {
        try {
            int bytesRead = bis.read(buffer);
            byte[] chunkBytes = new byte[bytesRead];
            System.arraycopy(buffer, 0, chunkBytes, 0, bytesRead);
            return chunkBytes;
        } catch (IOException e) {
            return null;
        }
    }

}

