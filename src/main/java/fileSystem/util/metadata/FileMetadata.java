package fileSystem.util.metadata;

import fileSystem.util.metadata.ChunkMetadata;

import java.util.ArrayList;

/**
 * Contains all relevant metadata for a file in the distributed filesystem
 */
public class FileMetadata {
    String fileName;
    int numberOfChunks;
    long fileSize;
    ArrayList<ChunkMetadata> chunkList;

    public FileMetadata(String fileName, int numberOfChunks, long fileSize) {
        this.fileName = fileName;
        this.numberOfChunks = numberOfChunks;
        this.fileSize = fileSize;
        chunkList = new ArrayList<>(numberOfChunks);
    }

}
