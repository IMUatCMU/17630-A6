/**
 * Wrapper for a time stamp (time index). We use millisecond as the unit
 * of time so we can always deal with whole numbers even in the case of
 * a fractional second.
 *
 * @author Weinan Jimmy Michael
 */
public class TimeIndex implements Renderable {

    private final long timeInMilliseconds;
    private static long HOUR_MILLISECONDS = 60 * 60 * 1000L;
    private static long MINUTE_MILLISECONDS = 60 * 1000L;
    private static long SECOND_MILLISECONDS = 1000L;
    private static float SECOND_MILLISECONDS_F = 1000.0F;

    /**
     * Create time index from hour, minute and integer second data
     * @param hour
     * @param minute
     * @param second
     * @return
     */
    public static TimeIndex of(int hour, int minute, int second) {
        Long secondsAsMillisecond = (long) second * SECOND_MILLISECONDS;
        Long minuteAsMillisecond = (long) minute * MINUTE_MILLISECONDS;
        Long hourAsMillisecond = (long) hour * HOUR_MILLISECONDS;
        return new TimeIndex(hourAsMillisecond + minuteAsMillisecond + secondsAsMillisecond);
    }

    /**
     * Create time index from hour, minute and float (fractional) second data
     * @param hour
     * @param minute
     * @param second
     * @return
     */
    public static TimeIndex of(int hour, int minute, float second) {
        Long secondsAsMillisecond = (long) (second * SECOND_MILLISECONDS_F);
        Long minuteAsMillisecond = (long) minute * MINUTE_MILLISECONDS;
        Long hourAsMillisecond = (long) hour * HOUR_MILLISECONDS;
        return new TimeIndex(hourAsMillisecond + minuteAsMillisecond + secondsAsMillisecond);
    }

    public TimeIndex(long timeInMilliseconds) {
        this.timeInMilliseconds = timeInMilliseconds;
    }

    public long getTimeInMilliseconds() {
        return timeInMilliseconds;
    }

    /**
     * User facing representation of the time index. We render accordingly
     * for integer second and float second.
     *
     * @return
     */
    @Override
    public String render() {
        long hours = this.timeInMilliseconds / HOUR_MILLISECONDS;
        long minutes = (this.timeInMilliseconds - hours * HOUR_MILLISECONDS) / MINUTE_MILLISECONDS;
        long secondsInMilliseconds = this.timeInMilliseconds - hours * HOUR_MILLISECONDS - minutes * MINUTE_MILLISECONDS;
        if (secondsInMilliseconds % SECOND_MILLISECONDS == 0) {
            return String.format("hour:%d minute:%d second:%d",
                    hours, minutes, secondsInMilliseconds / SECOND_MILLISECONDS);
        } else {
            return String.format("hour:%d minute:%d second:%f",
                    hours, minutes, (float) secondsInMilliseconds / (float) SECOND_MILLISECONDS);
        }
    }
}
