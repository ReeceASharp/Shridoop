package fileSystem.node.server;

import java.nio.file.Path;

/**
 * Data structure that holds data about the file-chunk (metadata), but not the actual file data
 */
public class FileChunkData {
    private final Path path;
    private final int version;
    private final String sequence;
    private final String timestamp;

    public FileChunkData(Path path, int version, String sequence, String timestamp) {
        this.path = path;
        this.version = version;
        this.sequence = sequence;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "FileChunkData{" +
                "path=" + path +
                ", version=" + version +
                ", sequence='" + sequence + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
