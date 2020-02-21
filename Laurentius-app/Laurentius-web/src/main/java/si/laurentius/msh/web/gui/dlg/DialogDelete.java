package si.laurentius.msh.web.gui.dlg;

import java.io.Serializable;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import org.primefaces.PrimeFaces;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.msh.web.abst.AbstractAdminJSFView;
import static si.laurentius.msh.web.abst.AbstractAdminJSFView.CB_PARA_REMOVED;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@Named("dialogDelete")
public class DialogDelete implements Serializable {

    private static final SEDLogger LOG = new SEDLogger(DialogDelete.class);

    AbstractAdminJSFView currentJSFView;
    String updateTarget;

    public AbstractAdminJSFView getCurrentJSFView() {
        return currentJSFView;
    }

    public void setCurrentJSFView(AbstractAdminJSFView currentJSFView, String update) {
        this.currentJSFView = currentJSFView;
        updateTarget = update;
    }

    public void removeSelectedRow() {
        if (currentJSFView != null) {
            boolean bSuc = currentJSFView.removeSelected();
            addCallbackParam(CB_PARA_REMOVED, bSuc);
        } else {
            LOG.logWarn("Remove selected row, but no view currentJSFView is setted!",
                    null);
        }
    }

    public String getSelectedDesc() {
        if (currentJSFView != null) {
            return currentJSFView.getSelectedDesc();
        }
        return null;
    }

    public boolean getIsSelectedTableRow() {
        return currentJSFView != null && currentJSFView.getSelected() != null;
    }

    public String getTargetTable() {
        return updateTarget;
    }

    public void addCallbackParam(String val, boolean bval) {
        PrimeFaces.current().ajax().addCallbackParam(val, bval);
    }

}
