package fileSystem.protocols;

/*
This is the initial thought process of the structure of the API that the
file system will use. As this is developed it will probably be changed
where there are weaknesses or further features

Different Paths
*********************

# Controller -> ChunkServer
    - Confirm startup
        ControllerReportsRegistrationStatus
    - Request shutdown
        ControllerRequestsDeregistration
    - Request Heartbeat that ChunkServer is still alive
        ControllerRequestsFunctionalHeartbeat
    - Request current files (metadata)
        ControllerRequestsFileMetadata

# ChunkServer -> Controller
    - Request to startup
        ChunkServerSendRegistration
    - Respond to shutdown
        ChunkServerReportsDeregistrationStatus
    - Respond to heartbeat
        ChunkServerReportsHeartbeat
    - Respond to minor heartbeat
        ChunkServerReportsMinorHeartbeat
    - Respond with current files (metadata)
        ChunkServerReportsFileChunkMetadata

# ChunkServer -> ChunkServer
    - Request to store information (Replication factor)
        ChunkServerRequestsReplication
    - ^ Confirm success of received file
        ChunkServerReportsReplication

# Controller -> Client
    - Respond with Servers to send chunked file to
        ControllerReportsFileChunkAddDestination
    - Respond with Server to ping for file transfer
        ControllerReportsFileChunkRequestLocation
    - Respond that file was deleted
        ControllerReportsFileDeleteStatus
    - Respond with current node-list
        ControllerReportsChunkServerMetadata
    - Respond with current file-list
        ControllerReportsFileMetadata

# Client -> Controller
    - Request to do something with a file (ADD, DELETE, GET)
        ClientRequestsFileDelete
    * ^NOTE: These might be able to be combined, we'll see what it looks like later
    - Request current node-list
        ClientRequestsChunkServerMetadata
    - Request current file-list
        ClientRequestsFileMetadata

# Client -> ChunkServer
    - Ask for part of file
        ClientRequestsFileChunk

# ChunkServer -> Client
    - Respond with file chunk
        ChunkServerSendsFileChunk

 */


//TODO: Implement above API requests
public class Protocol {
    //Protocol response values, used in responses to show the status of a request
    public static final int RESPONSE_SUCCESS = 1;
    public static final int RESPONSE_FAILURE = 2;

    public static final int HEARTBEAT_MINOR = 3;
    public static final int HEARTBEAT_MAJOR = 4;

    public static final int REQUEST_ADD = 5;
    public static final int REQUEST_DELETE = 6;
    public static final int REQUEST_GET = 7;
    public static final int REQUEST_FILE_LIST = 8;

    // Controller -> ChunkServer
    public static final int CONTROLLER_REPORTS_REGISTRATION_STATUS = 10;
    public static final int CONTROLLER_REQUESTS_DEREGISTRATION = 11;
    public static final int CONTROLLER_REPORTS_SHUTDOWN = 12;
    public static final int CONTROLLER_REQUESTS_FUNCTIONAL_HEARTBEAT = 13;

    // ChunkServer -> Controller
    public static final int CHUNK_SERVER_REQUESTS_REGISTRATION = 20;
    public static final int CHUNK_SERVER_REPORTS_DEREGISTRATION_STATUS = 21;
    public static final int CHUNK_SERVER_SENDS_MAJOR_HEARTBEAT = 22;
    public static final int CHUNK_SERVER_SENDS_MINOR_HEARTBEAT = 23;
    public static final int CHUNK_SERVER_REPORTS_FUNCTIONAL_HEARTBEAT = 24;

    // ChunkServer -> ChunkServer
    public static final int CHUNK_SERVER_REQUESTS_REPLICATION = 30;
    public static final int CHUNK_SERVER_REPORTS_REPLICATION = 31;

    // Controller -> Client
    public static final int CONTROLLER_REPORTS_CLIENT_REQUEST_STATUS = 40;
    public static final int CONTROLLER_REPORTS_CHUNK_SERVER_METADATA = 41;
    public static final int CONTROLLER_REPORTS_FILE_METADATA = 42;

    // Client -> Controller
    public static final int CLIENT_REQUEST = 50;
    public static final int CLIENT_REQUESTS_CHUNK_SERVER_METADATA = 51;

    // Client -> ChunkServer
    public static final int CLIENT_REQUESTS_FILE_CHUNK = 60;
    public static final int CLIENT_SENDS_FILE_CHUNK = 61;

    // ChunkServer -> Client
    public static final int CHUNK_SERVER_SENDS_FILE_CHUNK = 70;
}
