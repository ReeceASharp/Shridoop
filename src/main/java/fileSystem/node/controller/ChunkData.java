package fileSystem.node.controller;

import java.net.InetAddress;

public class ChunkData {
    public final String name;
    public final String address;
    public final int port;

    public ChunkData(String name, String address, int port) {
        this.name = name;
        this.address = address;
        this.port = port;
    }

    //TODO: Keep track of the current chunks each server contains
}
