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
package si.laurentius.msh.web.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.inject.Named;
import javax.inject.Inject;
import javax.enterprise.context.SessionScoped;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.enums.SEDOutboxMailStatus;
import si.laurentius.commons.exception.PModeException;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.outbox.event.MSHOutEvent;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.laurentius.msh.pmode.Action;
import si.laurentius.msh.pmode.PMode;
import si.laurentius.msh.pmode.Service;
import si.laurentius.msh.table.mail.TableOutMail;
import si.laurentius.msh.web.abst.AbstractMailView;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@Named("OutMailDataView")
public class OutMailDataView extends AbstractMailView<TableOutMail, MSHOutMail, MSHOutEvent>
        implements
        Serializable {

    private static final SEDLogger LOG = new SEDLogger(OutMailDataView.class);

    private static final long serialVersionUID = 1L;

    @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
    SEDDaoInterface mDB;

    @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
    SEDLookupsInterface mLookup;
    @EJB(mappedName = SEDJNDI.JNDI_PMODE)
    PModeInterface mPMode;

    @Inject
    private UserSessionData userSessionData;

    /**
     *
     */
    public void deleteSelectedMail() {
        long l = LOG.logStart();
        if (getSelected() != null && !getSelected().isEmpty()) {
            List<TableOutMail> molst = getSelected();
            String userId = getUserSessionData().getUser().getUserId();
            String desc = "Manually deleted by " + userId;
            for (TableOutMail mo : molst) {
                try {
                    Date dt = mDB.setStatusToOutMail(mo.getId(),
                            mo.getSenderMessageId(),
                            mo.getSentDate(),
                            mo.getReceivedDate(),
                            mo.getDeliveredDate(),
                            SEDOutboxMailStatus.DELETED,
                            desc,
                            userId,
                            AppConstant.S_APPLICATION_CODE,
                            null, null);
                    // update values
                    mo.setStatusDate(dt);
                    mo.setStatus(SEDOutboxMailStatus.DELETED.getValue());

                } catch (StorageException ex) {
                    String mail = String.format(
                            "id: %d, sender: %s, receiver %s, service %s, action %s",
                            mo.getId(), mo.getSenderEBox(), mo.getReceiverEBox(), mo.
                            getService(), mo.getAction());
                    facesContext().addMessage(null, new FacesMessage(
                            "'Napaka pri brisanju pošiljke",
                            mail));
                    LOG.logError(l, ex);
                    break;
                }
            }

        }
        PrimeFaces.current().executeScript("PF('blockMainPanel').hide();");
        LOG.logEnd(l);
    }

    /**
     *
     * @param filePath
     * @return
     */
    @Override
    public StreamedContent getEventEvidenceFile(String filePath) {
        long l = LOG.logStart();
        File f = StorageUtils.getFile(filePath);
        if (f.exists()) {
            try {
                return new DefaultStreamedContent(new FileInputStream(f), MimeValue.
                        getMimeTypeByFileName(
                                f.getName()),
                        f.getName());
            } catch (FileNotFoundException ex) {
                LOG.logError(l, ex);
            }
        }
        LOG.formatedWarning("Event file '%s' not found ", filePath);
        return null;
    }

    /**
     *
     * @param bi
     * @return
     */
    @Override
    public StreamedContent getFile(BigInteger bi) {
        long l = LOG.logStart();
        MSHOutPart part = null;

        MSHOutMail mom = getCurrentMail();
        if (mom == null || mom.getMSHOutPayload() == null
                || mom.getMSHOutPayload().getMSHOutParts().isEmpty()) {
            return null;
        }

        for (MSHOutPart ip : mom.getMSHOutPayload().getMSHOutParts()) {
            if (ip.getId().equals(bi)) {
                part = ip;
                break;
            }
        }
        if (part != null) {
            try {
                File f = StorageUtils.getFile(part.getFilepath());
                return new DefaultStreamedContent(new FileInputStream(f), part.
                        getMimeType(),
                        part.getFilename());
            } catch (FileNotFoundException ex) {
                LOG.logError(l, ex);
                addError("File '" + part.getFilepath() + "' reading error: " + ex.getMessage());
            }
        }
        return null;
    }

    /**
     *
     * @return
     */
    public OutMailDataModel getOutMailModel() {
        if (mMailModel == null) {
            mMailModel = new OutMailDataModel(TableOutMail.class, getUserSessionData(),
                    mDB);
        }
        return (OutMailDataModel) mMailModel;
    }

    /**
     *
     * @return
     */
    public List<SEDOutboxMailStatus> getOutStatuses() {
        return Arrays.asList(SEDOutboxMailStatus.values());
    }

    /**
     *
     * @param status
     * @return
     */
    @Override
    public String getStatusColor(String status) {
        return SEDOutboxMailStatus.getColor(status);
    }

    /**
     *
     * @return
     */
    public UserSessionData getUserSessionData() {
        return this.userSessionData;
    }

    @PostConstruct
    private void init() {
        mMailModel = new OutMailDataModel(TableOutMail.class, getUserSessionData(),
                mDB);
    }

    /**
     *
     * @throws IOException
     */
    public void resendSelectedMail()
            throws IOException {
        long l = LOG.logStart();
        if (getSelected() != null && !getSelected().isEmpty()) {
            List<TableOutMail> molst = getSelected();
            LOG.formatedWarning("get mail count %d to submit", molst.size());
            int iResend = 0, iNotResend = 0, iError = 0;
            StringWriter err = new StringWriter();
            for (TableOutMail mo : molst) {
                if (SEDOutboxMailStatus.DELIVERED.getValue().equals(mo.getStatus())) {
                    iNotResend++;

                } else {
                    try {
                        MSHOutMail mshom = mDB.getMailById(MSHOutMail.class, mo.getId());

                        PMode pmd = mPMode.getPModeMSHOutMail(mshom);
                        int iPriority = pmd.getPriority() == null ? 4 : pmd.getPriority();
                        iPriority = iPriority > 9 ? 9 : (iPriority < 0 ? 0 : iPriority);

                        Date dt = mDB.sendOutMessage(mo.getId(), SEDOutboxMailStatus.SCHEDULE, 0, 0, iPriority,
                                AppConstant.S_APPLICATION_CODE,
                                userSessionData.getUser().getUserId());
                        mo.setStatusDate(dt);
                        mo.setStatus(SEDOutboxMailStatus.SCHEDULE.getValue());

                        iResend++;
                    } catch (StorageException | PModeException ex) {
                        String mail = String.format(
                                "id: %d, sender: %s, receiver %s, service %s, action %s\n",
                                mo.getId(), mo.getSenderEBox(), mo.getReceiverEBox(), mo.
                                getService(),
                                mo.getAction());
                        err.append(mail);
                        LOG.logError(l, ex);
                        iError++;
                    }
                }
            }
            String strVal = err.toString();
            if (strVal.length() > 500) {
                strVal = strVal.substring(0, 500);
            }

            String msg = String.format(
                    "Resend %d, not resend %s (wrong status), error %s\n", iResend,
                    iNotResend, iError);
            facesContext().addMessage(null, new FacesMessage(
                    iError > 0 ? FacesMessage.SEVERITY_ERROR
                            : FacesMessage.SEVERITY_INFO,
                    "'Resending mail action",
                    msg + strVal));

        } else {
            facesContext().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_WARN,
                    "'Resending mail action",
                    "No mail selected"));

        }
        PrimeFaces.current().executeScript("PF('blockMainPanel').hide();");
        LOG.logEnd(l);
    }

    /**
     *
     * @param messageBean
     */
    public void setUserSessionData(UserSessionData messageBean) {
        this.userSessionData = messageBean;
    }

    /**
     *
     */
    @Override
    public void updateCurrentMailData(TableOutMail tm) {
        if (tm != null) {
            mCurrentMail = mDB.getMailById(MSHOutMail.class, tm.getId());
            mlstCurrentMailEvents = mDB.getMailEventList(MSHOutEvent.class, tm.getId());

        } else {
            this.mCurrentMail = null;
            this.mlstCurrentMailEvents = null;
        }
    }

    public List<Action> getCurrentFilterServiceActionList() {
        if (getOutMailModel().getFilter() != null
                && !Utils.isEmptyString(getOutMailModel().getFilter().getService())) {
            String srvId = getOutMailModel().getFilter().getService();
            Service srv = mPMode.getServiceById(srvId);
            if (srv != null) {
                return srv.getActions();
            }
        }
        return Collections.emptyList();
    }

    public void addNewMail(MSHOutMail newOutMail) throws StorageException, PModeException {

        PMode pmd = mPMode.getPModeMSHOutMail(newOutMail);
        mDB.serializeOutMail(newOutMail, userSessionData.getUser().getUserId(),
                "Laurentius-web",
                pmd);
    }

}
