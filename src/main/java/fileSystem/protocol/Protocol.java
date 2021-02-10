package fileSystem.protocol;

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
    public static final int CONTROLLER_REPORTS_FILE_LIST = 40;
    public static final int CONTROLLER_REPORTS_CHUNK_ADD_LIST = 41;
    public static final int CONTROLLER_REPORTS_CHUNK_GET_LIST = 42;
    public static final int CONTROLLER_REPORTS_FILE_REMOVE_STATUS = 43;
    public static final int CONTROLLER_REPORTS_CHUNK_SERVER_METADATA = 44;


    // Client -> Controller
    public static final int CLIENT_REQUESTS_FILE_ADD = 50;
    public static final int CLIENT_REQUESTS_FILE_DELETE = 51;
    public static final int CLIENT_REQUESTS_FILE = 52;
    public static final int CLIENT_REQUESTS_FILE_LIST = 53;
    public static final int CLIENT_REQUESTS_CHUNK_SERVER_METADATA = 54;

    // Client -> ChunkServer
    public static final int CLIENT_REQUESTS_FILE_CHUNK = 60;
    public static final int NODE_SENDS_FILE_CHUNK = 61;

    // ChunkServer -> Client
    public static final int CHUNK_SERVER_SENDS_FILE_CHUNK = 70;

}
