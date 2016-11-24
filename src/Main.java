import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Main {

    // start command to be issued to the server
    private static final String START_COMMAND = "start\n";

    // TODO change it back to 5 minutes, it is now 30 seconds
    private static final Long FIVE_MIN_IN_MILLISECONDS =  30 * 1000L;

    /**
     * Entry point of the program
     *
     * @param args command line args, expect the first arg being the config file path
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // Initialize configuration
        if (args.length > 0) {
            Config.init(args[0]);
        } else {
            throw new Exception("No config file path provided");
        }

        // Call server to collect data, collect summary stats and write to disk
        collectData();

        // Boot up the API for user query.
        startApi();
    }

    private static void startApi() throws Exception {
        // TODO some command line UI system that calls Api.java, examples are below.

        /* Getting summary */
//        System.out.println(Api.defaultApi().getSummary().render());

        /* Searching for hour=15 */
//        System.out.println("-------");
//        try {
//            while (true) {
//                System.out.println(Api.defaultApi().search("hour", "15").render());
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();     // expect NoMoreDataException
//        }

        /* Read data within a time frame */
//        try {
//            while (true) {
//                System.out.println("-------");
//                System.out.println(Api.defaultApi().read(
//                        TimeIndex.of(16,40,0),
//                        TimeIndex.of(17,40,0),
//                        new String[]{"hour", "minute", "second"}
//                ).render());
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();     // expect NoMoreDataException
//        }
    }

    private static void collectData() throws Exception {
        Socket socket = new Socket("localhost", Config.getInstance().getPort());
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        // Tell server to start sending data
        out.writeBytes(START_COMMAND);

        // Setup a few things:
        // 1) firstMeasurement: we need to know which one is the first one, in order
        //    to fill the startTimestamp in summary and hash
        // 2) summary: we need that to accumulate stats for all the data
        // 3) hash: we need that to determine timestamp-bucket correlation
        Measurement firstMeasurement = null;
        Summary summary = Summary.empty();
        summary.setSchema(Config.getInstance().getSchema());
        Hash hash = Hash.getInstance();

        // Loop for reading data from server
        while (true) {

            // Gather all data from configured schema
            Map<String, Object> data = new HashMap<>();
            Config.getInstance().getSchema().forEach(o -> {
                Config.Schema schema = (Config.Schema) o;
                try {
                    switch (schema.getType()) {
                        case Config.Schema.TYPE_INT:
                            data.put(schema.getName(), in.readInt());
                            summary.increaseBytesCount(4);  // int is 4 bytes in java
                            break;
                        case Config.Schema.TYPE_FLOAT:
                            data.put(schema.getName(), in.readFloat());
                            summary.increaseBytesCount(4);  // float is 4 bytes in java
                            break;
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });

            // Create a new measurement from the data
            Measurement newMeasurement = new Measurement(data);

            // Assign the measurement as the first measurement if it's vacant
            // Initialize the time range for the hash, so we can start assigning buckets
            if (null == firstMeasurement) {
                firstMeasurement = newMeasurement;
                hash.setStartMillisecond(firstMeasurement.getTimeInMilliseconds());
                hash.setEndMillisecond(firstMeasurement.getTimeInMilliseconds() + FIVE_MIN_IN_MILLISECONDS);
                summary.setStartTimestamp(firstMeasurement.getTimeInMilliseconds());
            }

            // Stop receiving if time is out of range
            if (newMeasurement.getTimeInMilliseconds() > hash.getEndMillisecond()) {
                System.out.println("Start: " + hash.getStartMillisecond() + " end: " + hash.getEndMillisecond());
                break;
            }

            // Update end time stamp
            summary.setEndTimestamp(newMeasurement.getTimeInMilliseconds());

            // TODO remove this printing line
            System.out.println(newMeasurement.toString());

            // Write the measurement to the appropriate bucket
            long bucketIndex = hash.getBucketIndex(newMeasurement.getTimeInMilliseconds());
            try {
                DataWriter.getInstance().writeToData(newMeasurement.toString(), bucketIndex);
            } catch (Exception ex) {
                throw new RuntimeException("Write data failed: " + ex.getMessage());
            }
        }

        // Bye to the server
        socket.close();

        // Write summary file
        DataWriter.getInstance().writeToSummary(summary.toString());
    }
}
