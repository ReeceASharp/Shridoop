package filesystem.node.metadata;

import java.util.ArrayList;

/**
 * Contains all relevant metadata for a file in the distributed filesystem
 */
public class FileMetadata {
    public final String fileName;
    public final long fileSize;
    public final ArrayList<ChunkMetadata> chunkList;

    public FileMetadata(String fileName, int numberOfChunks, long fileSize) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.chunkList = new ArrayList<>(numberOfChunks);
    }

    @Override
    public String toString() {
        return "FileMetadata{" +
                "fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", chunkList=" + chunkList +
                '}';
    }

//    TODO: Add functionality for Controller to push info to the through an update function here, receiving information
//      from each chunkServer recordhandler

}
