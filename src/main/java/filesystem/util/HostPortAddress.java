package filesystem.util;

import java.io.Serializable;

public class HostPortAddress implements Serializable {
    private final String hostname;
    private final int port;

    public HostPortAddress(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public final boolean equals(Object obj) {
        if (hostname != null && obj instanceof HostPortAddress) {
            HostPortAddress other = (HostPortAddress) obj;
            return hostname.equalsIgnoreCase(other.getHostname()) && this.port == other.getPort();
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "HostPortAddress{" +
                "hostname='" + hostname + '\'' +
                ", port=" + port +
                '}';
    }
}