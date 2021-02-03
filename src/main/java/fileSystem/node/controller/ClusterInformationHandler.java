package fileSystem.node.controller;

import java.net.Socket;
import java.util.ArrayList;

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

        /**
         * Private data structure used by the ServerMetadata to contruct separate objects for each ChunkServer
         */
        private class ServerMetadata {
            String nickname;
            String host;
            int port;
            Socket socket;
            String heartbeatTimestamp;
            int serverID;

            public ServerMetadata(String nickname, String host, int port, Socket socket, String heartbeatTimestamp) {
                this.nickname = nickname;
                this.host = host;
                this.port = port;
                this.socket = socket;
                this.heartbeatTimestamp = heartbeatTimestamp;
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
         */
        private class ChunkMetadata {
            int chunkNumber;
            ArrayList<Integer> serversHoldingChunk;
            //String chunkHash;


            public ChunkMetadata(int chunkNumber) {
                this.chunkNumber = chunkNumber;
                this.serversHoldingChunk = new ArrayList<>();
            }
        }

    }

}
