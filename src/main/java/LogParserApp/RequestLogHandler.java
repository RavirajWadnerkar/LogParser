package LogParserApp;

import java.io.FileWriter;
import java.io.IOException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder; // Import GsonBuilder for pretty printing
import java.util.*;

public class RequestLogHandler extends LogHandler {
    private Map<String, List<Integer>> responseTimes = new HashMap<>();
    private Map<String, Map<String, Integer>> statusCodes = new HashMap<>();

    @Override
    public void handleLog(String logLine) {
        if (logLine.contains("request_method")) {
            Map<String, String> logParts = parseLogLine(logLine);
            String url = logParts.get("request_url");

            try {
                int responseTime = Integer.parseInt(logParts.get("response_time_ms"));
                responseTimes.putIfAbsent(url, new ArrayList<>());
                responseTimes.get(url).add(responseTime);

                if (logParts.get("response_status").matches("\\d{3}")) {
                    String statusCode = logParts.get("response_status").charAt(0) + "XX";
                    statusCodes.putIfAbsent(url, new HashMap<>());
                    statusCodes.get(url).put(statusCode, statusCodes.get(url).getOrDefault(statusCode, 0) + 1);
                } else {
                    System.out.println("Invalid response status format, expected 3 digits, found: " + logParts.get("response_status"));
                }
            } catch (NumberFormatException e) {
                System.out.println("Ignoring non-numeric response time for URL: " + url);
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
                logParts.put(keyValue[0].trim().replace("\"", ""), keyValue[1].trim().replace("\"", ""));
            }
        }
        return logParts;
    }

    public void writeToJson() {
        Map<String, Object> summary = new HashMap<>();
        for (Map.Entry<String, List<Integer>> entry : responseTimes.entrySet()) {
            List<Integer> times = entry.getValue();
            Collections.sort(times);
            Map<String, Double> timeSummary = new HashMap<>();
            timeSummary.put("min", (double)times.get(0));
            timeSummary.put("max", (double)times.get(times.size() - 1));
            timeSummary.put("median", calculateMedian(times));
            timeSummary.put("average", calculateAverage(times));
            timeSummary.put("50_percentile", calculatePercentile(times, 50));
            timeSummary.put("90_percentile", calculatePercentile(times, 90));
            timeSummary.put("95_percentile", calculatePercentile(times, 95));
            timeSummary.put("99_percentile", calculatePercentile(times, 99));

            Map<String, Object> urlSummary = new HashMap<>();
            urlSummary.put("response_times", timeSummary);
            urlSummary.put("status_codes", statusCodes.get(entry.getKey()));

            summary.put(entry.getKey(), urlSummary);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter("output/request.json")) {
            gson.toJson(summary, writer);
        } catch (IOException e) {
            System.out.println("Error writing to JSON file: " + e.getMessage());
        }
    }

    public Map<String, List<Integer>> getResponseTimes() {
        return responseTimes;
    }

    public Map<String, Map<String, Integer>> getStatusCodes() {
        return statusCodes;
    }

    public double calculateMedian(List<Integer> values) {
        int size = values.size();
        if (size % 2 == 0) {
            return (values.get(size / 2 - 1) + values.get(size / 2)) / 2.0;
        } else {
            return values.get(size / 2);
        }
    }

    public double calculateAverage(List<Integer> values) {
        if (values.isEmpty()) {
            return 0.0;
        }
        double sum = 0;
        for (int value : values) {
            sum += value;
        }
        return sum / values.size();
    }

    public double calculatePercentile(List<Integer> values, double percentile) {
        int index = (int) Math.ceil(percentile / 100.0 * values.size()) - 1;
        return values.get(Math.max(index, 0));
    }
}
