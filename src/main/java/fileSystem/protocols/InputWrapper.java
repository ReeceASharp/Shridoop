package fileSystem.protocols;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Attempts to simplify the input process by only requiring an event read the information that pertains to it, the setup
 * and closing is handled by the wrapper
 */
public class InputWrapper {
    private final ByteArrayInputStream byteInStream;
    private final DataInputStream dataIn;

    public InputWrapper(byte[] marshalledBytes) throws IOException {
        byteInStream = new ByteArrayInputStream(marshalledBytes);
        dataIn = new DataInputStream(new BufferedInputStream(byteInStream));

        //disregard leading event type information, only used by EventFactory
        dataIn.readInt();
    }

    public DataInputStream getDataIn() {
        return dataIn;
    }

    public void close() throws IOException {
        byteInStream.close();
        dataIn.close();
    }
}

