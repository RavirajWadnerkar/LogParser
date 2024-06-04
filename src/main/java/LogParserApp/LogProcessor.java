package LogParserApp;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class LogProcessor {
    private String filename;

    public LogProcessor(String filename) {
        this.filename = filename;
    }

    public void processLogs() {
        LogHandler apmHandler = new APMLogHandler();
        LogHandler appHandler = new ApplicationLogHandler();
        LogHandler reqHandler = new RequestLogHandler();

        apmHandler.setSuccessor(appHandler);
        appHandler.setSuccessor(reqHandler);

        try {
            List<String> lines = Files.readAllLines(Paths.get(filename));
            for (String line : lines) {
                apmHandler.handleLog(line);
            }

            ((APMLogHandler) apmHandler).writeToJson();
            ((ApplicationLogHandler) appHandler).writeToJson();
            ((RequestLogHandler) reqHandler).writeToJson();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
