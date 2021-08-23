package fileSystem.util;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Keeps track of the contact information for each of the servers, organized by chunk
 * Used by both GET and ADD protocols
 * Note: serversToContact combines the host and port separated by a ':', which is an illegal
 * character for the hostname. Ref: https://man7.org/linux/man-pages/man7/hostname.7.html
 */
public class ContactList implements Serializable {
    private final int chunkNumber;
    private final ArrayList<String> serversToContact;

    public ContactList(int chunkNumber, ArrayList<String> serversToContact) {
        this.chunkNumber = chunkNumber;
        this.serversToContact = serversToContact;
    }

    public int getChunkNumber() {
        return chunkNumber;
    }

    @Override
    public String toString() {
        return "ContactList{" +
                "chunkNumber=" + chunkNumber +
                ", serversToContact=" + serversToContact +
                '}';
    }

    /**
     *
     * Note: The string needs to be separated, as it's in the form of "[HOST]:[PORT]"
     * @return Returns a list of servers that contain/will contain this chunk
     */
    public ArrayList<String> getServersToContact() {
        return serversToContact;
    }

}
