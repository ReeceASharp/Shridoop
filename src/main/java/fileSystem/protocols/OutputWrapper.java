package fileSystem.protocols;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 */
public class OutputWrapper {
    public DataOutputStream dataOut;
    private final ByteArrayOutputStream byteOutStream;

    /**
     * Constructs an a wrapper for byteOutputStreams that handles both the initialize, and the cleanup
     * @param eventType  the Event type, is automatically written into the buffer as that's what the receiving end
     *                   expects by default
     * @throws IOException
     */
    public OutputWrapper(int eventType) throws IOException {
        this.byteOutStream = new ByteArrayOutputStream();
        this.dataOut = new DataOutputStream(new BufferedOutputStream(byteOutStream));

        //write to type to the buffer as that's always required
        dataOut.writeInt(eventType);
    }

    public DataOutputStream getDataOut() {
        return dataOut;
    }

    /**
     * Cleans up the wrapper by flushing the current data in the buffer, and returning the data in byte form
     * @return  written data in byte form (serialized)
     * @throws IOException
     */
    public byte[] flushAndGetBytes() throws IOException {

        dataOut.flush();

        byte[] marshalledByes = byteOutStream.toByteArray();

        byteOutStream.close();
        dataOut.close();

        return marshalledByes;

    }


}
