package si.laurentius.msh.web.jsf;

import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * ServletRequestListener register maps the entry as request id, session id and client host to logging context
 *
 * @author Joze Rihtarsic
 * @since 2.1 (as4mail)
 */
public class LogMDCRequestListener implements ServletRequestListener {
    private static final Logger LOG = LoggerFactory.getLogger(LogMDCRequestListener.class);

    public static final String MDC_KEY_REQUEST_ID = "REQUEST_ID";
    public static final String MDC_KEY_SESSION_ID = "SESSION_ID";
    public static final String MDC_KEY_CLIENT_HOST = "CLIENT_HOST";

    /**
     * Need to clear the MDC parameters at the end of the request as the thread can be reused by another request
     */
    @Override
    public void requestDestroyed(ServletRequestEvent paramServletRequestEvent) {
        LOG.trace("Clear MDC");
        MDC.clear();
    }

    /**
     * Inject the necessary trackers into the MDC to use in the log4j loggers
     */
    @Override
    public void requestInitialized(ServletRequestEvent paramServletRequestEvent) {
        LOG.trace("Set MDC context for request");
        HttpServletRequest request = (HttpServletRequest) paramServletRequestEvent.getServletRequest();

        // generate a random requestId to correlate all logs from the same request
        String requestId = UUID.randomUUID().toString();
        MDC.put(MDC_KEY_REQUEST_ID, requestId);
        MDC.put(MDC_KEY_CLIENT_HOST, request.getRemoteAddr());

        HttpSession session = request.getSession(false);
        if (session != null) {
            MDC.put(MDC_KEY_SESSION_ID, session.getId());
        }
    }

}