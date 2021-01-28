package fileSystem.protocols.events;

import fileSystem.protocols.Event;

import java.io.*;

import static fileSystem.protocols.Protocol.CONTROLLER_REPORTS_SHUTDOWN;

public class ControllerReportsShutdown implements Event {

    static final int type = CONTROLLER_REPORTS_SHUTDOWN;

    public ControllerReportsShutdown() {
    }

    public ControllerReportsShutdown(byte[] marshalledBytes) throws IOException {
        //create a wrapper around the bytes to leverage some methods to easily extract values
        ByteArrayInputStream byteInStream = new ByteArrayInputStream(marshalledBytes);
        DataInputStream dataIn = new DataInputStream(new BufferedInputStream(byteInStream));

        //disregard, the buffer starts at the beginning of the byte array
        dataIn.readInt();

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

            //ensure all is written before the buffer is converted to a byte array
            dataOut.flush();

            data = byteOutStream.toByteArray();

            byteOutStream.close();
            dataOut.close();
        } catch (IOException e) {
            //failed for some reason
            e.printStackTrace();
        }

        return data;
    }
}
