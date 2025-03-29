/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.msh.web.gui.menu;

import java.io.Serializable;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.enterprise.context.SessionScoped;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.interfaces.SEDPluginManagerInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.web.gui.AppConstant;
import si.laurentius.msh.web.gui.MainWindow;
import si.laurentius.plugin.def.Plugin;

@Named("settingsMenuView")
@SessionScoped
public class SettingsMenuView implements Serializable {

  private static final SEDLogger LOG = new SEDLogger(SettingsMenuView.class);

  @Inject
  private MainWindow mainWindow;

  @EJB(mappedName = SEDJNDI.JNDI_PLUGIN)
  private SEDPluginManagerInterface mPluginManager;

  private TreeNode root;

  private TreeNode selectedNode;

  @PostConstruct
  public void init() {
      long l= LOG.getTime();
      root = createMenu();
      LOG.logEnd(l, "Menu is initialized");
  }

  public MainWindow getMainWindow() {
    return mainWindow;
  }

  public void setMainWindow(MainWindow mainWindow) {
    this.mainWindow = mainWindow;
  }

  public TreeNode getRoot() {
    return root;
  }

  public TreeNode getSelectedNode() {
    return selectedNode;
  }

  public void setSelectedNode(TreeNode selectedNode) {
    this.selectedNode = selectedNode;
  }

  public void onSelection() {
    if (selectedNode != null) {
      mainWindow.setCurrentPanel(((MenuItem) selectedNode.getData()).getType());
    }
  }
  
  private TreeNode createMenuItem(TreeNode parentItem, String code, String title, String iconCssName) {
      TreeNode item = new DefaultTreeNode(new MenuItem(title,
              code,
              iconCssName + " ui-icon-size-22"), parentItem);
      return item;
  }

  public TreeNode createMenu() {
    long l = LOG.logStart();  
    TreeNode privateRoot = createMenuItem(null, "ROOT", "Settings Menu", "");
           
    createMenuItem(privateRoot, AppConstant.S_PANEL_SETT_CUSTOM, "Custom", "ui-icon-svg-settings");
    createMenuItem(privateRoot, AppConstant.S_PANEL_ADMIN_EBOXES, "SED-Boxes", "ui-icon-svg-box");
    createMenuItem(privateRoot, AppConstant.S_PANEL_ADMIN_USERS, "Users", "ui-icon-svg-users");
    createMenuItem(privateRoot, AppConstant.S_PANEL_ADMIN_APPL, "Applications", "ui-icon-svg-cms");
    
    TreeNode certs = createMenuItem(privateRoot, AppConstant.S_PANEL_SETT_CERTS, "Certificates", "ui-icon-svg-certificate");
    certs.setExpanded(true);
    createMenuItem(certs, AppConstant.S_PANEL_SETT_CERTS, "Keystore", "ui-icon-svg-key");
    createMenuItem(certs, AppConstant.S_PANEL_SETT_CERT_ROOT_CA, "RootCA", "ui-icon-svg-certificate");
    createMenuItem(certs, AppConstant.S_PANEL_SETT_CERT_CRL, "CRL", "ui-icon-svg-crl");
    
    //- PMODES
    TreeNode pmodeSettings = createMenuItem(privateRoot, AppConstant.S_PANEL_SETT_PMODE, "SettingsPMode", "ui-icon-svg-pmode");
    pmodeSettings.setExpanded(true);
    createMenuItem(pmodeSettings, AppConstant.S_PANEL_SETT_PMODE_SERVICES, "PModeServiceDefinitions", "ui-icon-svg-service");   
    createMenuItem(pmodeSettings, AppConstant.S_PANEL_SETT_PMODE_PARTIES, "Parties", "ui-icon-svg-party");
    createMenuItem(pmodeSettings, AppConstant.S_PANEL_SETT_PMODE_SECURITIES, "SecurityPolicies", "ui-icon-svg-security");
    createMenuItem(pmodeSettings, AppConstant.S_PANEL_SETT_PMODE_AS4_RA, "ReceptionAwarenessPatterns", "ui-icon-svg-reliability");
    createMenuItem(pmodeSettings, AppConstant.S_PANEL_SETT_PMODE, "PModes", "ui-icon-svg-pmode");

    //- Plugins
    TreeNode addons = createMenuItem(privateRoot, AppConstant.S_PANEL_ADMIN_PLUGIN, "Plugins", "ui-icon-svg-plugin");
    addons.setExpanded(true);
    TreeNode interceptors = createMenuItem(addons, AppConstant.S_PANEL_INTERCEPTOR, "Interceptors", "ui-icon-svg-interceptor");
    interceptors.setExpanded(true);
    TreeNode inmail = createMenuItem(addons, AppConstant.S_PANEL_INMAIL_PROCESS, "InMailRules", "ui-icon-svg-process");
    inmail.setExpanded(true);
    TreeNode crontask = createMenuItem(addons, AppConstant.S_PANEL_ADMIN_CRON, "Scheduler", "ui-icon-svg-cron-exec");
    crontask.setExpanded(true);
 
    for (Plugin p : mPluginManager.getRegistredPlugins()) {
      if (!Utils.isEmptyString(p.getWebContext()) && p.getProcessMenu() != null) {
          p.getProcessMenu().
                  getMenuItems().forEach((pmi) -> {
                      new DefaultTreeNode(
                              new MenuItem(pmi.getName(), AppConstant.S_SETTINGS_PLUGIN,
                                      "ui-icon-svg-plugin ui-icon-size-16", String.format(
                                              "%s?page=%s&navigator=false", p.
                                                      getWebContext(), pmi.getPageId())),
                              inmail);
          });
      }
    }
    LOG.logEnd(l);
    return privateRoot;
  }

  public String getSelectedWebContext() {
    if (selectedNode != null) {
      return ((MenuItem) selectedNode.getData()).getWebUrl();
    }
    return null;
  }
}
