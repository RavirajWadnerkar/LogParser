package LogParserApp;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;

class RequestLogHandlerTest {
    private RequestLogHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RequestLogHandler();
    }

    @Test
    void testValidLogEntry() {
        handler.handleLog("request_method=GET request_url=/api/data response_status=200 response_time_ms=100");
        assertFalse(handler.getResponseTimes().get("/api/data").isEmpty());
        assertEquals(100, handler.getResponseTimes().get("/api/data").get(0));
    }

    @Test
    void testInvalidResponseTime() {
        handler.handleLog("request_method=POST request_url=/api/update response_status=200 response_time_ms=abc");
        assertTrue(handler.getResponseTimes().get("/api/update") == null || handler.getResponseTimes().get("/api/update").isEmpty());
    }

    @Test
    void testInvalidResponseStatus() {
        handler.handleLog("request_method=POST request_url=/api/update response_status=ABC response_time_ms=200");
        assertTrue(handler.getStatusCodes().get("/api/update") == null || handler.getStatusCodes().get("/api/update").isEmpty());
    }

    @Test
    void testMultipleValidLogsSameUrl() {
        handler.handleLog("request_method=POST request_url=/api/multi response_status=200 response_time_ms=150");
        handler.handleLog("request_method=POST request_url=/api/multi response_status=200 response_time_ms=200");
        handler.handleLog("request_method=POST request_url=/api/multi response_status=200 response_time_ms=200");
        assertEquals(3, handler.getResponseTimes().get("/api/multi").size());
    }

    @Test
    void testStatusCodeGrouping() {
        handler.handleLog("request_method=POST request_url=/api/status response_status=404 response_time_ms=120");
        assertEquals(Integer.valueOf(1), handler.getStatusCodes().get("/api/status").get("4XX"));
    }

    @Test
    void testEmptyLogEntry() {
        handler.handleLog("");
        assertTrue(handler.getResponseTimes().isEmpty());
    }

    @Test
    void testCalculateMedianOdd() {
        assertEquals(200.0, handler.calculateMedian(Arrays.asList(100, 200, 300)));
    }

    @Test
    void testCalculateMedianEven() {
        assertEquals(250.0, handler.calculateMedian(Arrays.asList(200, 300)));
    }

    @Test
    void testCalculateAverage() {
        assertEquals(175.0, handler.calculateAverage(Arrays.asList(100, 250)));
    }
}
