package filesystem.node.metadata;

import filesystem.util.Utils;

import java.util.Collections;
import java.util.TreeMap;

/**
 * Contains all relevant metadata for a file in the distributed filesystem
 */
public class FileMetadata {
    public final String fileName;
    public final long fileSize;
    public final TreeMap<Integer, ChunkMetadata> chunkList;

    public FileMetadata(String fileName, long fileSize) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.chunkList = new TreeMap<>();
    }

    public ChunkMetadata getChunkMetadata(int chunkNumber) {
        return chunkList.get(chunkNumber);
    }

    public void addChunkMetadata(int chunkNumber, ChunkMetadata cmd) {
        chunkList.put(chunkNumber, cmd);
    }

    @Override
    public String toString() {
        return String.format(
                "FileName: %s%n" +
                        "FileSize: %d%n" +
                        "%s",
                fileName, fileSize,
                Utils.GenericListFormatter.getFormattedOutput(Collections.singletonList(chunkList.values()), "|", true));
    }

//    TODO: Add functionality for Controller to push info to the through an update function here, receiving information
//      from each chunkServer recordhandler

}
