package si.laurentius.msh.web.security;


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

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.inject.Named;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.laurentius.commons.SEDGUIConstants;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.msh.web.jsf.FacesContextTools;
import si.laurentius.user.SEDUser;

import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Jože Rihtaršič
 */
@Named
@SessionScoped
public class LoginManager implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(LoginManager.class);

    public static final String LOGIN_ERROR_INVALID_CREDENTIALS = "Invalid Username or Password!";
    public static final String LOGIN_ERROR_EMPTY_USERNAME = "Username must not be null or empty!";
    public static final String LOGIN_ERROR_EMPTY_PASSWORD = "Password must not be null or empty!";
    private static final String HOME_PAGE = "/";
    private static final String PAGE_AFTER_LOGOUT = HOME_PAGE; // Another good option is the login
    // page back again
    private String mstrUsername = "";
    private String mstrPassword = "";
    private String mstrForwardUrl;


    @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
    SEDLookupsInterface mSedLookup;


    /**
     * @return
     */
    public String getUsername() {
        return mstrUsername;
    }

    /**
     * @param username
     */
    public void setUsername(String username) {
        this.mstrUsername = username;
    }

    /**
     * @return
     */
    public String getPassword() {
        return mstrPassword;
    }

    /**
     * @param password
     */
    public void setPassword(String password) {
        this.mstrPassword = password;
    }

    /**
     *
     */
    @PostConstruct
    public void init() {
        this.mstrForwardUrl = extractRequestedUrlBeforeLogin();
        LOG.info("Base URL [{}].", mstrForwardUrl);
    }

    private String extractRequestedUrlBeforeLogin() {
        ExternalContext externalContext = FacesContextTools.externalContext();
        String requestedUrl =
                (String) externalContext.getRequestMap().get(RequestDispatcher.FORWARD_REQUEST_URI);
        if (StringUtils.isBlank(requestedUrl)) {
            return externalContext.getRequestContextPath() + HOME_PAGE;
        }
        String queryString =
                (String) externalContext.getRequestMap().get(RequestDispatcher.FORWARD_QUERY_STRING);
        return requestedUrl + (queryString == null ? "" : "?" + queryString);
    }


    /**
     * Performs username/password login.
     *
     * @throws IOException from {@link ExternalContext#redirect(String)}
     */
    public void login() throws IOException {
        String remoteHost = getClientIP();
        LOG.info("Login from [{}], username [{}]", remoteHost, mstrUsername);
        HttpServletRequest request = FacesContextTools.getContextRequest();

        if (StringUtils.isEmpty(mstrUsername)) {
            LOG.info("Login failed for [{}] due to empty username!", remoteHost);
            FacesContextTools.submitErrorMessage(LOGIN_ERROR_EMPTY_USERNAME);
            return;
        }

        if (StringUtils.isEmpty(mstrPassword)) {
            LOG.info("Login failed for [{}] and username [{}] due to empty password!", remoteHost, mstrUsername);
            FacesContextTools.submitErrorMessage(LOGIN_ERROR_EMPTY_PASSWORD);
            return;
        }
        try {
            String userName = getUsername().trim();
            request.login(userName, getPassword().trim());

            SEDUser user = mSedLookup.getSEDUserByUserId(userName);
            if (user == null) {
                LOG.info("Login failed for [{}] and username [{}]! Username is not authorized!", remoteHost, mstrUsername);
                FacesContextTools.submitErrorMessage(LOGIN_ERROR_INVALID_CREDENTIALS);
                FacesContextTools.invalidateRequestSession();
                return;
            }

            Date dCd = Calendar.getInstance().getTime();
            if (user.getActiveFromDate().after(dCd)
                    || (user.getActiveToDate() != null && user.getActiveToDate().before(dCd))) {
                LOG.info("Login failed for [{}] and username [{}]! Username is not active!", remoteHost, mstrUsername);
                FacesContextTools.submitErrorMessage(LOGIN_ERROR_INVALID_CREDENTIALS);
                FacesContextTools.invalidateRequestSession();
                return;
            }

            if (!request.isUserInRole(SEDGUIConstants.ROLE_ADMIN) && !request.isUserInRole(SEDGUIConstants.ROLE_USER)) {
                LOG.info("Login failed for [{}] and username [{}]! Username does not have roles: USER or ADMIN!", remoteHost, mstrUsername);
                FacesContextTools.submitErrorMessage(LOGIN_ERROR_INVALID_CREDENTIALS);
                FacesContextTools.invalidateRequestSession();
                return;
            }
            user.setAdminRole(request.isUserInRole(SEDGUIConstants.ROLE_ADMIN));
            ExternalContext externalContext = FacesContextTools.externalContext();
            externalContext.getSessionMap().put(SEDGUIConstants.SESSION_USER_VARIABLE_NAME, user);
            externalContext.redirect(mstrForwardUrl);
            LOG.info("User from [{}] and username [{}] is logged in!", remoteHost, mstrUsername);
        } catch (ServletException e) {
            LOG.info("Login failed for [{}] and username [{}]! and password verification failed [{}]!", remoteHost, mstrUsername, ExceptionUtils.getRootCauseMessage(e));
            FacesContextTools.submitErrorMessage(LOGIN_ERROR_INVALID_CREDENTIALS);
            FacesContextTools.invalidateRequestSession();
        }
    }

    /**
     * Invalidates the current session, effectively logging out the current user.
     *
     * @throws IOException from {@link ExternalContext#redirect(String)}
     */
    public void logout() throws IOException {
        String remoteHost = getClientIP();
        LOG.info("Logout from [{}], username [{}]", remoteHost, mstrUsername);
        FacesContextTools.invalidateRequestSession();
        ExternalContext externalContext = FacesContextTools.externalContext();
        try {
            if (externalContext.getRequest() instanceof HttpServletRequest) {
                HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
                request.logout();
            }
        } catch (ServletException ex) {
            LOG.info("Login failed for [{}] and username [{}]! and password verification failed [{}]!",
                    remoteHost, mstrUsername,
                    ExceptionUtils.getRootCauseMessage(ex));
            FacesContextTools.submitErrorMessage(LOGIN_ERROR_INVALID_CREDENTIALS);
            FacesContextTools.invalidateRequestSession();
        }
        externalContext.redirect(externalContext.getRequestContextPath() + PAGE_AFTER_LOGOUT);
    }

    /**
     * Makes the current logged in available through EL: #{loginManager.user}. Notice as the user is
     * also placed in the session map (), it also is available through #{user}.
     *
     * @return The currently logged in {@link User}, or {@code null} if no user is logged in.
     */
    public SEDUser getUser() {
        ExternalContext externalContext = FacesContextTools.externalContext();
        return (SEDUser) externalContext.getSessionMap()
                .get(SEDGUIConstants.SESSION_USER_VARIABLE_NAME);
    }

    /**
     * Verifies if there is a currently logged in user.
     *
     * @return {@code true} if there's a logged in {@link User}, {@code false} otherwise.
     */
    public boolean isUserLoggedIn() {
        return getUser() != null;
    }

    /**
     * Verifies if the currently logged in user, if exists, is in the given ROLE.
     *
     * @param role The ROLE to verify if the user has.
     * @return {@code true} if the user is logged in and has the given ROLE. {@code false} otherwise.
     */
    public boolean isUserInRole(String role) {
        ExternalContext externalContext = FacesContextTools.externalContext();
        return externalContext.isUserInRole(role);
    }

    /**
     * @return
     */
    public String getClientIP() {
        return FacesContextTools.getClientRequestHost();
    }

}
