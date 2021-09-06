package fileSystem.util.metadata;

import fileSystem.util.*;

import java.util.*;
import java.util.stream.*;

/**
 * Contains all relevant metadata for a file in the distributed filesystem
 */
public class FileMetadata {
    public final String fileName;
    public final int numberOfChunks;
    public final long fileSize;
    public final ArrayList<LiteChunkMetadata> chunkList;

    public FileMetadata(String fileName, int numberOfChunks, long fileSize) {
        this.fileName = fileName;
        this.numberOfChunks = numberOfChunks;
        this.fileSize = fileSize;
        this.chunkList = new ArrayList<>(numberOfChunks);
    }

    @Override
    public String toString() {
        return "FileMetadata{" +
                       "fileName='" + fileName + '\'' +
                       ", numberOfChunks=" + numberOfChunks +
                       ", fileSize=" + fileSize +
                       ", chunkList=" + chunkList +
                       '}';
    }

    public ArrayList<ContactList> getChunkLocations() {
        return (ArrayList<ContactList>) chunkList.stream()
                                                .map(chunk ->
                                                             new ContactList(
                                                                     chunk.chunkNumber,
                                                                     chunk.serversHoldingChunk))
                                                .collect(Collectors.toList());
    }

//    TODO: Add functionality for Controller to push info to the through an update function here, receiving information
//      from each chunkServer recordhandler

}
