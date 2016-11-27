import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Configuration for the application. This class reads the configuration
 * file provided, parses it and keeps track of the configuration throughout
 * the lifecycle of the program
 *
 * @author Weinan Jimmy Michael
 */
public class Config {

    // Config file key for the server port to listen
    public static final String KEY_PORT = "port";

    // Config file key for the output directory to write data to
    public static final String KEY_OUTPUT_DIR = "output_dir";

    // Config file key for the array detailing the name and type of the fields in the data
    public static final String KEY_SCHEMA = "schema";

    // Singleton instance of the configuration
    private static Config instance;

    // Internal storage for the parsed configuration
    private final Map source;

    /**
     * Read the configuration file, perform some validation and parse
     * it into internal storage.
     *
     * @param configPath absolute file path for the configuration file
     * @throws Exception something wrong during file reading
     */
    public static void init(String configPath) throws Exception {
        // Read file into a stream of lines (we process line by line)
        Stream<String> stream = Files.lines(Paths.get(configPath));

        // Init a holding structure for the configuration
        Map<String, Object> localSource = new HashMap<>();

        // For each line of the config file, ...
        stream.forEach(s -> {
            // Separate key from value
            String[] kv = s.split("=");
            if (2 != kv.length)
                throw new RuntimeException("Bad config line format: " + s);
            else {
                switch (kv[0]) {

                    // port
                    case KEY_PORT:
                        localSource.put(KEY_PORT, Integer.parseInt(kv[1]));
                        break;

                    // schema
                    case KEY_SCHEMA:
                        LinkedList list = new LinkedList();
                        Arrays.stream(kv[1].split(";")).forEach(s1 -> {
                            String[] namesTypesAndUnits = s1.split(":");
                            if (namesTypesAndUnits.length == 2) {
                                list.add(new Schema(namesTypesAndUnits[0], namesTypesAndUnits[1], null), list.getSize());
                            } else if (namesTypesAndUnits.length == 3) {
                                list.add(new Schema(namesTypesAndUnits[0], namesTypesAndUnits[1], namesTypesAndUnits[2]), list.getSize());
                            } else {
                                throw new RuntimeException("Illegal schema format: " + s1);
                            }
                        });
                        localSource.put(KEY_SCHEMA, list);
                        break;

                    // output_dir
                    case KEY_OUTPUT_DIR:
                        if (Files.notExists(Paths.get(kv[1])))
                            throw new RuntimeException("Output directory does not exists: " + kv[1]);
                        localSource.put(KEY_OUTPUT_DIR, kv[1]);
                        break;

                    // invalid config key
                    default:
                        throw new RuntimeException("Unrecognized config key: " + kv[0]);
                }
            }
        });

        // Everything is valid, call internal constructor with those config
        instance = new Config(localSource);
    }

    /**
     * Internal constructor. This constructor makes sure all required parameters
     * are all there before copying over to the internal storage.
     *
     * @param source parsed configuration parameters
     */
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

    /**
     * Class for holding data field name and its type. Currently we only
     * support integer and float as type.
     *
     * @author Weinan Jimmy Michael
     */
    public static class Schema {

        public static final String TYPE_INT = "integer";
        public static final String TYPE_FLOAT = "float";

        private final String name;
        private final String type;
        private final String unit;

        public Schema(String name, String type, String unit) {
            this.name = name;
            this.type = type;
            this.unit = unit;

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

        public String getUnit() {
            return unit;
        }
    }
}
