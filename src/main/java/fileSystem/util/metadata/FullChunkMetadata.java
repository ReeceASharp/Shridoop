package fileSystem.util.metadata;

import fileSystem.util.*;

import java.util.*;
import fileSystem.util.Properties;


public class FullChunkMetadata extends LiteChunkMetadata {
    private static final int SLICE_SIZE = Integer.parseInt(Properties.get("SLICE_SIZE"));

    private final ArrayList<SliceMetadata> sliceList;


    public FullChunkMetadata(int chunkNumber,
                             int chunkSize,
                             String chunkHash,
                             ArrayList<Pair<String, Integer>> serversHoldingChunk,
                             byte[] chunkData) {
        super(chunkNumber, chunkSize, chunkHash, serversHoldingChunk);
        this.sliceList = generateSliceMetadata(chunkData);
    }


    private static ArrayList<SliceMetadata> generateSliceMetadata(byte[] chunkData) {
        //TODO: Refactor file chunking logic to generalize, as slicing is exactly the same methodology
        return null;
    }


    private static class SliceMetadata {
        public final int sliceNumber;
        public final int sliceSize;
        public final String checksum;

        public SliceMetadata(int sliceNumber, int sliceSize, String checksum) {
            this.sliceNumber = sliceNumber;
            this.sliceSize = sliceSize;
            this.checksum = checksum;
        }
    }
}
