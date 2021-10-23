package filesystem.protocol;

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps track of all intermediate changes inside a Chunk Server using records
 * Each relevant event will also create a record inside the RecordKeeper. This
 * list is then passed to from the Chunk Server to the Controller during the
 * minor heartbeats.
 */
public class RecordKeeper {
    private final ArrayList<Record> records;

    public RecordKeeper() {
        this.records = new ArrayList<>();
    }

    public synchronized boolean addRecord(Record record) {
        return records.add(record);
    }

    public void reset() {
        records.clear();
    }

    public List<Record> getRecords(boolean clear) {
        // Returning a deep-copy when the list isn't being
        if (clear) {
            ArrayList<Record> recordCache = new ArrayList<>(records);
            records.clear();
            return recordCache;
        }
        return new ArrayList<>(records);
    }

}
