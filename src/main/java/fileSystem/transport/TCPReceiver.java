package fileSystem.transport;

import fileSystem.node.Node;

import java.net.Socket;

/*
This packages a thread around the socket to grab the information being received, then handles the request
 */
public class TCPReceiver implements Runnable {

    private final Node node;
    private final Socket clientSocket;

    public TCPReceiver(Node node, Socket clientSocket) {
        this.node = node;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {


        //get the dataInput

        //while the connection is open

            //read in the data

        //package the data, and spawn a thread to handle it

    }
}
