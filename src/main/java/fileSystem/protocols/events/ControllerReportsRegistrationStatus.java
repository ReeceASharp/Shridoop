package fileSystem.protocols.events;

import fileSystem.protocols.Event;
import fileSystem.protocols.OutputWrapper;

import java.io.*;

import static fileSystem.protocols.Protocol.CONTROLLER_REPORTS_REGISTRATION_STATUS;

/**
 * Sent from the Controller to the ChunkServer acknowledging the server is setup and ready to go
 */
public class ControllerReportsRegistrationStatus implements Event {
    static final int type = CONTROLLER_REPORTS_REGISTRATION_STATUS;

    final int status;

    public ControllerReportsRegistrationStatus(int status) {
        this.status = status;
    }

    public ControllerReportsRegistrationStatus(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream byteInStream = new ByteArrayInputStream(marshalledBytes);
        DataInputStream dataIn = new DataInputStream(new BufferedInputStream(byteInStream));

        //disregard, the buffer starts at the beginning of the byte array
        dataIn.readInt();

        //read in the status of the message, handle appropriately
        this.status = dataIn.readInt();

        //close wrapper streams
        byteInStream.close();
        dataIn.close();
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

            data = wrapper.flushAndGetBytes();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;

    }

    public int getStatus() {
        return status;
    }
}
