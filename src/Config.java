import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Configuration for the application.
 *
 * @author Weinan Qiu
 */
public class Config {

    public static final String KEY_PORT = "port";
    public static final String KEY_OUTPUT_DIR = "output_dir";
    public static final String KEY_SCHEMA = "schema";

    private static Config instance;
    private final Map source;

    public static void init(String configPath) throws Exception {
        Stream<String> stream = Files.lines(Paths.get(configPath));
        Map<String, Object> localSource = new HashMap<>();
        stream.forEach(s -> {
            String[] kv = s.split("=");
            if (2 != kv.length)
                throw new RuntimeException("Bad config line format: " + s);
            else {
                switch (kv[0]) {
                    case KEY_PORT:
                        localSource.put(KEY_PORT, Integer.parseInt(kv[1]));
                        break;

                    case KEY_SCHEMA:
                        LinkedList list = new LinkedList();
                        Arrays.stream(kv[1].split(";")).forEach(s1 -> {
                            String[] namesAndTypes = s1.split(":");
                            list.add(new Schema(namesAndTypes[0], namesAndTypes[1]), list.getSize());
                        });
                        localSource.put(KEY_SCHEMA, list);
                        break;

                    case KEY_OUTPUT_DIR:
                        if (Files.notExists(Paths.get(kv[1])))
                            throw new RuntimeException("Output directory does not exists: " + kv[1]);
                        localSource.put(KEY_OUTPUT_DIR, kv[1]);
                        break;

                    default:
                        throw new RuntimeException("Unrecognized config key: " + kv[0]);
                }
            }
        });
        instance = new Config(localSource);
    }

    private Config(final Map source) {
        if (!source.containsKey(KEY_PORT))
            throw new RuntimeException("Config object missing key '" + KEY_PORT + "'");
        else if (!source.containsKey(KEY_SCHEMA))
            throw new RuntimeException("Config object missing key '" + KEY_SCHEMA + "'");
        else if (!source.containsKey(KEY_OUTPUT_DIR))
            throw new RuntimeException("Config object missing key '" + KEY_OUTPUT_DIR + "'");
        else
            this.source = source;
    }

    public Map getSource() {
        return source;
    }

    public Integer getPort() {
        return (Integer) this.source.get(KEY_PORT);
    }

    public String getOutputDirectory() {
        return (String) this.source.get(KEY_OUTPUT_DIR);
    }

    public LinkedList getSchema() {
        return (LinkedList) this.source.get(KEY_SCHEMA);
    }

    public static Config getInstance() {
        return instance;
    }

    public static void setInstance(Config instance) {
        Config.instance = instance;
    }

    public static class Schema {

        public static final String TYPE_INT = "integer";
        public static final String TYPE_FLOAT = "float";

        private final String name;
        private final String type;

        public Schema(String name, String type) {
            this.name = name;
            this.type = type;

            switch (this.type) {
                case TYPE_INT:
                    break;

                case TYPE_FLOAT:
                    break;

                default:
                    throw new RuntimeException("Illegal schema member type: " + this.type);
            }
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }
    }
}
