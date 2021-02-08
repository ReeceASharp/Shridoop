package fileSystem.node.controller;

import fileSystem.transport.SocketStream;

import java.io.IOException;
import java.net.Socket;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

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
    private final FileMetadataHandler fileHandler;
    private final ServerMetadataHandler serverHandler;

    public ClusterInformationHandler() {
        fileHandler = new FileMetadataHandler();
        serverHandler = new ServerMetadataHandler();
    }

    /**
     * An object holding all of the current metadata for the ChunkServers
     */
    private class ServerMetadataHandler {
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
        public synchronized Socket getServer(Socket socket) {
            Optional<ServerMetadata> server = currentServers.stream().filter(metadata->metadata.socketStream.socket.equals(socket)).findFirst();
            return server.map(serverMetadata -> serverMetadata.socketStream.socket).orElse(null);
        }

        /**
         * Private data structure used by the ServerMetadata to contruct separate objects for each ChunkServer
         */
        private class ServerMetadata {
            final String nickname;
            final String host;
            final int port;
            final SocketStream socketStream;
            final UUID serverID;
            String heartbeatTimestamp;

            public ServerMetadata(String nickname, String host, int port, Socket socket, String heartbeatTimestamp) throws IOException {
                this.nickname = nickname;
                this.host = host;
                this.port = port;
                this.socketStream = new SocketStream(socket);
                this.heartbeatTimestamp = heartbeatTimestamp;

                this.serverID = UUID.randomUUID();
            }

            public String getNickname() {
                return nickname;
            }

            public String getHost() {
                return host;
            }

            public int getPort() {
                return port;
            }


            public UUID getServerID() {
                return serverID;
            }

            public String getHeartbeatTimestamp() {
                return heartbeatTimestamp;
            }
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
        private class FileMetadata {
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

        /**
         * Metadata for each chunk, contains the chunkNumber of the associated file,
         * and a list of ID's that when used with the ServerHandler, can get relevant details
         * Default size of Chunk is 64kb, default size of slice is 8kb
         */
        private class ChunkMetadata {
            int chunkNumber;
            int chunkSize;
            String chunkHash;
            ArrayList<Integer> serversHoldingChunk;
            ArrayList<String> sliceCheckSums;


            public ChunkMetadata(int chunkNumber, int chunkSize, String chunkHash) {
                this.chunkNumber = chunkNumber;
                this.chunkSize = chunkSize;
                this.chunkHash = chunkHash;
                this.serversHoldingChunk = new ArrayList<>();
                this.sliceCheckSums = new ArrayList<>();
            }
        }

    }


}
