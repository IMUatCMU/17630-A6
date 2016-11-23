import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class Main {

    private static final String START_COMMAND = "start\n";

    // TODO change it back to 5 minutes, it is now 30 seconds
    private static final Long FIVE_MIN_IN_MILLISECONDS =  30 * 1000L;

    public static void main(String[] args) throws Exception {
        Config.init("/Users/davidiamyou/Downloads/A6/a6.config");
        a6();
    }

    private static void a6() throws Exception {
        Socket socket = new Socket("localhost", Config.getInstance().getPort());
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        out.writeBytes(START_COMMAND);

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
                            break;
                        case Config.Schema.TYPE_FLOAT:
                            data.put(schema.getName(), in.readFloat());
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

            // Update end time stamp
            summary.setEndTimestamp(firstMeasurement.getTimeInMilliseconds());

            // Stop receiving if time is out of range
            if (newMeasurement.getTimeInMilliseconds() > hash.getEndMillisecond()) {
                System.out.println("Start: " + hash.getStartMillisecond() + " end: " + hash.getEndMillisecond());
                break;
            }

            System.out.println(newMeasurement.toString());

            // Write the measurement to the appropriate bucket
            long bucketIndex = hash.getBucketIndex(newMeasurement.getTimeInMilliseconds());
            try {
                DataWriter.getInstance().writeToData(newMeasurement.toString(), bucketIndex);
            } catch (Exception ex) {
                throw new RuntimeException("Write data failed: " + ex.getMessage());
            }

            // Increase count
            summary.increaseRecordCount();
        }
        socket.close();

        // Write summary file
        DataWriter.getInstance().writeToSummary(summary.toString());
    }
}
