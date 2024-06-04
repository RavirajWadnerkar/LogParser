package LogParserApp;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ApplicationLogHandlerTest {
    private ApplicationLogHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ApplicationLogHandler();
    }

    @Test
    void testHandleValidLogEntry() {
        handler.handleLog("level=INFO message='Application started successfully'");
        assertEquals(Integer.valueOf(1), handler.getLogCounts().get("INFO"));
    }

    @Test
    void testIgnoreInvalidLogEntry() {
        handler.handleLog("something=weird message='Invalid log format'");
        assertTrue(handler.getLogCounts().isEmpty());
    }

    @Test
    void testHandleMultipleLogEntries() {
        handler.handleLog("level=ERROR message='Null pointer exception'");
        handler.handleLog("level=ERROR message='Index out of bounds'");
        handler.handleLog("level=INFO message='Application started successfully'");
        assertEquals(Integer.valueOf(2), handler.getLogCounts().get("ERROR"));
        assertEquals(Integer.valueOf(1), handler.getLogCounts().get("INFO"));
    }

    @Test
    void testValidAndInvalidMixedLogs() {
        handler.handleLog("level=INFO message='Valid log entry'");
        handler.handleLog("levl=INFO message='Typo in level key'");
        assertEquals(Integer.valueOf(1), handler.getLogCounts().get("INFO"));
        assertNull(handler.getLogCounts().get("levl"));
    }

    @Test
    void testJsonOutputForValidLogs() {
        handler.handleLog("level=DEBUG message='Debugging mode enabled'");
        assertDoesNotThrow(() -> handler.writeToJson());
    }
}
