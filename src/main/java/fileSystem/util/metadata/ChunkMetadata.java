package fileSystem.util.metadata;

import java.util.ArrayList;

/**
 * Metadata for each chunk, contains the chunkNumber of the associated file,
 * and a list of ID's that when used with the ServerHandler, can get relevant details
 * Default size of Chunk is 64kb, default size of slice is 8kb
 */
public class ChunkMetadata {
    int chunkNumber;
    int chunkSize;
    String chunkHash;
    ArrayList<Integer> serversHoldingChunk;
    ArrayList<String> sliceCheckSums;


    public ChunkMetadata(int chunkNumber, int chunkSize, String chunkHash) {
        this.chunkNumber = chunkNumber;
        this.chunkSize = chunkSize;
        this.chunkHash = chunkHash;
        this.serversHoldingChunk = new ArrayList<>();
        this.sliceCheckSums = new ArrayList<>();
    }

    private class SliceMetadata {


    }
}
