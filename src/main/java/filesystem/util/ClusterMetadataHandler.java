package filesystem.util;

import filesystem.node.metadata.FileMetadata;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;

/**
 * This object contains all useful information about the cluster. It is updated by the heartbeats send to and from
 * the ChunkHolders along with containing startup information
 */
public class ClusterMetadataHandler {
    private final ArrayList<HolderMetadata> currentServers;
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

    public ArrayList<HolderMetadata> getServers() {
        return currentServers;
    }

    public void addFile(String file, int totalChunks, long fileSize) {
        currentFiles.put(file, new FileMetadata(file, fileSize));
    }

    public synchronized void addServer(String serverName, InetSocketAddress address, Socket socket) {
        String heartbeatStamp = Utils.timestampNowString();
        currentServers.add(new HolderMetadata(serverName, address, socket, heartbeatStamp));
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
    public synchronized HolderMetadata getServer(Socket socket) {
        Optional<HolderMetadata> server = currentServers.stream().filter(metadata -> metadata.socket.equals(socket)).findFirst();
        return server.orElse(null);
    }

    public synchronized HolderMetadata getServer(InetSocketAddress address) {
        Optional<HolderMetadata> server = currentServers.stream().filter(metadata -> metadata.address.equals(address)).findFirst();
        return server.orElse(null);
    }


    public synchronized void updateHeartBeatBySocket(Socket socket) {
        HolderMetadata server = getServer(socket);
        if (server == null)
            return;
        server.heartbeatTimestamp = Utils.timestampNowString();
    }

    /**
     * Used by the
     */
    public static class HolderMetadata {
        public final String serverName;
        public final InetSocketAddress address;
        public final Socket socket;
        public final UUID serverID;
        public String heartbeatTimestamp;

        public HolderMetadata(String serverName, InetSocketAddress address, Socket socket, String heartbeatTimestamp) {
            this.serverName = serverName;
            this.address = address;
            this.socket = socket;
            this.heartbeatTimestamp = heartbeatTimestamp;
            this.serverID = UUID.randomUUID();
        }

        @Override
        public String toString() {
            return "ServerMetadata{" +
                           "serverName='" + serverName +
                           ", address='" + address +
                           ", socket=" + socket +
                           ", serverID=" + serverID +
                           ", heartbeatTimestamp='" + heartbeatTimestamp + '\'' +
                           '}';
        }


    }
}
