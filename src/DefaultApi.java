import java.util.Optional;
import java.util.stream.Stream;

/**
 * The default implementation of the {@link Api}.
 *
 * @author Weinan Jimmy Michael
 */
public class DefaultApi implements Api {

    // the singleton instance
    private static DefaultApi instance;

    // the cached time index match from the last search
    // operation. If not null, subsequent searches should
    // start from here.
    private TimeIndex lastTimeIndex = null;

    // the cached measurements from the last read operation.
    // If not null, subsequent reads should try to deplete
    // this first.
    private Queue readBuffer = null;

    /**
     * Get the singleton of this Api.
     *
     * @return the singleton instance.
     */
    public static DefaultApi getInstance() {
        if (null == instance)
            instance = new DefaultApi();
        return instance;
    }

    @Override
    public Summary getSummary() {
        try {
            // Initialize an empty summary structure first
            Summary summary = Summary.empty();

            // Ask DataReader to read from disk, it will return
            // a stream of string based lines in the summary file.
            // We then let the empty summary object parse each
            // stat line.
            DataReader.getInstance()
                    .readFromSummary()
                    .forEach(summary::parse);

            return summary;
        } catch (Exception ex) {
            throw new RuntimeException("Read from summary failed: " + ex.getMessage());
        }
    }

    @Override
    public TimeIndex search(String fieldName, String value) throws NoMoreDataException {
        // Assuming this is the first search call of the session, start from bucket 0.
        long startBucket = 0L;

        // If this is actually not the first search call, adjust starting bucket to the
        // bucket of the cached hit since we shall start from there.
        if (lastTimeIndex != null) {
            startBucket = Hash.getInstance().getBucketIndex(lastTimeIndex.getTimeInMilliseconds());
        }

        // Do the search from the starting bucket to the last bucket available
        for (long bucketIndex = startBucket; bucketIndex <= Hash.getInstance().getLastBucketIndex(); bucketIndex++) {
            try {
                // Ask DataReader to get a stream of string based lines from the data bucket
                Stream<String> data = DataReader.getInstance().readFromData((int) bucketIndex);

                // We first create a Measurement object from each line
                // Then we filter out any measurement that happened prior to the last hit
                // In case this is the first search call of the session, we filter out nothing.
                // Then we find the first hit where the measurement matches the provided criteria.
                Optional<Measurement> result = data.map(Measurement::new)
                        .filter(measurement -> {
                            if (lastTimeIndex == null)
                                return true;
                            return measurement.getTimeInMilliseconds() > lastTimeIndex.getTimeInMilliseconds();
                        })
                        .filter(measurement -> measurement.matches(fieldName, value))
                        .findFirst();

                // If there is actually a hit, we cache the hit time index and return it.
                if (result.isPresent()) {
                    lastTimeIndex = new TimeIndex(result.get().getTimeInMilliseconds());
                    return lastTimeIndex;
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        // If we are ever here, the search must have ended and we have no hit.
        // We should end this search session and report no hit by throwing the exception.
        this.endSearch();
        throw new NoMoreDataException();
    }

    @Override
    public void endSearch() {
        this.lastTimeIndex = null;
    }

    @Override
    public Page read(TimeIndex startTime, TimeIndex endTime, String[] fields) throws NoMoreDataException {
        // Do not allow end time to be prior to start time
        if (endTime.getTimeInMilliseconds() < startTime.getTimeInMilliseconds())
            throw new NoMoreDataException();

        // Sanitize the time range, if they are beyond the time range of the data collected,
        // make them the range of the data collected.
        startTime = new TimeIndex(Math.max(startTime.getTimeInMilliseconds(), Hash.getInstance().getStartMillisecond()));
        endTime = new TimeIndex(Math.min(endTime.getTimeInMilliseconds(), Hash.getInstance().getEndMillisecond()));

        // TODO sanitize fields

        // Call inner method to perform the real read function.
        return this.readInner(startTime, endTime, fields);
    }

    /**
     * The internal read function that performs on sanitized fields.
     *
     * @param startTime sanitized start time, within data time range, lte to endTime
     * @param endTime sanitized end time, within data time range, gte to startTime
     * @param fields fields to be rendered
     *
     * @return a page of measurement
     *
     * @throws NoMoreDataException thrown when there are no more measurement that fits criteria
     */
    private Page readInner(TimeIndex startTime, TimeIndex endTime, String[] fields) throws NoMoreDataException {

        // If this is not the first read function within the session, we already
        // have results buffered from the last session, pack those into the page.
        if (this.readBuffer != null) {
            // Initialize page
            Page page = new Page(Api.PAGE_SIZE);

            // Dequeue the size of a page from the buffer and added to the page, stop
            // when buffer is empty (don't dry draw).
            for (int i = Api.PAGE_SIZE; i > 0 && this.readBuffer.getSize() > 0; i--) {
                page.add((Measurement) this.readBuffer.dequeue());
            }

            // If the buffer didn't give us any data (meaning the last read function left it
            // empty already), end this read session and tell the user there are no more
            // data by throwing the exception.
            if (page.getSize() == 0) {
                this.endRead();
                throw new NoMoreDataException();
            }

            return page;
        }

        // If this is actually the first read function in the session.
        else {
            // Initialize the buffer.
            this.readBuffer = new Queue();

            // Determine the bucket range by hashing the start and end timestamp
            long startBucket = Hash.getInstance().getBucketIndex(startTime.getTimeInMilliseconds());
            long endBucket = Hash.getInstance().getBucketIndex(endTime.getTimeInMilliseconds());

            // Traverse all the buckets in range
            for (long bucket = startBucket; bucket <= endBucket; bucket++) {
                try {
                    // Ask DataReader to get all data in the current bucket
                    Stream<String> lines = DataReader.getInstance().readFromData((int) bucket);

                    // For each line of data, create a Measurement out of it first.
                    // Next, filter out any measurement that is out of the requested time range
                    // After that, set the render fields on the measurement. This will help
                    // measurement determine how to render()
                    // Finally, add the measurement to the buffer queue.
                    lines.map(Measurement::new)
                            .filter(m -> m.getTimeInMilliseconds() <= endTime.getTimeInMilliseconds() &&
                                    m.getTimeInMilliseconds() >= startTime.getTimeInMilliseconds()
                            ).forEach(measurement -> {
                        measurement.setRenderFields(fields);
                        readBuffer.enqueue(measurement);
                    });
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }

            // Now we have all measurement that meets the criteria in the buffer queue.
            // Instead of padding the first page here, we can trick self into thinking
            // it is not the first read function call and let it fill the page. DRY!
            return this.readInner(startTime, endTime, fields);
        }
    }

    @Override
    public void endRead() {
        this.readBuffer = null;
    }
}
