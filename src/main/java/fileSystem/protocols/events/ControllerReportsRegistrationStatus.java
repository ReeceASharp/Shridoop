package fileSystem.protocols.events;

import fileSystem.protocols.Event;

import java.io.*;

import static fileSystem.protocols.Protocol.CONTROLLER_REPORTS_REGISTRATION_STATUS;

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
        ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(new BufferedOutputStream(byteOutStream));

        try {
            //write event type to decode on arrival
            dataOut.writeInt(type);

            //write response status type
            dataOut.writeInt(status);

            dataOut.flush();

            data = byteOutStream.toByteArray();

            byteOutStream.close();
            dataOut.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;

    }

    public int getStatus() {
        return status;
    }
}
