package filesystem.util.metadata;

import filesystem.util.Pair;

import java.util.ArrayList;


/**
 * Metadata for each chunk, contains the chunkNumber of the associated file,
 * and a list of ID's that when used with the ServerHandler, can get relevant details
 * Default size of Chunk is 64kb
 */
public class LiteChunkMetadata {
    int chunkNumber;
    int chunkSize;
    String chunkHash;
    ArrayList<Pair<String, Integer>> serversHoldingChunk;

    public LiteChunkMetadata(int chunkNumber,
                             int chunkSize,
                             String chunkHash,
                             ArrayList<Pair<String, Integer>> serversHoldingChunk) {
        this.chunkNumber = chunkNumber;
        this.chunkSize = chunkSize;
        this.chunkHash = chunkHash;
        this.serversHoldingChunk = serversHoldingChunk;

    }

    //TODO: Refactor so that the chunk hash info is generated in the constructor, and the data itself is passed in to
    // be processed


    @Override
    public String toString() {
        return "LiteChunkMetadata{" +
                       "chunkNumber=" + chunkNumber +
                       ", chunkSize=" + chunkSize +
                       ", chunkHash='" + chunkHash + '\'' +
                       ", serversHoldingChunk=" + serversHoldingChunk +
                       '}';
    }
}
