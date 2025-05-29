package si.laurentius.msh.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a dedicated logger for logging the  security event as
 *  - invoke web-service method
 *  - authentication event
 *  - authorization event
 *
 *  The primary intention was to use Markers, but the current Wildfly 27 logging framework does not yet support them.
 *
 * @author Joze Rihtarsic
 * @since 2.1 (as4mail)
 */
public class SecurityLogger {
    private static final Logger LOG = LoggerFactory.getLogger(SecurityLogger.class);

    public  static Logger getLogger(){
        return LOG;
    }

}
