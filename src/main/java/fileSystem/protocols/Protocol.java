package fileSystem.protocols;

/*
This is the initial thought process of the structure of the API that the
file system will use. As this is developed it will probably be changed
where there are weaknesses or further features

Different Paths
*********************

# Controller -> ChunkServer
    - Confirm startup
    - Send out major Heartbeat
    - Send out minor Heartbeat
    - Request current files (metadata)

# ChunkServer -> Controller
    - Request to startup
    - Respond to major heartbeat
    - Respond to minor heartbeat
    - Respond with current files (metadata)

# ChunkServer -> ChunkServer
    - Request to store information (Replication factor)
    - ^ Confirm success of received file

# Client -> Controller
    - Request to save file in system
    - Request to get file
    - Request to delete file
    * ^NOTE: These might be able to be combined, we'll see what it looks like later
    - Request current node-list
    - Request current file-list


# Controller -> Client
    - Respond with Servers to send chunked file to
    - Respond with Server to ping for file transfer
    - Respond that file was deleted
    - Respond with current node-list
    - Respond with current file-list

# Client -> ChunkServer
    - Ask for part of file

# ChunkServer -> Client
    - Respond with file chunk

 */


//TODO: Implement above API requests
public class Protocol {
    public static final int CHUNK_SERVER_SENDS_REGISTRATION = 10;
    public static final int CONTROLLER_REPORTS_REGISTRATION_STATUS = 20;
    public static final int CLIENT_SENDS_FILE_SAVE_REQUEST = 30;
    public static final int CONTROLLER_SENDS_CHUNK_SERVER_LIST = 40;
}
