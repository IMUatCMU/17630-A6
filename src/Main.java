public class Main {
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
        DataCollector.getInstance().collectData();

        // Boot up the API for user query.
        Cli.getInstance().start();
    }
}
