/**
 * This is created to hash time measurements into the
 * the corresponding data buckets. It constructs the correlation
 * between a time index and a data bucket index. It determines where
 * to write and read a specified frame of data.
 *
 * @author Weinan Jimmy Michael
 */
public class Hash {

    // Let's have 10 data buckets. Ideally this is configurable, but
    // for the purpose of A6, this seems fair for ~300 frames of data
    // with each data bucket containing ~30 frames of data.
    private static long NUM_BUCKETS = 10L;

    // Singleton instance
    private static Hash instance = null;

    // Start timestamp of the collected data frames
    private Long startMillisecond;

    // End timestamp of the collected data frames
    private Long endMillisecond;

    /**
     * Private constructor. Default timestamp to the range of long
     * so they start by containing everything.
     */
    private Hash() {
        this.startMillisecond = Long.MIN_VALUE;
        this.endMillisecond = Long.MAX_VALUE;
    }

    public static Hash getInstance() {
        if (null == instance)
            instance = new Hash();
        return instance;
    }

    /**
     * Express helper to return the first bucket index, which is fixed at 0
     * @return
     */
    public long getFirstBucketIndex() {
        return 0L;
    }

    /**
     * Express helper to return the last bucket index, which is 10-1=9 since
     * bucket index is 0 based.
     *
     * @return
     */
    public long getLastBucketIndex() {
        return NUM_BUCKETS - 1L;
    }

    /**
     * Main hash method. Transform the timestamp to a specified bucket index.
     * This index is determined by the size of the interval.
     *
     * @param timeInMillisecond
     * @return
     */
    public long getBucketIndex(Long timeInMillisecond) {
        return (timeInMillisecond - this.startMillisecond) / getInterval();
    }

    /**
     * Helper to get the timestamp interval between buckets. We add 1 to take
     * care of fraction problems.
     *
     * @return
     */
    public long getInterval() {
        return 1 + (this.endMillisecond - this.startMillisecond) / NUM_BUCKETS;
    }

    public Long getStartMillisecond() {
        return startMillisecond;
    }

    public void setStartMillisecond(Long startMillisecond) {
        this.startMillisecond = startMillisecond;
    }

    public Long getEndMillisecond() {
        return endMillisecond;
    }

    public void setEndMillisecond(Long endMillisecond) {
        this.endMillisecond = endMillisecond;
    }
}
