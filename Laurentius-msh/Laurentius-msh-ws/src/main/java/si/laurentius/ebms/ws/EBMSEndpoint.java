package si.laurentius.ebms.ws;

import java.util.Collection;
import java.util.Date;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.jms.JMSException;
import javax.naming.NamingException;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.BindingType;
import jakarta.xml.ws.Provider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.ServiceMode;
import jakarta.xml.ws.WebServiceContext;
import jakarta.xml.ws.WebServiceProvider;
import jakarta.xml.ws.soap.SOAPBinding;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.jaxws.context.WrappedMessageContext;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.message.Message;

import si.jrc.msh.exception.EBMSErrorCode;
import si.jrc.msh.interceptor.EBMSOutFaultInterceptor;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.ebox.SEDBox;
import si.laurentius.commons.enums.SEDInboxMailStatus;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.cxf.SoapUtils;
import si.laurentius.commons.ebms.EBMSError;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.JMSManagerInterface;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.pmode.EBMSMessageContext;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.StringFormater;
import si.laurentius.commons.utils.Utils;

/**
 *
 * @author Jože Rihtaršič
 */
@WebServiceProvider(serviceName = "msh")
@ServiceMode(value = Service.Mode.MESSAGE)
@BindingType(SOAPBinding.SOAP12HTTP_BINDING)
@org.apache.cxf.interceptor.InInterceptors(interceptors = {
  "si.jrc.msh.interceptor.EBMSLogInInterceptor",
  "si.jrc.msh.interceptor.EBMSInInterceptor",
  "si.jrc.msh.interceptor.MSHPluginInInterceptor"})
@org.apache.cxf.interceptor.OutInterceptors(interceptors = {
  "si.jrc.msh.interceptor.EBMSLogOutInterceptor",
  "si.jrc.msh.interceptor.EBMSOutInterceptor",
  "si.jrc.msh.interceptor.MSHPluginOutInterceptor"
})
@org.apache.cxf.interceptor.OutFaultInterceptors(interceptors = {
  "si.jrc.msh.interceptor.EBMSLogOutInterceptor",
  "si.jrc.msh.interceptor.EBMSOutFaultInterceptor",
  "si.jrc.msh.interceptor.MSHPluginOutFaultInterceptor"})
@org.apache.cxf.interceptor.InFaultInterceptors(interceptors = {
  "si.jrc.msh.interceptor.EBMSLogInInterceptor",
  "si.jrc.msh.interceptor.EBMSInFaultInterceptor",
  "si.jrc.msh.interceptor.MSHPluginInFaultInterceptor"})
public class EBMSEndpoint implements Provider<SOAPMessage> {

  private static final SEDLogger LOG = new SEDLogger(EBMSEndpoint.class);

  @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
  SEDDaoInterface mDB;
  StringFormater msfFormat = new StringFormater();
  StorageUtils msuStorageUtils = new StorageUtils();
  @Resource
  WebServiceContext wsContext;

  @EJB(mappedName = SEDJNDI.JNDI_JMSMANAGER)
  JMSManagerInterface mJMS;

  /**
   *
   */
  public EBMSEndpoint() {

  }

  @Override
  public SOAPMessage invoke(SOAPMessage request) {
    long l = LOG.logStart();
    SOAPMessage response = null;
    try {
      // create empty response
      MessageFactory mf = MessageFactory.newInstance(
              SOAPConstants.SOAP_1_2_PROTOCOL);
      response = mf.createMessage();

      // Using this cxf specific code you can access the CXF Message and Exchange objects
      WrappedMessageContext wmc = (WrappedMessageContext) wsContext.
              getMessageContext();
      Message msg = wmc.getWrappedMessage();
      MSHInMail inmail = SoapUtils.getMSHInMail(msg);
      if (inmail == null) {
        String errmsg = "No inbox message";
        LOG.logError(l, errmsg, null);
        throw new EBMSError(EBMSErrorCode.ApplicationError,
                null,
                errmsg,
                SoapFault.FAULT_CODE_SERVER);
      }

      SEDBox sb = SoapUtils.getMSHInMailReceiverBox(msg);
      EBMSMessageContext mc = SoapUtils.getEBMSMessageInContext(msg);

      if (sb == null) {
        String errmsg = String.format(
                "Inbox message %s but no inbox found  for message: %s",
                inmail.getId(),
                inmail.getReceiverEBox());
        LOG.logError(l, errmsg, null);
        throw new EBMSError(EBMSErrorCode.ApplicationError,
                inmail.getMessageId(),
                errmsg,
                SoapFault.FAULT_CODE_SERVER);
      } else if ((mc.getPMode().getIsTest() == null || !mc.getPMode().
              getIsTest()) && Utils.isEmptyString(inmail.getStatus())) {
        serializeMail(inmail, msg.getAttachments(), sb);
      }

    } catch (SOAPException ex) {
      String errmsg = String.format(
              "SOAPException: %s", ex.getMessage());
      LOG.logError(l, errmsg, ex);
      throw new EBMSError(EBMSErrorCode.ApplicationError,
              null,
              errmsg,
              SoapFault.FAULT_CODE_SERVER);
    }
    LOG.logEnd(l);
    return response;
  }

  private void serializeMail(MSHInMail mail, Collection<Attachment> lstAttch,
          SEDBox sb) {
    long l = LOG.logStart();
    // prepare mail to persist
    Date dt = new Date();
    // set current status
    mail.setStatus(SEDInboxMailStatus.RECEIVED.getValue());
    mail.setStatusDate(dt);
    mail.setReceivedDate(dt);

    try {
      mDB.serializeInMail(mail, "Laurentius-msh-ws");
    } catch (StorageException ex) {
      String errmsg = "Internal error occured while serializing incomming mail.";
      LOG.logError(l, errmsg, ex);
      throw new EBMSError(EBMSErrorCode.ExternalPayloadError, mail.
              getMessageId(), errmsg,
              SoapFault.FAULT_CODE_CLIENT);
    }

    try {
      // --------------------
      // serialize data to db

      mDB.setStatusToInMail(mail, SEDInboxMailStatus.RECEIVED, null);
    } catch (StorageException ex) {
       String errmsg = "Error occured while receiving mail:'" + mail.
              getId() + "'!";
      LOG.logError(l, errmsg, ex);
      throw new EBMSError(EBMSErrorCode.ApplicationError, mail.
              getMessageId(), errmsg,
              SoapFault.FAULT_CODE_SERVER);
      
      
    }

    try {
      LOG.formatedlog("EXPORT MAIL %d", mail.getId().longValue());
      mJMS.exportInMail(mail.getId().longValue());
    } catch (NamingException | JMSException ex) {
      LOG.logError(l,
              "Error occured while submitting mail to export queue:'" + mail.
                      getId() + "'!",
              ex);
    }

    LOG.logEnd(l);
  }

}
