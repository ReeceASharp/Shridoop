package fileSystem.util;

import fileSystem.util.metadata.FileChunkData;

import java.io.*;
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
    // TODO: Move over this system to the full metadata version, which supports checksum slice handling
    //private final ArrayList<FullChunkMetadata> fileChunks;

    //path of where the FileHandler starts its storage system
    private final Path homePath;

    public FileHandler(String homePath) {
        this.homePath = Paths.get(homePath);
        this.fileChunks = new ArrayList<>();
    }


    public synchronized byte[] getFileData(String file) {

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
     * Resolves the passed local path of the file with the current home directory of this Handler, and gets the full
     * pathname of requested file
     *
     * @param localPath
     * @return
     */
    private Path resolveFileName(String localPath) {
        Path resolvedPath = homePath.resolve(localPath);
        return resolvedPath.toAbsolutePath();
    }

    public synchronized void storeFileChunk(String path, byte[] chunkData, String fileHash) {
        //output to

        Path localPath = Paths.get(path);
        fileChunks.add(new FileChunkData(localPath, 1, fileHash));

        //use full path to store data
        Path fullPath = resolveFileName(localPath);

        //create a file using the full path to output to
        File file = new File(fullPath.toString());
        try (OutputStream os = new FileOutputStream(file)) {
            os.write(chunkData);
            os.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //TODO: generate slices + SHA1 values

    }

    /**
     * Resolves the passed local path of the file with the current home directory of this Handler, and gets the full
     * pathname of requested file
     *
     * @return
     */
    private Path resolveFileName(Path localPath) {
        Path resolvedPath = homePath.resolve(localPath);
        return resolvedPath.toAbsolutePath();
    }

    public synchronized ArrayList<FileChunkData> getFileChunks() {
        return fileChunks;
    }
}
