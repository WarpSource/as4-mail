/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.laurentius.msh.web.abst;

import java.io.Serializable;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import org.primefaces.PrimeFaces;

/**
 *
 * @author Jože Rihtaršič
 */
public class AbstractJSFView implements Serializable {

    public static final String CB_PARA_SAVED = "saved";
    public static final String CB_PARA_REMOVED = "removed";
    public static final String CB_PARA_SUCCESS = "success";

    /**
     *
     * @return
     */
    protected ExternalContext externalContext() {
        return facesContext().getExternalContext();
    }

    /**
     *
     * @return
     */
    protected FacesContext facesContext() {
        return FacesContext.getCurrentInstance();
    }

    protected void addError(String desc) {
        facesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                        desc));

    }

    /**
     *
     * @return
     */
    public String getClientIP() {
        return ((HttpServletRequest) externalContext().getRequest()).
                getRemoteAddr();
    }

    public void addCallbackParam(String val, boolean bval) {
        PrimeFaces.current().ajax().addCallbackParam(val, bval);
    }

    public void update(String... ids) {
        for (String id : ids) {
            PrimeFaces.current().ajax().update(id);

        }
    }

    public void showDialog(String dlgVarName, String panelUpdateId) {
        executeScript("PF('"+dlgVarName+"').show();");
        update(panelUpdateId);
    }

    public void executeScript(String... scripts) {
        for (String script : scripts) {
            PrimeFaces.current().executeScript(script);

        }
    }
}
