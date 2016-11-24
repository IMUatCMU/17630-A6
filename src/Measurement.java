import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Container for the frame of data.
 *
 * @author Weinan Jimmy Michael
 */
public class Measurement implements Renderable {

    // Required schema key for hour
    public static final String HOUR = "hour";

    // Required schema key for minute
    public static final String MINUTE = "minute";

    // Required schema key for second
    public static final String SECOND = "second";

    // Internal storage for the data
    private final Map<String, Object> data;

    // Ephemeral array to assist rendering, tells render()
    // method which fields from data to include in rendering.
    private String[] renderFields;

    /**
     * Constructor from a already parsed data dictionary. Makes sure
     * all the required keys (i.e. hour, minute, second) is included.
     *
     * @param data
     */
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

    /**
     * Constructor from a line of data fetched from disk.
     *
     * @param raw
     */
    public Measurement(String raw) {
        this.data = new HashMap<>();
        Arrays.stream(raw.split(";")).forEach(s -> {
            String name = s.split(":")[0];
            String value = s.split(":")[1];
            String type = ((Config.Schema) Config.getInstance().getSchema().find(o -> {
                Config.Schema each = (Config.Schema) o;
                return each.getName().equals(name);
            })).getType();
            switch (type) {
                case Config.Schema.TYPE_INT:
                    this.data.put(name, Integer.parseInt(value));
                    break;

                case Config.Schema.TYPE_FLOAT:
                    this.data.put(name, Float.parseFloat(value));
                    break;

                default:
                    throw new IllegalArgumentException("Unrecognized type: " + type);
            }
        });
    }

    /**
     * Get the timestamp for this measurement frame. It accounts for
     * fractional second where second might be float.
     *
     * @return
     */
    public Long getTimeInMilliseconds() {
        if (Integer.class.isInstance(data.get(SECOND))) {
            return TimeIndex.of(
                    (Integer) data.get(HOUR),
                    (Integer) data.get(MINUTE),
                    (Integer) data.get(SECOND)
            ).getTimeInMilliseconds();
        } else if (Float.class.isInstance(data.get(SECOND))) {
            return TimeIndex.of(
                    (Integer) data.get(HOUR),
                    (Integer) data.get(MINUTE),
                    (Float) data.get(SECOND)
            ).getTimeInMilliseconds();
        } else {
            throw new RuntimeException("Unrecognized second type: " + data.get(SECOND).getClass().getName());
        }
    }

    public Map<String, Object> getData() {
        return data;
    }

    /**
     * Return the disk form of this frame of data. This is directly
     * used to write to disk. Key-value are separated by semi-colon.
     * Fields are separated by colon.
     *
     * @return
     */
    public String toString() {
        return this.data.entrySet()
                .stream()
                .map(entry -> entry.getKey() + ":" + entry.getValue().toString())
                .collect(Collectors.joining(";"));
    }

    public String[] getRenderFields() {
        return renderFields;
    }

    public void setRenderFields(String[] renderFields) {
        this.renderFields = renderFields;
    }

    /**
     * Determine whether the given name and value matches the current frame.
     * The criteria is that whether the data frame contains the name as key
     * (whether it has this field) and whether the string representation of the
     * value matches.
     *
     * @param name
     * @param value
     * @return
     */
    public boolean matches(String name, Object value) {
        if (!this.data.containsKey(name))
            return false;
        return this.data.get(name).toString().equals(value);
    }

    /**
     * User facing representation of this frame of data.
     *
     * @return
     */
    @Override
    public String render() {
        return Arrays.stream(this.renderFields)
                .filter(this.data::containsKey)
                .map(field -> field + "=" + this.data.get(field))
                .collect(Collectors.joining(" "))
                .trim();
    }
}
