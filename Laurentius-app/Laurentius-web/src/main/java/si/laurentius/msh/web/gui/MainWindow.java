package si.laurentius.msh.web.gui;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.inject.Named;
import org.primefaces.event.TabChangeEvent;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.msh.web.enums.GUIPanelName;

import java.io.Serializable;

/**
 * @author Jože Rihtaršič
 */
@SessionScoped
@Named("mainWindow")
public class MainWindow implements Serializable {

    private static final SEDLogger LOG = new SEDLogger(MainWindow.class);


    GUIPanelName mCurrentPanel = GUIPanelName.PANEL_INBOX;


    //String mstrWindowShow = AppConstant.S_PANEL_INBOX;
    int currentProgressVal = 0;
    String currentProgressLabel = "";

    int activeToolbarTabIndex = 0;


    /**
     * @param summary
     * @param detail
     */
    public void addMessage(String summary, String detail) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail);
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    /**
     * @return
     */
    public String currentPanel() {
        return mCurrentPanel != null ? mCurrentPanel.getCode() : "";
    }

    public void setCurrentPanel(String strVal) {
        LOG.formatedlog("Set current panel: %s", strVal);

        mCurrentPanel = GUIPanelName.valueOf(strVal);
        activeToolbarTabIndex = mCurrentPanel.getGroupIndex();

    }

    public void setCurrentPanelByIndex(int index) {
        LOG.formatedlog("Set current panel: %s", index);

        mCurrentPanel = GUIPanelName.getByGroupIndex(index);
        activeToolbarTabIndex = index;

    }


    public boolean isCurrentPanel(String gpn) {

        return gpn != null && mCurrentPanel != null && mCurrentPanel.getCode().equalsIgnoreCase(gpn);
    }

    public int getActiveToolbarTabIndex() {
        return activeToolbarTabIndex;
    }

    public void setActiveToolbarTabIndex(int ati) {
        this.activeToolbarTabIndex = ati;
    }


    /**
     * @param event
     */
    public void onToolbarButtonAction(ActionEvent event) {
        if (event != null) {
            String res = (String) event.getComponent().getAttributes().get("panel");
            setCurrentPanel(res);
        }
    }

    /**
     * @param event
     */
    public void onToolbarTabChange(TabChangeEvent event) {
        LOG.formatedWarning("Tab Changed ");
        if (event == null) {
            LOG.formatedWarning("Tab Changed event is NULL.");
            return;
        }
        if (event.getTab() != null) {
            setCurrentPanel(event.getTab().getId());
            LOG.formatedlog("Tab Changed %s.", event.getTab().getId());
        } else if (event.getIndex() > -1) {
            LOG.formatedlog("Tab Changed %s.", event.getIndex());
            setCurrentPanelByIndex(event.getIndex());
        } else {
            LOG.formatedWarning("Tab Changed event is not NULL, but can not define tab.");
        }
    }

    public int getCurrentProgressVal() {
        return currentProgressVal;
    }

    public void setCurrentProgressVal(int currentProgressVal) {
        this.currentProgressVal = currentProgressVal;
    }

    public String getCurrentProgressLabel() {
        return currentProgressLabel;
    }

    public void setCurrentProgressLabel(String currentProgressLabel) {
        this.currentProgressLabel = currentProgressLabel;
    }


}
