package fileSystem.metadata;

import fileSystem.metadata.ChunkMetadata;
import fileSystem.metadata.ServerMetadata;
import fileSystem.transport.SocketStream;

import java.io.IOException;
import java.net.Socket;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;

/**
 * This object contains all useful information about the cluster. It is updated by the heartbeats send to and from
 * the ChunkServers along with containing startup information
 * <p>
 * ServerMetadata
 * - Current ChunkServers
 * - Host
 * - port
 * - socket
 * - nickname
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
public class ClusterInformationHandler {
    public final FileMetadataHandler fileHandler;
    public final ServerMetadataHandler serverHandler;

    public ClusterInformationHandler() {
        fileHandler = new FileMetadataHandler();
        serverHandler = new ServerMetadataHandler();
    }

    public ArrayList<ServerMetadata> getServers() {
        return serverHandler.currentServers;
    }


    /**
     * An object holding all of the current metadata for the ChunkServers
     */
    public class ServerMetadataHandler {
        private final ArrayList<ServerMetadata> currentServers;

        public ServerMetadataHandler() {
            this.currentServers = new ArrayList<>();
        }

        public synchronized void addServer(String nickname, String host, int port, Socket socket) throws IOException {
            String heartbeatStamp = Instant.now().toString();
            currentServers.add(new ServerMetadata(nickname, host, port, socket, heartbeatStamp));
        }

        /**
         * Attempting to use java streams to find the relevant server based on known connection
         * TODO: FIX
         * @param socket
         * @return
         */
        public synchronized ServerMetadata getServer(Socket socket) {
            Optional<ServerMetadata> server = currentServers.stream().filter(metadata->metadata.socket.equals(socket)).findFirst();
            return server.orElse(null);
        }

        public boolean removeBySocket(Socket socket) {
            return currentServers.remove(getServer(socket));
        }

    }

    /**
     * Contains, organizes, and parses all requests regarding current files in the distributed filesystem
     */
    private class FileMetadataHandler {
        private final ArrayList<FileMetadata> currentFiles;

        public FileMetadataHandler() {
            this.currentFiles = new ArrayList<>();
        }

        /**
         * Contains all relevant metadata for a file in the distributed filesystem
         */
        public class FileMetadata {
            String fileName;
            int numberOfChunks;
            int fileSize;
            String FileHash;
            ArrayList<ChunkMetadata> chunkList;

            public FileMetadata(String fileName, int numberOfChunks, int fileSize, String fileHash) {
                this.fileName = fileName;
                this.numberOfChunks = numberOfChunks;
                this.fileSize = fileSize;
                FileHash = fileHash;
                chunkList = new ArrayList<>();
            }
        }

    }


}
