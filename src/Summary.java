import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Summary {

    public static final String KEY_COUNT = "count";
    public static final String KEY_START = "start";
    public static final String KEY_END = "end";
    public static final String KEY_SCHEMA = "schema";

    private int recordCount;
    private long startTimestamp;
    private long endTimestamp;
    private LinkedList schema;

    public static Summary empty() {
        return new Summary();
    }

    private Summary() {
        this.recordCount = 0;
        this.startTimestamp = 0L;
        this.endTimestamp = 0L;
        this.schema = new LinkedList();
    }

    public void increaseRecordCount() {
        this.recordCount++;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
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

    public String toString() {
        Stream.Builder<String> schemaSB = Stream.builder();
        this.schema.forEach(o -> {
            Config.Schema schema = (Config.Schema) o;
            schemaSB.add(schema.getName() + ":" + schema.getType());
        });

        StringBuilder sb = new StringBuilder();
        sb.append(KEY_COUNT);
        sb.append(":");
        sb.append(this.recordCount);
        sb.append("\n");

        sb.append(KEY_START);
        sb.append(":");
        sb.append(this.startTimestamp);
        sb.append("\n");

        sb.append(KEY_END);
        sb.append(":");
        sb.append(this.endTimestamp);
        sb.append("\n");

        sb.append(KEY_SCHEMA);
        sb.append(":");
        sb.append(schemaSB.build().collect(Collectors.joining(";")));
        sb.append("\n");

        return sb.toString();
    }
}
