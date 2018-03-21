import java.util.Date;

public class DateOutputHandler extends OutputHandler {
    public DateOutputHandler(Appendable outputDump, String lineSeparator)
    {
        super(outputDump, lineSeparator);
    }

    @Override
    protected String formatMessage(String message) {
        long time = System.currentTimeMillis();
        return "[" + new Date(time) + "; " + time + "] " + message;
    }
}
