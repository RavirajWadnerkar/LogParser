package LogParserApp;

public abstract class LogHandler {
    protected LogHandler successor;

    public void setSuccessor(LogHandler successor) {
        this.successor = successor;
    }

    public abstract void handleLog(String logLine);
}
