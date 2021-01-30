package fileSystem.protocols.events;

import fileSystem.protocols.Event;
import fileSystem.protocols.InputWrapper;
import fileSystem.protocols.OutputWrapper;

import java.io.*;

import static fileSystem.protocols.Protocol.CHUNK_SERVER_REPORTS_DEREGISTRATION_STATUS;

/**
 * Sent from the ChunkServer to the Controller confirming its request for deregistration, and by
 * extension shutdown
 */
public class ChunkServerReportsDeregistrationStatus implements Event {
    static final int type = CHUNK_SERVER_REPORTS_DEREGISTRATION_STATUS;

    private final int status;
    private final String originatingIP;
    private final int originatingPort;
    private final String name;

    public ChunkServerReportsDeregistrationStatus(int status, String ip, int port, String name) {
        this.status = status;
        originatingIP = ip;
        originatingPort = port;
        this.name = name;
    }

    public ChunkServerReportsDeregistrationStatus(byte[] marshalledBytes) throws IOException {
        InputWrapper wrapper = new InputWrapper(marshalledBytes);
        DataInputStream dataIn = wrapper.getDataIn();

        //read in the status of the message, handle appropriately
        this.status = dataIn.readInt();

        //retrieve IP address
        int ipLength = dataIn.readInt();
        byte[] ipBytes = new byte[ipLength];
        dataIn.readFully(ipBytes);
        originatingIP = new String(ipBytes);

        //retrieve port
        originatingPort = dataIn.readInt();

        //retrieve name
        int nameLength = dataIn.readInt();
        byte[] nameBytes = new byte[nameLength];
        dataIn.readFully(nameBytes);
        name = new String(nameBytes);


        //close wrapper streams
        wrapper.close();
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public byte[] getBytes() {

        byte[] data = null;
        //create a wrapper around the bytes to leverage some methods to easily extract values

        try {
            OutputWrapper wrapper = new OutputWrapper(type);
            DataOutputStream dataOut = wrapper.getDataOut();

            //write response status type
            dataOut.writeInt(status);

            //write IP address
            //Note: this has to be done as a thread is sending this on a random port, not the main
            //one the TCPServer is listening on for the ChunkServer
            byte[] ipBytes = originatingIP.getBytes();
            dataOut.writeInt(ipBytes.length);
            dataOut.write(ipBytes);

            //write port
            dataOut.writeInt(originatingPort);

            byte[] nameBytes = name.getBytes();
            dataOut.writeInt(nameBytes.length);
            dataOut.write(nameBytes);

            data = wrapper.flushAndGetBytes();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    public int getStatus() {
        return status;
    }

    public String getIP() {
        return originatingIP;
    }

    public int getPort() {
        return originatingPort;
    }

    public String getName() {
        return name;
    }
}
