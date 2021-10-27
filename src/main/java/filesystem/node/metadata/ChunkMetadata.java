package filesystem.node.metadata;


import java.io.Serializable;

public abstract class ChunkMetadata implements Comparable<ChunkMetadata>, Serializable {
    public String fileName;
    public int chunkNumber;
    public int chunkSize;
    public String chunkHash;

    @Override
    public int compareTo(ChunkMetadata o) {
        return this.chunkNumber - o.chunkNumber;
    }

    public ChunkMetadata(String fileName,
                         int chunkNumber,
                         int chunkSize,
                         String chunkHash) {
        this.fileName = fileName;
        this.chunkNumber = chunkNumber;
        this.chunkSize = chunkSize;
        this.chunkHash = chunkHash;
    }
}


// add example_files/fileOne.txt dsajklda