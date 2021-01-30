package fileSystem.protocols.events;

import fileSystem.protocols.Event;
import fileSystem.protocols.InputWrapper;
import fileSystem.protocols.OutputWrapper;

import java.io.*;

import static fileSystem.protocols.Protocol.CHUNK_SERVER_REQUESTS_REGISTRATION;

/**
 * Sent from the Chunk Server to the Controller when it has finished setting up
 */
public class ChunkServerRequestsRegistration implements Event {
    static final int type = CHUNK_SERVER_REQUESTS_REGISTRATION;

    private final String originatingIP;
    private final int originatingPort;
    private final String name;

    public ChunkServerRequestsRegistration(String ip, int port, String name) {
        originatingIP = ip;
        originatingPort = port;
        this.name = name;
    }

    public ChunkServerRequestsRegistration(byte[] marshalledBytes) throws IOException {
        //create a wrapper around the bytes to leverage some methods to easily extract values
        InputWrapper wrapper = new InputWrapper(marshalledBytes);
        DataInputStream dataIn = wrapper.getDataIn();

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

            //ensure all is written before the buffer is converted to a byte array
            data = wrapper.flushAndGetBytes();
        } catch (IOException e) {
            //failed for some reason
            e.printStackTrace();
        }

        return data;
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
