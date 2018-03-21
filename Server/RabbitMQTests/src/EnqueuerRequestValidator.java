import org.eclipse.jetty.server.Request;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class will monitor requests by IP address, and return that a request is invalid based on how many requests are
 * coming from an IP address per second, and how many bytes are coming from an IP address per request.
 *
 * @author Philip Rodriguez
 */
public class EnqueuerRequestValidator implements RequestValidator {
    private final OutputHandler outputHandler;
    private final int maxRequestsPerSecond;
    private final int maxBytesPerRequest;
    private final ConcurrentHashMap<String, RequesterInformation> requesterInformationByIP;

    /**
     * Creates an EnqueuerRequestValidator based on the parameters provided.
     *
     * @param maxRequestsPerSecond is the maximum number of requests an IP can make in one second before banning occurs.
     * @param maxBytesPerRequest is the maximum content length of a request allowed before banning occurs.
     * @param outputHandler is where output information produced by this class should go.
     */
    public EnqueuerRequestValidator(int maxRequestsPerSecond, int maxBytesPerRequest, OutputHandler outputHandler)
    {
        this.maxRequestsPerSecond = maxRequestsPerSecond;
        this.maxBytesPerRequest = maxBytesPerRequest;
        this.outputHandler = outputHandler;
        this.requesterInformationByIP = new ConcurrentHashMap<String, RequesterInformation>();
    }

    @Override
    public boolean isValid(Request baseRequest) {
        String ip = baseRequest.getRemoteAddr();
        long requestSize = baseRequest.getContentLength();

        // Make sure there is an entry for this IP! (Could be a new IP)
        requesterInformationByIP.putIfAbsent(ip, new RequesterInformation(ip, maxRequestsPerSecond, outputHandler));

        // Now guaranteed to not be null
        RequesterInformation requesterInformation = requesterInformationByIP.get(ip);

        // Count this request!
        requesterInformation.pushRequestTimestamp();

        // Check size...
        if (requestSize > maxBytesPerRequest)
        {
            // Too large! Ban!
            requesterInformation.manuallyBan();
        }

        //Return based on banned status.
        return !requesterInformation.isBanned();
    }

    /**
     * This class stores information relevant to each IP address's history through this validator.
     */
    private static class RequesterInformation
    {
        private final OutputHandler outputHandler;
        private final int maxRequestsPerSecond;

        private final String ipAddress;
        private int numberOfTimesBanned;
        private long bannedUntil;
        private final LinkedList<Long> requestTimestamps;

        public RequesterInformation(String ipAddress, int maxRequestsPerSecond, OutputHandler outputHandler)
        {
            this.ipAddress= ipAddress;
            this.outputHandler = outputHandler;
            this.maxRequestsPerSecond = maxRequestsPerSecond;
            this.requestTimestamps = new LinkedList<Long>();
        }

        /**
         * Pushes on a timestamp with the current time. Removes from the head of requestTimestamps to keep the length of
         * it <= maxRequestsPerSecond. This should be called once for every request a client makes.
         */
        public synchronized void pushRequestTimestamp()
        {
            requestTimestamps.addLast(System.currentTimeMillis());
            while(requestTimestamps.size() > this.maxRequestsPerSecond)
            {
                requestTimestamps.removeFirst();
            }

            // Ban if appropriate
            if (requestTimestamps.size() == maxRequestsPerSecond && requestTimestamps.getLast()-requestTimestamps.getFirst() < 1000)
            {
                // Less than 1000ms for the last maxRequestsPerSecond requests! Ban!
                manuallyBan();
            }
        }

        public synchronized void manuallyBan()
        {
            // Prevent double banning...
            if (!isBanned()) {
                numberOfTimesBanned++;

                // If they didn't learn their lesson in 5 bans, just permaban them...
                if (numberOfTimesBanned > 5)
                {
                    permanentlyBan();
                }

                // Ban for uh... 5 to the power of the number of times banned minutes?
                // That way... the bans will look like 5 minutes, 25 minutes, 125 minutes, 625 minutes, 3125 minutes...
                long banLengthInMs = (long)Math.pow(5, numberOfTimesBanned)*60000;
                bannedUntil = System.currentTimeMillis() + banLengthInMs;
                outputHandler.appendMessage("Banned " + ipAddress + " for " + getRemainingBanPretty());
            }
        }

        /**
         * Should enforce some firewall-level ban on the associated IP address...
         */
        private synchronized void permanentlyBan()
        {
            // TODO: Assuming we're running linux, do something like iptables -A INPUT -s [ipAddress] -j DROP
            System.err.println("permanentlyBan() not yet implemented!");
            outputHandler.appendMessage("Permanently banned " + ipAddress);
        }

        /**
         * Returns true if the IP address is banned, false otherwise.
         *
         * @return true if the IP address is banned, false otherwise
         */
        public synchronized boolean isBanned()
        {
            return System.currentTimeMillis() < bannedUntil;
        }

        /**
         *
         * @return The remaining ban time for this IP address in milliseconds.
         */
        public synchronized long getRemainingBan()
        {
            if (!isBanned())
                return 0;
            return bannedUntil - System.currentTimeMillis();
        }

        /**
         *
         * @return The remaining ban time for this IP address in hours, minutes, and seconds.
         */
        public synchronized String getRemainingBanPretty()
        {
            long remainingBan = getRemainingBan()/1000;
            long seconds = remainingBan % 60;
            remainingBan -= seconds;
            remainingBan /= 60;
            long minutes = remainingBan % 60;
            remainingBan -= minutes;
            remainingBan /= 60;
            long hours = remainingBan % 60;

            return hours + " hours " + minutes + " minutes and " + seconds + " seconds";
        }
    }
}
