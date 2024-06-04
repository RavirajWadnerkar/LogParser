package LogParserApp;

public class Main {
    public static void main(String[] args) {
        String filename = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--file") && i + 1 < args.length) {
                filename = args[i + 1];
            }
        }
        if (filename == null) {
            System.out.println("Please specify a file name using '--file <filename.txt>'");
            System.exit(1);
        }

        LogProcessor processor = new LogProcessor(filename);
        processor.processLogs();
    }
}
