package fileSystem.util;

import java.io.*;
import java.util.ArrayList;

/**
 * Utility functions involving the chunking of a file
 */
public class FileChunker {
    //Could be stored in some general config file both the Client and the Controller have access to
    public static final int CHUNK_SIZE = 65536;  // 1024 * 64

    public static final ArrayList<Chunk> getChunks(String path) {
        File file = new File(path);

        ArrayList<Chunk> chunks = new ArrayList<>();

        int partCounter = 1;
        byte[] buffer = new byte[CHUNK_SIZE];

        String fileName = file.getName();

        //try-with-resources to ensure closing stream
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            int bytesAmount = 0;
            while ((bytesAmount = bis.read(buffer)) > 0) {
                //write each chunk of data into separate file with different number in name
                String filePartName = String.format("%s.%03d", fileName, partCounter);
                File newFile = new File(file.getParent(), filePartName);
                try (FileOutputStream out = new FileOutputStream(newFile)) {
                    out.write(buffer, 0, bytesAmount);
                    chunks.add(new Chunk(partCounter, newFile));
                }
                partCounter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return chunks;
    }

    public static int getChunkNumber(String file) {

        long chunkNumber = 0;
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            long fileLength = raf.length();
            chunkNumber = fileLength / CHUNK_SIZE +
                    fileLength % CHUNK_SIZE != 0 ? 1 : 0;


        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return (int) chunkNumber;
    }

}

