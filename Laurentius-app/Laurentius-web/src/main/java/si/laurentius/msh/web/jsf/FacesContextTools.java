package si.laurentius.msh.web.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common java servlet faces utils.
 * Class contains most common static methods used for Faces context interaction.
 *
 * @author Joze Rihtarsic
 * @since 2.1
 */
public class FacesContextTools {
    private static final Logger LOG = LoggerFactory.getLogger(FacesContextTools.class);

    private FacesContextTools() {
    }

    public static ExternalContext externalContext() {
        return facesContext().getExternalContext();
    }

    public static FacesContext facesContext() {
        return FacesContext.getCurrentInstance();
    }

    public static String getClientRequestHost() {
        HttpServletRequest request = getContextRequest();
        return request == null ? null : request.getRemoteAddr();
    }

    public static HttpServletRequest getContextRequest() {
        ExternalContext externalContext = externalContext();
        if (externalContext == null) {
            LOG.warn("No external context. Return null for request");
            return null;
        }
        Object requestObject = externalContext().getRequest();
        if (requestObject instanceof HttpServletRequest) {
            return (HttpServletRequest) requestObject;
        }
        throw new ClassCastException("Can not cast request object [" + requestObject + "] to HttpServletRequest!");
    }

    public static void submitErrorMessage(String message) {
        submitMessage(null, FacesMessage.SEVERITY_ERROR, message, null);
    }

    public static void submitMessage(String clientId, FacesMessage.Severity severity, String message, String details) {
        facesContext().addMessage(clientId, new FacesMessage(severity, message, details));
    }

    public static void invalidateRequestSession() {
        ExternalContext externalContext = externalContext();
        if (externalContext == null) {
            return;
        }
        externalContext.invalidateSession();
    }
}
