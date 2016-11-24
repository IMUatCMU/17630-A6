import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * This class is responsible for writing data to the storage area.
 *
 * @author Weinan Jimmy Michael
 */
public class DataWriter {

    // Singleton instance
    private static DataWriter instance;

    // summary data file name
    public static final String SUMMARY_FILE_NAME = "summary";

    // data bucket file name (i.e. data.0 data.1 ...)
    public static final String DAT_FILE_PREFIX = "data";

    // output directory (a.k.a storage area)
    private final String outputDirectory;

    /**
     * Private constructor, makes sure output directory ends with a file
     * separator, so we can directly append file names to it later.
     *
     * @param outputDirectory
     */
    private DataWriter(String outputDirectory) {
        if (!outputDirectory.endsWith(File.separator))
            this.outputDirectory = outputDirectory + File.separator;
        else
            this.outputDirectory = outputDirectory;
    }

    /**
     * Append the content to summary file.
     *
     * @param content
     * @throws Exception
     */
    public void writeToSummary(String content) throws Exception {
        this.writeToFile(SUMMARY_FILE_NAME, content);
    }

    /**
     * Append the data bucket specified by index with the given content.
     *
     * @param content
     * @param index
     * @throws Exception
     */
    public void writeToData(String content, long index) throws Exception {
        this.writeToFile(getDataFileName(index), content);
    }

    /**
     * Append the custom file with the given content.
     *
     * @param fileName
     * @param content
     * @throws Exception
     */
    private void writeToFile(String fileName, String content) throws Exception {
        sanitizeNewLine(content);

        Path outputPath = Paths.get(this.outputDirectory + fileName);
        if (!Files.exists(outputPath)) {
            Files.createFile(outputPath);
        } else {
            content = "\n" + content;
        }

        Files.write(outputPath, content.getBytes(), StandardOpenOption.APPEND);
    }

    public static String getDataFileName(long index) {
        return DAT_FILE_PREFIX + "." + index;
    }

    /**
     * Helper to trim all new lines from start and end of the content
     * @param content
     */
    private void sanitizeNewLine(String content) {
        while (content.startsWith("\n"))
            content = content.substring(1);
        while (content.endsWith("\n"))
            content = content.substring(0, content.length() - 1);
    }

    /**
     * Get the {@link DataWriter} singleton.
     * @return
     */
    public static DataWriter getInstance() {
        if (null == instance)
            instance = new DataWriter(Config.getInstance().getOutputDirectory());
        return instance;
    }
}
