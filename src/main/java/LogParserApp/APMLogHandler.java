
package LogParserApp;

import java.io.FileWriter;
import java.io.IOException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;  // Import GsonBuilder for pretty printing
import java.util.*;

public class APMLogHandler extends LogHandler {
    private Map<String, List<Double>> metrics = new HashMap<>();

    // Public getter for metrics
    public Map<String, List<Double>> getMetrics() {
        return metrics;
    }

    @Override
    public void handleLog(String logLine) {
        if (logLine.contains("metric")) {
            Map<String, String> logParts = parseLogLine(logLine);
            String metricName = logParts.get("metric");
            if (logParts.containsKey("value") && logParts.get("value") != null && !logParts.get("value").isEmpty()) {
                try {
                    double value = Double.parseDouble(logParts.get("value"));
                    metrics.putIfAbsent(metricName, new ArrayList<>());
                    metrics.get(metricName).add(value);
                } catch (NumberFormatException e) {
                    System.out.println("Ignoring non-numeric value for metric " + metricName);
                    metrics.putIfAbsent(metricName, new ArrayList<>());  // Initialize list but don't add value
                }
            } else {
                System.out.println("No value provided for metric " + metricName);
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
            if (keyValue.length == 2 && keyValue[1] != null) {  // Ensure there is a value part and it's not null
                logParts.put(keyValue[0], keyValue[1]);
            }
        }
        return logParts;
    }

    public void writeToJson() {
        Map<String, Map<String, Double>> summary = new HashMap<>();
        for (Map.Entry<String, List<Double>> entry : metrics.entrySet()) {
            List<Double> values = entry.getValue();
            Collections.sort(values);
            double min = values.isEmpty() ? 0 : values.get(0);
            double max = values.isEmpty() ? 0 : values.get(values.size() - 1);
            double average = values.stream().mapToDouble(val -> val).average().orElse(0.0);
            double median = calculateMedian(values);

            Map<String, Double> metricSummary = new HashMap<>();
            metricSummary.put("minimum", min);
            metricSummary.put("average", average);
            metricSummary.put("median", median);
            metricSummary.put("max", max);

            summary.put(entry.getKey(), metricSummary);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();  // Use GsonBuilder to enable pretty printing
        try (FileWriter writer = new FileWriter("output/apm.json")) {
            gson.toJson(summary, writer);
        } catch (IOException e) {
            System.out.println("Error writing to JSON file: " + e.getMessage());
        }
    }

    public double calculateMedian(List<Double> values) {
        if (values.isEmpty()) {
            return 0.0;  // Handle empty list scenario
        }
        int size = values.size();
        if (size % 2 == 0) {
            return (values.get(size / 2 - 1) + values.get(size / 2)) / 2.0;
        } else {
            return values.get(size / 2);
        }
    }
}
