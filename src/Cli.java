import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Command line interface to interact with APIs.
 *
 * @author Weinan, Jimmy, Michael
 */
public class Cli {

    // the singleton instance
    private static Cli instance;

    /**
     * Get the singleton of this CLI.
     *
     * @return the singleton instance.
     */
    public static Cli getInstance() {
        if (null == instance)
        instance = new Cli();
        return instance;
    }

    /**
     * Start the Command-Line interface to the APIs.
     */
    public void start() {
        System.out.println("Select an option (press Ctrl-C to quit):");
        System.out.println();
        System.out.println("1. Get Summary Data");
        System.out.println("2. Search Data");
        System.out.println("3. Read Data");
        System.out.println();
        System.out.print("Action: ");
        try {
            Scanner reader = new Scanner(System.in);  // Reading from System.in
            int n = reader.nextInt(); // Scans the next token of the input as an int.

            // Ensure input is either 1, 2, or 3.
            switch(n) {
            case 1:
                getSummaryData();
                break;
            case 2:
                searchData();
                break;
            case 3:
                readData();
                break;
            default:
                System.out.println("Invalid input");
                break;
            }
        } catch (InputMismatchException ex) {
            System.out.println("Invalid input");
        }

        // start the program again.
        start();
    }

    /**
     * The command-line interface for getting summary data
     */
    private void getSummaryData() {
        System.out.println(Api.defaultApi().getSummary().render());
    }

    /**
     * The command-line interface for searching data
     */
    private void searchData() {
        try {
            System.out.print("Field name: ");
            Scanner reader = new Scanner(System.in);
            String fieldName = reader.nextLine();

            System.out.print("value: ");
            String value = reader.nextLine();

            // Continue searching for the next instance until there are no more.
            while (true) {
                System.out.println(Api.defaultApi().search(fieldName, value).render());
                System.out.println("Press [Enter] to continue");
                reader.nextLine();
            }
        } catch (InputMismatchException ex) {
            System.out.println("Invalid input.");
        } catch (Api.NoMoreDataException ex) {
            System.out.println("End of data points.");
        }
    }

    /**
     * The command-line interface for reading data
     */
    private void readData() {
        try {
            Scanner reader = new Scanner(System.in);

            System.out.print("Start time hour: ");
            int startHour = reader.nextInt();

            System.out.print("Minute: ");
            int startMinute = reader.nextInt();

            System.out.print("Second: ");
            float startSecond = reader.nextFloat();

            System.out.print("End time hour: ");
            int endHour = reader.nextInt();

            System.out.print("Minute: ");
            int endMinute = reader.nextInt();

            System.out.print("Second: ");
            float endSecond = reader.nextFloat();
            reader.nextLine(); // Consume the \n in \r\n so it doesn't skip the next nextLine() call.

            System.out.print("Measurements (comma separated, no spaces): ");
            String measurementsString = reader.nextLine();

            String[] measurements = measurementsString.split(",");

            LinkedList schema = Config.getInstance().getSchema();

            // Get array of valid schema names
            int numberOfSchemaValues = schema.getSize();
            String[] validValues = new String[numberOfSchemaValues];
            for (int i = 0; i < numberOfSchemaValues; i++) {
                Config.Schema schemaValue = (Config.Schema) schema.get(i);
                validValues[i] = schemaValue.getName();
            }

            // Make sure at least one measurement given was valid.
            boolean atLeastOneMeasurementIsValid = Arrays.stream(measurements)
            .anyMatch(data -> contains(validValues, data));

            if (atLeastOneMeasurementIsValid) {
                // Read and display a page of data until there are no more data points.
                while (true) {
                    System.out.println(Api.defaultApi().read(
                            TimeIndex.of(startHour, startMinute, startSecond),
                            TimeIndex.of(endHour,endMinute,endSecond),
                            measurements
                    ).render());
                    System.out.println("Press [Enter] to continue");
                    reader.nextLine();
                }
            } else {
                System.out.println("No valid measurements given.");
            }
        } catch (InputMismatchException ex) {
            System.out.println("Invalid input.");
        } catch (Api.NoMoreDataException ex) {
            System.out.println("End of data points.");
        }
    }

    /**
     * Method to check if a string is in a string array.
     */
    private static boolean contains(String[] strArray, String searchValue) {
        for(String s: strArray) {
            if(s.equals(searchValue))
                return true;
        }
        return false;
    }
}
