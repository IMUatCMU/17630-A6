import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * This class is responsible for reading data from the designated storage area.
 *
 * @author Weinan Jimmy Michael
 */
public class DataReader {

    // Singleton instance
    private static DataReader instance;

    // Storage directory
    private final String outputDirectory;

    /**
     * Private constructor, makes sure the output directory ends
     * with a file separator so we can directory append file name
     * to it later.
     *
     * @param outputDirectory
     */
    private DataReader(String outputDirectory) {
        if (!outputDirectory.endsWith(File.separator))
            this.outputDirectory = outputDirectory + File.separator;
        else
            this.outputDirectory = outputDirectory;
    }

    /**
     * Get the {@link DataReader} singleton. Initialize it when it's NULL.
     * @return
     */
    public static DataReader getInstance() {
        if (null == instance)
            instance = new DataReader(Config.getInstance().getOutputDirectory());
        return instance;
    }

    /**
     * Read all data into stream of lines from the summary file.
     *
     * @return
     * @throws Exception
     */
    public Stream<String> readFromSummary() throws Exception {
        return this.readFromFile(DataWriter.SUMMARY_FILE_NAME);
    }

    /**
     * Read all data into stream of lines from the data bucket specified by the index.
     *
     * @param index index of the data bucket to read.
     * @return
     * @throws Exception
     */
    public Stream<String> readFromData(int index) throws Exception {
        return this.readFromFile(DataWriter.getDataFileName(index));
    }

    /**
     * Read all data into stream of lines from a custom file.
     *
     * @param fileName
     * @return
     * @throws Exception
     */
    private Stream<String> readFromFile(String fileName) throws Exception {
        return Files.lines(Paths.get(this.outputDirectory + fileName));
    }
}
