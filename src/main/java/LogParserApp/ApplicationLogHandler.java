package LogParserApp;

import java.io.FileWriter;
import java.io.IOException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;  // Import GsonBuilder
import java.util.HashMap;
import java.util.Map;

public class ApplicationLogHandler extends LogHandler {
    private Map<String, Integer> logCounts = new HashMap<>();

    @Override
    public void handleLog(String logLine) {
        if (logLine.contains("level")) {
            Map<String, String> logParts = parseLogLine(logLine);
            // Get the level value safely, checking for null or empty
            String level = logParts.getOrDefault("level", "").trim();
            if (!level.isEmpty()) {
                logCounts.put(level, logCounts.getOrDefault(level, 0) + 1);
            } else {
                System.out.println("Ignoring log with empty or null level value");
            }
        } else if (successor != null) {
            successor.handleLog(logLine);
        }
    }
    

    private Map<String, String> parseLogLine(String logLine) {
        Map<String, String> logParts = new HashMap<>();
        String[] parts = logLine.split(" ");
        for (String part : parts) {
            String[] keyValue = part.split("=");
            if (keyValue.length == 2) {
                logParts.put(keyValue[0], keyValue[1]);
            }
        }
        return logParts;
    }

    public Map<String, Integer> getLogCounts() {
        return logCounts;
    }
    

    public void writeToJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();  // Use GsonBuilder to enable pretty printing
        try (FileWriter writer = new FileWriter("output/application.json")) {
            gson.toJson(logCounts, writer);
        } catch (IOException e) {
            System.out.println("Error writing to JSON file: " + e.getMessage());
        }
    }
}