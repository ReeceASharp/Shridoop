package filesystem.util.metadata;

import java.net.Socket;
import java.net.URL;
import java.util.UUID;

/**
 * Private data structure used by the ServerMetadata to contruct separate objects for each ChunkServer
 */
public class ServerMetadata {
    public final String serverName;
    public final URL url;
    public final Socket socket;
    public final UUID serverID;
    public String heartbeatTimestamp;

    public ServerMetadata(String serverName, URL url, Socket socket, String heartbeatTimestamp) {
        this.serverName = serverName;
        this.url = url;
        this.socket = socket;
        this.heartbeatTimestamp = heartbeatTimestamp;
        this.serverID = UUID.randomUUID();
    }

    @Override
    public String toString() {
        return "ServerMetadata{" +
                       "serverName='" + serverName +
                       ", url='" + url +
                       ", socket=" + socket +
                       ", serverID=" + serverID +
                       ", heartbeatTimestamp='" + heartbeatTimestamp + '\'' +
                       '}';
    }


}
