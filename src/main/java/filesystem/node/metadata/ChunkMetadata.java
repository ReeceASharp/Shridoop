package filesystem.node.metadata;


public abstract class ChunkMetadata {
    public int chunkNumber;
    public int chunkSize;
    public String chunkHash;

    public ChunkMetadata(int chunkNumber,
                         int chunkSize,
                         String chunkHash) {
        this.chunkNumber = chunkNumber;
        this.chunkSize = chunkSize;
        this.chunkHash = chunkHash;
    }

    protected interface ChunkMetadataExtraction {

    }
}
