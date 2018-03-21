import org.eclipse.jetty.server.Request;

/**
 * The purpose of this class is, as the name implies, to determine the validity of requests (based on past request
 * information). It's sole purpose is thus basically to look at every request and determine "yeah, that's an ok request,
 * you can go ahead and handle it" or "that request is malicious or spam, dump it!". This class handles auto banning,
 * etc.
 *
 * @author Philip Rodriguez
 */
public interface RequestValidator {
    /**
     * This method should be called one time per every request a server receives to determine if the request should
     * actually be processed. If this returns false, that means the request should just be dumped.
     *
     * @param baseRequest is the base request object passed into the server handler.
     * @return true if the request is not malicious, false otherwise.
     */
    public boolean isValid(Request baseRequest);
}
