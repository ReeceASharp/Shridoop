package filesystem.util.metadata;

import java.net.URL;
import java.util.ArrayList;


/**
 * Metadata for each chunk, contains the chunkNumber of the associated file,
 * and a list of ID's that when used with the ServerHandler, can get relevant details
 * Default size of Chunk is 64kb
 */
public class LiteChunkMetadata {
    public int chunkNumber;
    public int chunkSize;
    public String chunkHash;
    public ArrayList<URL> serversHoldingChunk;

    public LiteChunkMetadata(int chunkNumber,
                             int chunkSize,
                             String chunkHash,
                             ArrayList<URL> serversHoldingChunk) {
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
