package si.laurentius.msh.web.jsf;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.laurentius.msh.logging.SecurityLogger;

import java.io.IOException;

public class SecurityLoggerFilter extends HttpFilter {
    private static final Logger LOG = SecurityLogger.getLogger();
    @Override
    public void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        logSecurityEventOnRequest(req);
        //before request processing
        chain.doFilter(req, res);//calls other filters and processes request
        //after request processing
        logSecurityEventOnResponse(req, res);
    }
    public void logSecurityEventOnRequest(HttpServletRequest request){
        LOG.debug("Method [{}], path: [{}], host: [{}({})]",
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteHost(),request.getRemoteAddr());

    }

    public void logSecurityEventOnResponse(HttpServletRequest request, HttpServletResponse response){
        LOG.info("Method [{}], path: [{}], host: [{}({})], status [{}] ",
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteHost(),request.getRemoteAddr(),
                response.getStatus());

    }
}
