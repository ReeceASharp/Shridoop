package filesystem.interfaces;

public interface MetadataCache {
    /**
     * Caches the state for the object to a configurable location. The object is expected to handle Null cases as well,
     * with the option of either exiting, or delegating the location to an internal location already known
     * by the object. This state should be maintained so the ensuing updateFromCache can correctly pull state
     */
    void cacheInfo(String path);

    /**
     * Reads in the data saved by a prior cacheInfo() call to set the state of the object.
     */
    void updateFromCache(String path);
}
