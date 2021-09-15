package fileSystem.util.metadata;

import java.net.Socket;
import java.util.UUID;

/**
 * Private data structure used by the ServerMetadata to contruct separate objects for each ChunkServer
 */
public class ServerMetadata {
    public final String serverName;
    public final String host;
    public final int port;
    public final Socket socket;
    public final UUID serverID;
    public String heartbeatTimestamp;

    public ServerMetadata(String serverName, String host, int port, Socket socket, String heartbeatTimestamp) {
        this.serverName = serverName;
        this.host = host;
        this.port = port;
        this.socket = socket;
        this.heartbeatTimestamp = heartbeatTimestamp;
        this.serverID = UUID.randomUUID();
    }

    @Override
    public String toString() {
        return "ServerMetadata{" +
                       "serverName='" + serverName + '\'' +
                       ", host='" + host + '\'' +
                       ", port=" + port +
                       ", socket=" + socket +
                       ", serverID=" + serverID +
                       ", heartbeatTimestamp='" + heartbeatTimestamp + '\'' +
                       '}';
    }
}
