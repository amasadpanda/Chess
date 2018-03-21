import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * A class to pass around for specifying where output can go from various classes...
 *
 * @author Philip Rodriguez
 */
public abstract class OutputHandler {
    private final Appendable outputDump;
    private final String lineSeparator;

    /**
     * Creates an output handler that will write output to outputDump and separate messages using lineSeparator.
     * @param outputDump is the location to append all output to.
     * @param lineSeparator is the separator to use between messages.
     */
    public OutputHandler(Appendable outputDump, String lineSeparator)
    {
        this.outputDump = outputDump;
        this.lineSeparator = lineSeparator;
    }

    /**
     * This method formats a message so that i can be appended to the output dump.
     *
     * @param message the raw message to be formatted
     * @return a formatted version of message to be appended to the output dump
     */
    protected abstract String formatMessage(String message);

    /**
     * Writes a formatted version of message to the output dump.
     *
     * @param message is the message to format and then write to the output dump.
     */
    public final void appendMessage(String message)
    {
        // Ignore if no output dump...
        if (outputDump == null)
            return;

        // Otherwise, append!
        synchronized (outputDump)
        {
            try {
                outputDump.append(formatMessage(message));
                outputDump.append(lineSeparator);
            } catch (IOException e) {
                // Output handler issue! Not our problem...
            }
        }
    }
}
