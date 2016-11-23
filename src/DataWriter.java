import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class DataWriter {

    private static DataWriter instance;

    public static final String SUMMARY_FILE_NAME = "summary";
    public static final String DAT_FILE_PREFIX = "data";
    private final String outputDirectory;

    private DataWriter(String outputDirectory) {
        if (!outputDirectory.endsWith(File.separator))
            this.outputDirectory = outputDirectory + File.separator;
        else
            this.outputDirectory = outputDirectory;
    }

    public void writeToSummary(String content) throws Exception {
        this.writeToFile(SUMMARY_FILE_NAME, content);
    }

    public void writeToData(String content, long index) throws Exception {
        this.writeToFile(getDataFileName(index), content);
    }

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

    private void sanitizeNewLine(String content) {
        while (content.startsWith("\n"))
            content = content.substring(1);
        while (content.endsWith("\n"))
            content = content.substring(0, content.length() - 1);
    }

    public static DataWriter getInstance() {
        if (null == instance)
            instance = new DataWriter(Config.getInstance().getOutputDirectory());
        return instance;
    }
}
