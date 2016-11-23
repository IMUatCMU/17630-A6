/**
 * This is created to hash time measurements into the
 * the corresponding data buckets.
 *
 * @author Weinan Qiu
 */
public class Hash {

    private static long NUM_BUCKETS = 10L;
    private static Hash instance = null;

    private Long startMillisecond;
    private Long endMillisecond;

    private Hash() {
        this.startMillisecond = Long.MIN_VALUE;
        this.endMillisecond = Long.MAX_VALUE;
    }

    public static Hash getInstance() {
        if (null == instance)
            instance = new Hash();
        return instance;
    }

    public long getBucketIndex(Long timeInMillisecond) {
        return (timeInMillisecond - this.startMillisecond) / getInterval();
    }

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
