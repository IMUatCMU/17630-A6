import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by davidiamyou on 2016-11-22.
 */
public class Measurement {

    public static final String HOUR = "hour";
    public static final String MINUTE = "minute";
    public static final String SECOND = "second";

    private final Map<String, Object> data;

    public Measurement(Map<String, Object> data) {
        if (!data.containsKey(HOUR))
            throw new RuntimeException("Measurement missing key: " + HOUR);
        else if (!data.containsKey(MINUTE))
            throw new RuntimeException("Measurement missing key: " + MINUTE);
        else if (!data.containsKey(SECOND))
            throw new RuntimeException("Measurement missing key: " + SECOND);
        else
            this.data = data;
    }

    public Long getTimeInMilliseconds() {
        Integer second = (Integer) data.get(SECOND);
        Long secondsAsMillisecond = (long) Math.round(second * 1000.0f);

        Integer minute = (Integer) data.get(MINUTE);
        Long minuteAsMillisecond = (long) (minute * 60 * 1000);

        Integer hour = (Integer) data.get(HOUR);
        Long hourAsMillisecond = (long) (hour * 60 * 60 * 1000);

        return hourAsMillisecond + minuteAsMillisecond + secondsAsMillisecond;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public String toString() {
        return this.data.entrySet()
                .stream()
                .map(entry -> entry.getKey() + ":" + entry.getValue().toString())
                .collect(Collectors.joining(";"));
    }
}
