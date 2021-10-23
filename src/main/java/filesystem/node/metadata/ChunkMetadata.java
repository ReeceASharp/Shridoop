package filesystem.node.metadata;


public abstract class ChunkMetadata {
    public String fileName;
    public int chunkNumber;
    public int chunkSize;
    public String chunkHash;

    public ChunkMetadata(String fileName,
                         int chunkNumber,
                         int chunkSize,
                         String chunkHash) {
        this.fileName = fileName;
        this.chunkNumber = chunkNumber;
        this.chunkSize = chunkSize;
        this.chunkHash = chunkHash;
    }

    protected interface ChunkMetadataExtraction {

    }
}
