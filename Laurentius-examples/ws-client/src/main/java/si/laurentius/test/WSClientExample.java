/*
* Copyright 2016, Supreme Court Republic of Slovenia 
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved by 
* the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
*
* https://joinup.ec.europa.eu/software/page/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis, WITHOUT 
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and  
* limitations under the Licence.
 */
package si.laurentius.test;


import java.io.File;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import org.apache.logging.log4j.LogManager;

import org.apache.logging.log4j.Logger;
import si.laurentius.GetInMailRequest;
import si.laurentius.GetInMailResponse;
import si.laurentius.InMailEventListRequest;
import si.laurentius.InMailEventListResponse;
import si.laurentius.InMailListRequest;
import si.laurentius.InMailListResponse;
import si.laurentius.Mailbox;
import si.laurentius.ModifOutActionCode;
import si.laurentius.ModifyActionCode;
import si.laurentius.ModifyInMailRequest;
import si.laurentius.ModifyInMailResponse;
import si.laurentius.ModifyOutMailRequest;
import si.laurentius.ModifyOutMailResponse;
import si.laurentius.OutMailEventListRequest;
import si.laurentius.OutMailEventListResponse;
import si.laurentius.OutMailListRequest;
import si.laurentius.OutMailListResponse;
import si.laurentius.SEDException_Exception;
import si.laurentius.SEDMailBoxWS;

import si.laurentius.SubmitMailRequest;
import si.laurentius.SubmitMailResponse;
import si.laurentius.control.Control;
import si.laurentius.inbox.mail.InMail;
import si.laurentius.outbox.event.OutEvent;
import si.laurentius.outbox.mail.OutMail;
import si.laurentius.outbox.payload.OutPart;
import si.laurentius.outbox.payload.OutPayload;
import si.laurentius.outbox.property.OutProperties;
import si.laurentius.outbox.property.OutProperty;



/**
 *
 * @author Jože Rihtaršič
 */
public class WSClientExample {

  public static final String APPL_ID = "appl_1";
  public static final String APPL_PASSWORD = "appl1234";

  public static final String MAILBOX_ADDRESS
          = "http://localhost:8080/laurentius-ws/mailbox?wsdl";

  public static final String DOMAIN = "mb-laurentius.si"; // CHANGE BOX DOMAIN!!!
  public static final String SENDER_BOX = "a.department@" + DOMAIN;
  public static final String RECEIVER_BOX = "b.department@" + DOMAIN;
  public static final String SERVICE = "DeliveryWithReceipt";
  public static final String ACTION = "Delivery";
  public static final String LOG_SECTION_SEPARATOR = "*****************************";

  public static final Logger LOG = LogManager.getLogger(WSClientExample.class);

  File[] testFiles;
  SEDMailBoxWS mTestInstance = null;

  public SEDMailBoxWS getService() {
    if (mTestInstance == null) {

      try {
        /*
        //-----------------------------------------
        // Authenticator method 1 for "fat client"
        // use HTTPHandlerBasicAuthenticator for "weblogic"
        Authenticator.setDefault(new Authenticator() {
          @Override
          protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(APPL_ID, APPL_PASSWORD.
                    toCharArray());
          }
        });*/

        Mailbox msb = new Mailbox(Mailbox.class.
                getResource("/wsdl/mailbox.wsdl")); // wsdl is in laurentius-wsdl.jar
        mTestInstance = msb.getSEDMailBoxWSPort();
        

        
        Map<String, Object> req_ctx = ((BindingProvider) mTestInstance).
                getRequestContext();
        req_ctx.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, MAILBOX_ADDRESS);

        //-----------------------------------------
        // Authenticator method 2 for "multithreaded env. as server"
        Binding binding = ((BindingProvider) mTestInstance).getBinding();
        List hchain = binding.getHandlerChain();
        if (hchain == null) {
          hchain = new ArrayList();
        }
        hchain.add(new HTTPHandlerBasicAuthenticator(APPL_ID, APPL_PASSWORD));
        binding.setHandlerChain(hchain);

      } catch (Exception pe) {
        LOG.error("Bad password or application account: " + pe.getMessage());
      }
    }
    return mTestInstance;

  }

  public static void main(String... args) throws InterruptedException {


    WSClientExample wc = new WSClientExample();

    try {
      // example submit mail
      BigInteger bi = wc.submitMail(createOutMail(RECEIVER_BOX, "Mr. Receiver",
              SENDER_BOX, "Mr. Sender",
              SERVICE,
              ACTION,
              "Test message",
              "VL 1/2016"));

      // example list all out mail
      List<OutMail> lstOm = wc.getOutMailList(SENDER_BOX);
      OutMail om = null;
      // get out mail data
      for (OutMail m : lstOm) {
        if (Objects.equals(m.getId(), bi)) {
          om = m;
          break;
        }
      }

      if (om == null) {
        LOG.error(
                "Message sent but no out message found for senderbox " + SENDER_BOX);
        return;
      }

      boolean msgSent = false;
      int iCnt = 0;
      while (!msgSent) {
        Thread.sleep(1000);
        // example list events for  out mail
        List<OutEvent> lstOE = wc.getOutMailEventList(bi, SENDER_BOX);
        for (OutEvent e : lstOE) {
          if (Objects.equals(e.getStatus(), "SENT")) {
            msgSent = true;
            break;
          }

        }
        if (++iCnt > 5) {
          LOG.error("Message is not sent in 5 seconds! - check url connection");
          return;
        }
      }

      // example modify status for out mail
      // because mail is sent - soapfault is returned!
      wc.modifyOutMail(bi, SENDER_BOX);

      //-----------------------------------------------------------------------
      // RECEIVED MAIL
      //-----------------------------------------------------------------------
      // example get in mail list
      // because "in process" in demo laurentius sets inmail status to DELIVERED - search is done by this status
      // else default is RECEIVED
      Thread.sleep(2000);
      List<InMail> lstIM = wc.getInMailList(RECEIVER_BOX, "RECEIVED");
      // search for corresponding in mail
      InMail im = null;
      for (InMail m : lstIM) {
        LOG.info(m.getSenderMessageId() + " - " + om.
                getSenderMessageId());
        if (Objects.equals(m.getSenderMessageId(), om.getSenderMessageId())) {
          im = m;
          break;
        }
      }
       if (im == null) {
          LOG.error("Message is not sent but no mail found in statu RECEIVED for " + RECEIVER_BOX);
          return;
        }

      // example get in mail
      wc.getInMail(im.getId(), RECEIVER_BOX);
      // example get in mail events
      wc.getInMailEventList(im.getId(), RECEIVER_BOX);

      Thread.sleep(1000);
      // example modify status for out mail
      wc.modifyInMail(im.getId(), RECEIVER_BOX);

    } catch (JAXBException | SEDException_Exception ex) {
      LOG.error("Error occured while executing sample", ex);
    }

  }

  public long getDeltaTime(long l) {
    return Calendar.getInstance().getTimeInMillis() - l;
  }

  /**
   * Sumit mail example
   *
   * @return
   * @throws SEDException_Exception
   */
  public BigInteger submitMail(OutMail om)
          throws SEDException_Exception, JAXBException {

    // create message
    SubmitMailRequest smr = new SubmitMailRequest();
    smr.setControl(createControl());
    smr.setData(new SubmitMailRequest.Data());

    smr.getData().setOutMail(om);

    // submit request
    LOG.info("submit message");
    
    SubmitMailResponse mr = getService().submitMail(smr);
    LOG.info(LOG_SECTION_SEPARATOR);
    LOG.info("got 'sumitMail' response:\n" + serialize(mr));
    LOG.info(LOG_SECTION_SEPARATOR);

    return mr.getRData() != null ? mr.getRData().getMailId() : null;
  }

  public List<OutMail> getOutMailList(String senderBox)
          throws SEDException_Exception, JAXBException {
    OutMailListRequest omlr = new OutMailListRequest();
    Control c = createControl();
    c.setResponseSize(BigInteger.valueOf(100));
    omlr.setControl(c);
    omlr.setData(new OutMailListRequest.Data());
    omlr.getData().setSenderEBox(senderBox);

    OutMailListResponse mlr = getService().getOutMailList(omlr);
    LOG.info(LOG_SECTION_SEPARATOR);
    LOG.info("Got 'getOutMailList' response:\n" + serialize(mlr));
    LOG.info(LOG_SECTION_SEPARATOR);
    return mlr.getRData().getOutMails();

  }

  public List<OutEvent> getOutMailEventList(BigInteger bi, String senderBox)
          throws SEDException_Exception, JAXBException {
    OutMailEventListRequest omelr = new OutMailEventListRequest();
    Control c = createControl();
    c.setResponseSize(BigInteger.valueOf(100));
    omelr.setControl(c);
    omelr.setData(new OutMailEventListRequest.Data());
    omelr.getData().setSenderEBox(senderBox);
    omelr.getData().setMailId(bi);
    LOG.info(LOG_SECTION_SEPARATOR);
    LOG.info("'getOutMailEventList' request:\n" + serialize(omelr));
    LOG.info(LOG_SECTION_SEPARATOR);
    OutMailEventListResponse mler = getService().getOutMailEventList(omelr);
    LOG.info(LOG_SECTION_SEPARATOR);
    LOG.info("Got 'getOutMailEventList' response:\n" + serialize(mler));
    LOG.info(LOG_SECTION_SEPARATOR);
    return mler.getRData().getOutEvents();

  }

  public void modifyOutMail(BigInteger bi, String senderBox)
          throws JAXBException {
    ModifyOutMailRequest req = new ModifyOutMailRequest();
    Control c = createControl();
    c.setResponseSize(BigInteger.valueOf(100));
    req.setControl(c);
    req.setData(new ModifyOutMailRequest.Data());
    req.getData().setSenderEBox(senderBox);
    req.getData().setMailId(bi);
    req.getData().setAction(ModifOutActionCode.DELETE);

    ModifyOutMailResponse res;
    try {
      LOG.info(LOG_SECTION_SEPARATOR);
      LOG.info("'getInMailList' request:\n" + serialize(req));
      LOG.info(LOG_SECTION_SEPARATOR);
      res = getService().modifyOutMail(req);
      LOG.info(LOG_SECTION_SEPARATOR);
      LOG.info("Got 'modifyOutMail' response:\n" + serialize(res));
      LOG.info(LOG_SECTION_SEPARATOR);
    } catch (SEDException_Exception ex) {
      LOG.info(LOG_SECTION_SEPARATOR);
      LOG.info("Got 'modifyOutMail' SEDException_Exception:\n" + serialize(ex.
              getFaultInfo()));
      LOG.info(LOG_SECTION_SEPARATOR);
    }
  }

  public List<InMail> getInMailList(String receiverBox, String status)
          throws SEDException_Exception, JAXBException {
    InMailListRequest req = new InMailListRequest();
    Control c = createControl();
    c.setResponseSize(BigInteger.valueOf(100));
    req.setControl(c);
    req.setData(new InMailListRequest.Data());
    req.getData().setReceiverEBox(receiverBox);
    req.getData().setStatus(status);

    LOG.info(LOG_SECTION_SEPARATOR);
    LOG.info("'getInMailList' request:\n" + serialize(req));
    LOG.info(LOG_SECTION_SEPARATOR);

    InMailListResponse mlr = getService().getInMailList(req);
    LOG.info(LOG_SECTION_SEPARATOR);
    LOG.info("Got 'getInMailList' response:\n" + serialize(mlr));
    LOG.info(LOG_SECTION_SEPARATOR);
    // return first mail id
    return mlr.getRData().getInMails();

  }

  public void getInMailEventList(BigInteger bi, String receiverBox)
          throws SEDException_Exception, JAXBException {
    InMailEventListRequest omelr = new InMailEventListRequest();
    Control c = createControl();
    c.setResponseSize(BigInteger.valueOf(100));
    omelr.setControl(c);
    omelr.setData(new InMailEventListRequest.Data());
    omelr.getData().setReceiverEBox(receiverBox);
    omelr.getData().setMailId(bi);

    InMailEventListResponse mler = getService().getInMailEventList(omelr);
    LOG.info(LOG_SECTION_SEPARATOR);
    LOG.info("Got 'getInMailEventList' response:\n" + serialize(mler));
    LOG.info(LOG_SECTION_SEPARATOR);

  }

  public void getInMail(BigInteger bi, String receiverBox)
          throws SEDException_Exception, JAXBException {
    GetInMailRequest reg = new GetInMailRequest();
    Control c = createControl();
    c.setResponseSize(BigInteger.valueOf(100));
    reg.setControl(c);
    reg.setData(new GetInMailRequest.Data());
    reg.getData().setReceiverEBox(receiverBox);
    reg.getData().setMailId(bi);

    GetInMailResponse mler = getService().getInMail(reg);
    LOG.info(LOG_SECTION_SEPARATOR);
    LOG.info("Got 'getInMail' response:\n" + serialize(mler));
    LOG.info(LOG_SECTION_SEPARATOR);

  }

  public void modifyInMail(BigInteger bi, String receiverBox)
          throws JAXBException {
    ModifyInMailRequest req = new ModifyInMailRequest();
    Control c = createControl();
    c.setResponseSize(BigInteger.valueOf(100));
    req.setControl(c);
    req.setData(new ModifyInMailRequest.Data());
    req.getData().setReceiverEBox(receiverBox);
    req.getData().setMailId(bi);
    req.getData().setAction(ModifyActionCode.DELETE);

    ModifyInMailResponse res;
    try {
      res = getService().modifyInMail(req);
      LOG.info(LOG_SECTION_SEPARATOR);
      LOG.info("Got response:\n" + serialize(res));
      LOG.info(LOG_SECTION_SEPARATOR);
    } catch (SEDException_Exception ex) {
      LOG.info("Got SEDException_Exception: " + serialize(ex.getFaultInfo()));
    }
  }

  public String serialize(Object o)
          throws JAXBException {

    StringWriter sw = new StringWriter();

    JAXBContext context = JAXBContext.newInstance(o.getClass());
    Marshaller m = context.createMarshaller();
    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    m.marshal(o, sw);

    return sw.toString();
  }

  private static OutMail createOutMail(String rcBox, String rcName,
          String sndBox,
          String sndName,
          String service, String action, String contentDesc,
          String oprst) {

    OutMail om = new OutMail();

    om.setSenderMessageId("SM_ID-" + UUID.randomUUID().toString());
    om.setConversationId(UUID.randomUUID().toString());
    om.setAction(action);
    om.setService(service);
    om.setReceiverName(rcName);
    om.setReceiverEBox(rcBox);
    om.setSenderName(sndName);
    om.setSenderEBox(sndBox);
    om.setSubject(oprst + " " + contentDesc);
    om.setOutProperties(new OutProperties());
    OutProperty opr = new OutProperty();
    opr.setName("oprst");
    opr.setValue(oprst);
    om.getOutProperties().getOutProperties().add(opr);

    om.setOutPayload(new OutPayload());

    OutPart op = new OutPart();
    op.setFilename("hello.txt");
    op.setDescription("This is test attachment");
    op.setBin("hello".getBytes());
    op.setMimeType("plain/text");
    om.getOutPayload().getOutParts().add(op);

    OutPart op1 = new OutPart();
    op1.setFilename("helloAgain.txt");
    op1.setDescription("This is second test attachment");
    op1.setBin("hello again".getBytes());
    op1.setMimeType("plain/text");
    om.getOutPayload().getOutParts().add(op1);

    return om;

  }

  private Control createControl() {

    Control c = new Control();
    c.setApplicationId(APPL_ID);
    c.setUserId("UserId"); /// user id is for log purposes
    return c;

  }
}
