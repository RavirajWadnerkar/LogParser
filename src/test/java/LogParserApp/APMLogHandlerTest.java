package LogParserApp;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

class APMLogHandlerTest {
    private APMLogHandler handler;

    @BeforeEach
    void setUp() {
        handler = new APMLogHandler();
    }

    @Test
    void testHandleLogWithValidMetric() {
        handler.handleLog("metric=cpu_usage_percent value=65");
        handler.handleLog("metric=cpu_usage_percent value=72");
        assertEquals(Arrays.asList(65.0, 72.0), handler.getMetrics().get("cpu_usage_percent"));
    }

    @Test
    void testHandleLogWithNoMetric() {
        handler.handleLog("some_random_text");
        assertTrue(handler.getMetrics().isEmpty());
    }

    @Test
    void testCalculateMedianWithOddNumberOfElements() {
        List<Double> sampleData = Arrays.asList(10.0, 20.0, 30.0);
        assertEquals(20.0, handler.calculateMedian(sampleData));
    }

    @Test
    void testCalculateMedianWithEvenNumberOfElements() {
        List<Double> sampleData = Arrays.asList(10.0, 20.0, 30.0, 40.0);
        assertEquals(25.0, handler.calculateMedian(sampleData));
    }

    @Test
    void testWriteToJson() {
        handler.handleLog("metric=memory_usage_percent value=85");
        handler.handleLog("metric=memory_usage_percent value=90");
        assertDoesNotThrow(() -> handler.writeToJson());
    }

    // Additional tests to cover edge cases and complex scenarios
    @Test
    void testResponseTimesAcrossMultipleLogs() {
        handler.handleLog("metric=cpu_usage_percent value=50");
        handler.handleLog("metric=cpu_usage_percent value=60");
        handler.handleLog("metric=cpu_usage_percent value=70");
        assertEquals(3, handler.getMetrics().get("cpu_usage_percent").size());
    }

    @Test
    void testLogsWithMixedValidAndInvalidData() {
        handler.handleLog("metric=network_bytes_in value=123456");
        handler.handleLog("metric=network_bytes_in value=invalid");
        assertEquals(1, handler.getMetrics().get("network_bytes_in").size());
        assertEquals(123456.0, handler.getMetrics().get("network_bytes_in").get(0));
    }

    @Test
    void testCalculateAverageNoData() {
        List<Double> noData = Arrays.asList();
        assertEquals(0.0, handler.calculateMedian(noData));
    }

    @Test
    void testCalculateAverageSingleEntry() {
        List<Double> singleData = Arrays.asList(100.0);
        assertEquals(100.0, handler.calculateMedian(singleData));
    }

    @Test
    void testComplexScenarioMultipleMetrics() {
        handler.handleLog("metric=cpu_usage_percent value=10");
        handler.handleLog("metric=cpu_usage_percent value=20");
        handler.handleLog("metric=memory_usage_percent value=30");
        handler.handleLog("metric=memory_usage_percent value=40");
        assertAll(
            () -> assertEquals(Arrays.asList(10.0, 20.0), handler.getMetrics().get("cpu_usage_percent")),
            () -> assertEquals(Arrays.asList(30.0, 40.0), handler.getMetrics().get("memory_usage_percent"))
        );
    }
}
