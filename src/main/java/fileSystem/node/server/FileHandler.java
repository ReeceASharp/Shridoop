package fileSystem.node.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Goal of the fileHandler is to keep track of the files, used by the ChunkServer
 */
public class FileHandler {
    //list of known chunks stored by the ChunkServer
    private final ArrayList<FileChunkData> fileChunks;
    //path of where the FileHandler starts its storage system
    private final Path homePath;

    public FileHandler(String homePath) {
        this.homePath = Paths.get(homePath);
        this.fileChunks = new ArrayList<>();
    }


    public byte[] getFileData(String file) {
        Path absolutePath = resolveFileName(file);
        byte[] fileData = null;
        try {
            fileData = Files.readAllBytes(absolutePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileData;
    }

    /**
     * Resolves the passed fileName with the current home directory of this Handler, and gets the full
     * pathname of requested file
     *
     * @return
     */
    private Path resolveFileName(String localPath) {
        Path resolvedPath = homePath.resolve(localPath);
        return resolvedPath.toAbsolutePath();
    }


}
