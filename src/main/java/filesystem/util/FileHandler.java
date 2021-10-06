package filesystem.util;

import filesystem.node.metadata.ChunkMetadata;
import filesystem.protocol.RecordKeeper;
import filesystem.protocol.records.ChunkAdd;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static filesystem.util.Utils.appendLn;
import static filesystem.util.Utils.resolveFileName;

/**
 * Goal of the fileHandler is to keep track of the files, used by the ChunkHolder
 */
public class FileHandler {
    //list of known chunks stored by the ChunkHolder
    private final Map<String, ArrayList<ChunkMetadata>> fileChunks;
    private final RecordKeeper recordKeeper;

    //path of where the FileHandler starts its storage system
    protected final Path homePath;

    public FileHandler(String homePath) {
        this.homePath = Paths.get(homePath);
        this.recordKeeper = new RecordKeeper();
        this.fileChunks = new HashMap<>();
    }


    public synchronized byte[] getFileData(String file) {

        Path absolutePath = resolveFileName(homePath, Paths.get(file));
        byte[] fileData = null;
        try {
            fileData = Files.readAllBytes(absolutePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileData;
    }

    public synchronized void storeFileChunk(String path, ChunkMetadata cmd, byte[] chunkData,boolean writeToDisk) {
        //use full-path to store data
        Path fullPath = resolveFileName(homePath, Paths.get(path));

        if (writeToDisk)
            writeDataToDisk(fullPath, chunkData);

        if (!fileChunks.containsKey(path)) {
            fileChunks.put(path, new ArrayList<>());
        }
        fileChunks.get(path).add(cmd);

        //Store the update in records to be sent to the Controller
        recordKeeper.addRecord(new ChunkAdd(path, cmd.chunkNumber, cmd.chunkHash));
    }

    public synchronized void writeDataToDisk(Path fullPath, byte[] data) {
        //create a file using the full path to output to
        File file = new File(fullPath.toString());
        try (OutputStream os = new FileOutputStream(file)) {
            os.write(data);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        appendLn(sb, String.format("Path: %s", homePath));

        boolean showFieldNames = true;

        for (Entry<String, ArrayList<ChunkMetadata>> file : fileChunks.entrySet()) {
            String chunkDump = Utils.GenericListFormatter.getFormattedOutput(file.getValue(), "|", showFieldNames);
            sb.append(chunkDump);

            // Show only the field names at the top
            if (showFieldNames)
                showFieldNames = false;
        }

        sb.append(Utils.GenericListFormatter.getFormattedOutput(recordKeeper.getRecords(false), "|", showFieldNames));

        return sb.toString();
    }
}
