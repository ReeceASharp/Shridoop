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
    - Send out major Heartbeat
        ControllerRequestsMajorHeartbeat
    - Send out minor Heartbeat
        ControllerRequestsMinorHeartbeat
    - Request current files (metadata)
        ControllerRequestsFileMetadata

# ChunkServer -> Controller
    - Request to startup
        ChunkServerSendRegistration
    - Respond to shutdown
        ChunkServerReportsDeregistrationStatus
    - Respond to major heartbeat
        ChunkServerReportsMajorHeartbeat
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
    - Request to save file in system
        ClientRequestsFileSave
    - Request to get file
        ClientRequestsFile
    - Request to delete file
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

    // Controller -> ChunkServer
    public static final int CONTROLLER_REPORTS_REGISTRATION_STATUS = 10;
    public static final int CONTROLLER_REQUESTS_DEREGISTRATION = 11;
    public static final int CONTROLLER_REPORTS_SHUTDOWN = 12;
    public static final int CONTROLLER_REQUESTS_MAJOR_HEARTBEAT = 13;
    public static final int CONTROLLER_REQUESTS_MINOR_HEARTBEAT = 14;
    public static final int CONTROLLER_REQUESTS_FILE_METADATA = 15;

    // ChunkServer -> Controller
    public static final int CHUNK_SERVER_REQUESTS_REGISTRATION = 20;
    public static final int CHUNK_SERVER_REPORTS_DEREGISTRATION_STATUS = 21;
    public static final int CHUNK_SERVER_REPORTS_MAJOR_HEARTBEAT = 22;
    public static final int CHUNK_SERVER_REPORTS_MINOR_HEARTBEAT = 23;
    public static final int CHUNK_SERVER_REPORTS_FILE_CHUNK_METADATA = 24;

    // ChunkServer -> ChunkServer
    public static final int CHUNK_SERVER_REQUESTS_REPLICATION = 30;
    public static final int CHUNK_SERVER_REPORTS_REPLICATION = 31;

    // Controller -> Client
    public static final int CONTROLLER_REPORTS_FILE_CHUNK_ADD_DESTINATION = 40;
    public static final int CONTROLLER_REPORTS_FILE_CHUNK_REQUEST_LOCATION = 41;
    public static final int CONTROLLER_REPORTS_FILE_DELETE_STATUS = 42;
    public static final int CONTROLLER_REPORTS_CHUNK_SERVER_METADATA = 43;
    public static final int CONTROLLER_REPORTS_FILE_METADATA = 44;

    // Client -> Controller
    public static final int CLIENT_REQUESTS_FILE_SAVE = 50;
    public static final int CLIENT_REQUESTS_FILE = 51;
    public static final int CLIENT_REQUESTS_FILE_DELETE = 52;
    public static final int CLIENT_REQUESTS_CHUNK_SERVER_METADATA = 53;
    public static final int CLIENT_REQUESTS_FILE_METADATA = 54;

    // Client -> ChunkServer
    public static final int CLIENT_REQUESTS_FILE_CHUNK = 60;

    // ChunkServer -> Client
    public static final int CHUNK_SERVER_SENDS_FILE_CHUNK = 70;
}
