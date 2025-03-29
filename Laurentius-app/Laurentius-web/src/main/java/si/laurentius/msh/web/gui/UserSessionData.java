package si.laurentius.msh.web.gui;


/*
 * Copyright 2016, Supreme Court Republic of Slovenia
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */

import com.ctc.wstx.util.StringUtil;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TransferEvent;
import org.primefaces.event.UnselectEvent;
import si.laurentius.commons.SEDGUIConstants;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.msh.web.abst.AbstractJSFView;
import si.laurentius.msh.web.security.LoginManager;
import si.laurentius.plugin.def.Plugin;
import si.laurentius.user.SEDUser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jože Rihtaršič
 */
@SessionScoped
@Named("userSessionData")
public class UserSessionData extends AbstractJSFView {


    private static final SEDLogger LOG = new SEDLogger(UserSessionData.class);
    @Inject
    private LoginManager loginManager;
    private String mstrCurrentSEDBox;

    private String userTheme = "saga";
    private Map<String, String> themeMap;

    @PostConstruct
    public void init() {
        setThemeMapInit();
    }

    /**
     * @return
     */
    public String getCurrentSEDBox() {
        return mstrCurrentSEDBox == null && getUserEBoxes() != null && !getUserEBoxes().isEmpty() ? getUserEBoxes().get(0) : mstrCurrentSEDBox;
    }

    /**
     * @return
     */
    public LoginManager getLoginManager() {
        return loginManager;
    }

    /**
     * @return
     */
    public SEDUser getUser() {
        long l = LOG.logStart();
        FacesContext context = facesContext();
        ExternalContext externalContext = context.getExternalContext();
        SEDUser su = (SEDUser) externalContext.getSessionMap().get(SEDGUIConstants.SESSION_USER_VARIABLE_NAME);
        if (su == null) {
            try {
                loginManager.logout();
            } catch (IOException ex) {
                LOG.logError(l, ex);
            }
        }
        return su;
    }

    /**
     * @return
     */
    public List<String> getUserEBoxes() {
        List<String> lst = new ArrayList<>();
        SEDUser usr = getUser();
        if (usr != null) {
            getUser().getSEDBoxes().stream().forEach((sb) -> {
                lst.add(sb.getLocalBoxName());
            });
        }
        return lst;
    }

    public List<String> getUserEBoxesWithDomain() {
        List<String> lst = new ArrayList<>();
        SEDUser usr = getUser();
        if (usr != null) {
            getUser().getSEDBoxes().stream().forEach((sb) -> {
                lst.add(sb.getLocalBoxName() + "@" + SEDSystemProperties.getLocalDomain());
            });
        }
        return lst;
    }

    /**
     *
     */
    public void onReorder() {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "List Reordered", null));
    }

    /**
     * @param event
     */
    public void onSelect(SelectEvent event) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Item Selected", event.getObject().toString()));
    }

    /**
     * @param event
     */
    public void onTransfer(TransferEvent event) {
        /*
         * StringBuilder builder = new StringBuilder(); for(Object item : event.getItems()) {
         * builder.append(((Theme) item).getName()).append("<br />"); }
         *
         * FacesMessage msg = new FacesMessage(); msg.setSeverity(FacesMessage.SEVERITY_INFO);
         * msg.setSummary("Items Transferred"); msg.setDetail(builder.toString());
         *
         * FacesContext.getCurrentInstance().addMessage(null, msg);
         */
    }

    /**
     * @param event
     */
    public void onUnselect(UnselectEvent event) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Item Unselected", event.getObject().toString()));
    }

    /**
     * @param strCurrBox
     */
    public void setCurrentSEDBox(String strCurrBox) {
        mstrCurrentSEDBox = strCurrBox;
    }

    /**
     * @param loginManager
     */
    public void setLoginManager(LoginManager loginManager) {
        this.loginManager = loginManager;
    }

    public boolean showPluginForUser(Plugin plg) {

        return getUser().getAdminRole() || plg.getWebRoles().contains(SEDGUIConstants.ROLE_USER);
    }

    public String getUserTheme() {
        return userTheme;
    }

    public void setUserTheme(String userTheme) {
        this.userTheme = userTheme;
        LOG.log("Theme changed to ", userTheme);
    }

    public Map<String, String> getThemeMap() {
        return themeMap;
    }

    public void setThemeMapInit() {
        themeMap = new LinkedHashMap<>();
        themeMap.put("arya", "arya");
        themeMap.put("luna-amber", "luna-amber");
        themeMap.put("luna-blue", "luna-blue");
        themeMap.put("luna-green", "luna-green");
        themeMap.put("luna-pink", "luna-pink");
        themeMap.put("nova-colored", "nova-colored");
        themeMap.put("nova-dark", "nova-dark");
        themeMap.put("nova-light", "nova-light");
        themeMap.put("saga", "saga");
        themeMap.put("vela", "vela");
    }

    public void setThemeMap(Map<String, String> themeMap) {
        this.themeMap = themeMap;
    }

    public void themeSelectionChanged(ValueChangeEvent event) {

        String newTheme =  (String) event.getNewValue();
        LOG.log("theme changed", newTheme);
        if (StringUtils.equalsIgnoreCase(newTheme, userTheme)){
           return;
        }
        this.userTheme = newTheme;
    }

}
