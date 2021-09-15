package filesystem.util;

import filesystem.util.metadata.FileMetadata;
import filesystem.util.metadata.ServerMetadata;

import java.net.Socket;
import java.time.Instant;
import java.util.*;

/**
 * This object contains all useful information about the cluster. It is updated by the heartbeats send to and from
 * the ChunkServers along with containing startup information
 * <p>
 * ServerMetadata
 * - Current ChunkServers
 * - Host
 * - port
 * - socket
 * - serverName
 * - timestamp of last heartbeat
 * - ID to uniquely identify server, used by the FileMetadata to
 * reference the servers holding chunks
 * <p>
 * FileMetadata
 * - Master list of files
 * - name of file
 * - # of chunks
 * - size of file
 * - complete file hash
 * - ChunkList
 * - chunkNumber
 * - ChunkServers holding that file chunk
 */
public class ClusterMetadataHandler {
    private final ArrayList<ServerMetadata> currentServers;
    private final Map<String, FileMetadata> currentFiles;

    public ClusterMetadataHandler() {
        currentServers = new ArrayList<>();
        currentFiles = new HashMap<>();
    }

    public Collection<FileMetadata> getFiles() {
        return currentFiles.values();
    }

    public FileMetadata getFile(String filePath) {
        return currentFiles.getOrDefault(filePath, null);
    }

    public ArrayList<ServerMetadata> getServers() {
        return currentServers;
    }

    public void addFile(String file, int totalChunks, long fileSize) {
        currentFiles.put(file, new FileMetadata(file, totalChunks, fileSize));
    }

    public synchronized void addServer(String serverName, String host, int port, Socket socket) {
        String heartbeatStamp = Instant.now().toString();
        currentServers.add(new ServerMetadata(serverName, host, port, socket, heartbeatStamp));
    }

    public synchronized boolean removeBySocket(Socket socket) {
        return currentServers.remove(getServer(socket));
    }

    /**
     * Attempting to use java streams to find the relevant server based on known connection
     *
     * @param socket
     * @return
     */
    public synchronized ServerMetadata getServer(Socket socket) {
        Optional<ServerMetadata> server = currentServers.stream().filter(metadata -> metadata.socket.equals(socket)).findFirst();
        return server.orElse(null);
    }


}
