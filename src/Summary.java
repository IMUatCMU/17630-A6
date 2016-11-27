import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Holding structure for the summary of data.
 *
 * @author Weinan Jimmy Michael
 */
public class Summary implements Renderable {

    // disk writing key for bytes count
    public static final String KEY_COUNT = "count";

    // disk writing key for start timestamp
    public static final String KEY_START = "start";

    // disk writing key for end timestamp
    public static final String KEY_END = "end";

    // disk writing key for schema
    public static final String KEY_SCHEMA = "schema";

    // bytes count
    private int bytesCount;

    // start timestamp
    private long startTimestamp;

    // end timestamp
    private long endTimestamp;

    // a list of Config.Schema
    private LinkedList schema;

    /**
     * Create an empty summary structure.
     *
     * @return
     */
    public static Summary empty() {
        return new Summary();
    }

    private Summary() {
        this.bytesCount = 0;
        this.startTimestamp = 0L;
        this.endTimestamp = 0L;
        this.schema = new LinkedList();
    }

    /**
     * Fill in the stat, fetched from the summary file on disk.
     *
     * @param raw
     */
    public void parse(String raw) {
        String[] kv = raw.split("=");
        switch (kv[0]) {
            case KEY_COUNT:
                this.setBytesCount(Integer.parseInt(kv[1]));
                break;

            case KEY_START:
                this.setStartTimestamp(Long.parseLong(kv[1]));
                break;

            case KEY_END:
                this.setEndTimestamp(Long.parseLong(kv[1]));
                break;

            case KEY_SCHEMA:
                LinkedList list = new LinkedList();
                Arrays.stream(kv[1].split(";")).forEach(s -> {
                    String[] split = s.split(":");
                    Config.Schema schema = null;
                    if (split.length == 2) {
                        schema = new Config.Schema(split[0], split[1], null);
                    } else if (split.length == 3) {
                        schema = new Config.Schema(split[0], split[1], split[2]);
                    }
                    list.add(schema, list.getSize());
                });
                this.setSchema(list);
                break;

            default:
                throw new IllegalArgumentException("Unrecognized raw data: " + raw);
        }
    }

    /**
     * Increase the byte count. Frequently called when receiving data from server.
     *
     * @param delta
     */
    public void increaseBytesCount(int delta) {
        this.bytesCount += delta;
    }

    public int getBytesCount() {
        return bytesCount;
    }

    public void setBytesCount(int bytesCount) {
        this.bytesCount = bytesCount;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public LinkedList getSchema() {
        return schema;
    }

    public void setSchema(LinkedList schema) {
        this.schema = schema;
    }

    /**
     * Disk representation of this summary data. This results are written
     * directly to disk.
     *
     * @return
     */
    public String toString() {
        Stream.Builder<String> schemaSB = Stream.builder();
        this.schema.forEach(o -> {
            Config.Schema schema = (Config.Schema) o;
            if (schema.getUnit() != null && schema.getUnit().length() > 0) {
                schemaSB.add(schema.getName() + ":" + schema.getType() + ":" + schema.getUnit());
            } else {
                schemaSB.add(schema.getName() + ":" + schema.getType());
            }
        });

        StringBuilder sb = new StringBuilder();
        sb.append(KEY_COUNT);
        sb.append("=");
        sb.append(this.bytesCount);
        sb.append("\n");

        sb.append(KEY_START);
        sb.append("=");
        sb.append(this.startTimestamp);
        sb.append("\n");

        sb.append(KEY_END);
        sb.append("=");
        sb.append(this.endTimestamp);
        sb.append("\n");

        sb.append(KEY_SCHEMA);
        sb.append("=");
        sb.append(schemaSB.build().collect(Collectors.joining(";")));
        sb.append("\n");

        return sb.toString();
    }

    /**
     * User facing representation of this summary data.
     *
     * @return
     */
    @Override
    public String render() {
        Stream.Builder<String> schemaSB = Stream.builder();
        this.schema.forEach(o -> {
            Config.Schema schema = (Config.Schema) o;
            if (schema.getUnit() != null && schema.getUnit().length() > 0) {
                schemaSB.add(schema.getName() + " (" + schema.getType() + ") - " + schema.getUnit());
            } else {
                schemaSB.add(schema.getName() + " (" + schema.getType() + ")");
            }
        });

        StringBuilder sb = new StringBuilder();
        sb.append("total_bytes:");
        sb.append("\n");
        sb.append(this.bytesCount);
        sb.append("\n\n");

        sb.append("start_time:");
        sb.append("\n");
        sb.append(new TimeIndex(this.startTimestamp).render());
        sb.append("\n\n");

        sb.append("end_time:");
        sb.append("\n");
        sb.append(new TimeIndex(this.endTimestamp).render());
        sb.append("\n\n");

        sb.append("fields:");
        sb.append("\n");
        sb.append(schemaSB.build().collect(Collectors.joining("\n")));
        sb.append("\n\n");

        return sb.toString();
    }
}
