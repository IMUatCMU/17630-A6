/**
 * Application Programming Interface for A6. Everything this API
 * returns as an implementation {@link Renderable}, which means
 * use can invoke {@link Renderable#render()} to get a string
 * representation of the return result to print to console.
 *
 * @author Weinan, Jimmy, Michael
 */
public interface Api {

    // Default page/buffer size for "Read Data" function.
    int PAGE_SIZE = 10;

    /**
     * Get the default implementation this API.
     * @return the default implementation {@link DefaultApi}
     */
    static Api defaultApi() {
        return DefaultApi.getInstance();
    }

    /**
     * "Summary Data" function. The {@link Summary} object returned mainly keeps track
     * of four things:
     * - {@link Summary#bytesCount} the number of bytes received from the server.
     * - {@link Summary#startTimestamp} the millisecond-based timestamp of the start time.
     * - {@link Summary#endTimestamp} the millisecond-based timestamp of the end time.
     * - {@link Summary#schema} a {@link LinkedList} of {@link Config.Schema} which describes all the fields received by the client.
     *
     * From the above fields, we can answer:
     * - what measurements are in the file, their units (type)
     * - how much data has been collected (in bytes)
     * - time span of the data
     */
    Summary getSummary();

    /**
     * Search for the first (when called first time) or next (when called after first
     * time) occurrence that some field has some value. Subsequent searches after the
     * first search are based on the previous search. To start from the beginning again,
     * call {@link #endSearch()} and call {@link #search(String, String)} again. If the
     * search didn't find any matches in the rest of the files, it will throw {@link NoMoreDataException}.
     *
     * @param fieldName name of the field to search
     * @param value expected value of that field in search
     * @return time index when occurrence happens
     * @throws NoMoreDataException
     */
    TimeIndex search(String fieldName, String value) throws NoMoreDataException;

    /**
     * Cancel any existing search session (so you can start from the beginning).
     */
    void endSearch();

    /**
     * Read all the data that is packable into a page. Users can call this method again
     * to see if there's more data. If the user calls this method again after all data
     * has been returned, it will throw {@link NoMoreDataException}
     *
     * @param startTime timestamp for the start time
     * @param endTime timestamp for the end time, if less than start time, will immediately throw {@link NoMoreDataException}
     * @param fields list of fields to return
     *
     * @return one page of sensor data
     */
    Page read(TimeIndex startTime, TimeIndex endTime, String[] fields) throws NoMoreDataException;

    /**
     * Cancel any read session (so you can start from the beginning)
     */
    void endRead();

    /**
     * Exception thrown when the api did not find any more
     * occurrence by the search criteria
     */
    class NoMoreDataException extends Exception {}
}
