import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that collects the data from the server
 *
 * @author Weinan, Jimmy, Michael
 */
public class DataCollector {
    // start command to be issued to the server
    private static final String START_COMMAND = "start\n";

    // the singleton instance
    private static DataCollector instance;

    /**
     * Get the singleton of this data collector.
     *
     * @return the singleton instance.
     */
    public static DataCollector getInstance() {
        if (null == instance)
            instance = new DataCollector();
        return instance;
    }

    public void collectData(Long dataRecordTime) throws Exception {
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

        System.out.println("Start receiving data...");
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
                hash.setEndMillisecond(firstMeasurement.getTimeInMilliseconds() + dataRecordTime);
                summary.setStartTimestamp(firstMeasurement.getTimeInMilliseconds());
            }

            // Stop receiving if time is out of range
            if (newMeasurement.getTimeInMilliseconds() > hash.getEndMillisecond()) {
                System.out.println("Start: " + hash.getStartMillisecond() + " end: " + hash.getEndMillisecond());
                break;
            }

            // Print out data on console
            System.out.println(newMeasurement.toString());

            // Update end time stamp
            summary.setEndTimestamp(newMeasurement.getTimeInMilliseconds());

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
        System.out.println("Finished receiving data.");

        // Write summary file
        DataWriter.getInstance().writeToSummary(summary.toString());
    }
}
