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
     * @return
     */
    private Path resolveFileName(Path localPath) {
        Path resolvedPath = homePath.resolve(localPath);
        return resolvedPath.toAbsolutePath();
    }

    /**
     * Resolves the passed local path of the file with the current home directory of this Handler, and gets the full
     * pathname of requested file
     * @param localPath
     * @return
     */
    private Path resolveFileName(String localPath) {
        Path resolvedPath = homePath.resolve(localPath);
        return resolvedPath.toAbsolutePath();
    }

    public synchronized void storeFileChunk(String path, byte[] chunkData, String fileHash) throws IOException {


        //Add fileMetadata to memory
        Path localPath = Paths.get(path);
        fileChunks.add(new FileChunkData(localPath, 1, fileHash));

        //Get full path to properly save file data
        Path fullPath = resolveFileName(localPath);

        Files.createDirectories(fullPath);

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

    public synchronized boolean createDirectory(String localPath) {
        Path resolvedPath = resolveFileName(localPath);

        boolean isValidPath = checkPath(resolvedPath);

        boolean created;
        File resolvedDirectory = new File(resolvedPath.getParent().toString());
        if (isValidPath)


        //edge case issue with checkPath
        if (new File)

    }

    /**
     * Checks to see if the resolved path given (HomePath + local path) is empty, or is already a directory
     * One potential issue is that this doesn't take into account the case where a part of the path is a filename
     * and not just the parent folder.
     * @param resolvedPath
     * @return
     */
    private synchronized boolean checkPath(Path resolvedPath) {
        File resolvedFile = new File(resolvedPath.toString());
        if (resolvedFile.exists())
            return false;

        File resolvedDirectory = new File(resolvedFile.getParent());
        if (resolvedDirectory.isFile())
            return false;

        //TODO: fix case where a local file part of a path: /FOLDER/FILE/FOLDER/FOLDER

        return true;
    }




    public synchronized ArrayList<FileChunkData> getFileChunks() {
        return fileChunks;
    }
}
