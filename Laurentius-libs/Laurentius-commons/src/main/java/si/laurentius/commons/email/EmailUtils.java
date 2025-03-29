package si.laurentius.commons.email;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import static java.nio.charset.Charset.forName;
import java.nio.charset.CharsetEncoder;
import static java.util.Calendar.getInstance;
import java.util.Date;
import static java.util.UUID.randomUUID;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.Address;
import static jakarta.mail.Message.RecipientType.CC;
import static jakarta.mail.Message.RecipientType.TO;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import static jakarta.mail.Transport.send;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import static jakarta.mail.internet.MimeUtility.encodeText;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * @author Jože Rihtaršič
 */
public class EmailUtils {

  private static final String S_MIME_TXT = "text/plain";

  static CharsetEncoder asciiEncoder = forName("US-ASCII").newEncoder();
  // private static final String S_OUTMAIL_ADDRESS = "nobody@sodisce.si";
  // do no use DCLogger -> cyclic dependecy..
  private static final Logger mlgLogger = LogManager.getLogger(EmailUtils.class.getName());

  private Address[] getAddresses(String strAddrString) throws AddressException,
      UnsupportedEncodingException {
    Address[] to = null;
    if (strAddrString == null || strAddrString.trim().isEmpty()) {
      return to;
    }
    String[] lstAdr = strAddrString.split(",");
    to = new InternetAddress[lstAdr.length];
    for (int i = 0; i < lstAdr.length; i++) {
      String adr = lstAdr[i].trim();
      if (asciiEncoder.canEncode(adr)) {
        to[i] = new InternetAddress(adr);
      } else { // suppose non asci char in name !!
        String[] lst = adr.split("<");
        if (lst.length == 2) {
          to[i] = new InternetAddress(lst[1].replaceAll(">", ""), lst[0].trim(), "UTF-8");
        }

      }
    }

    return to;

  }

  /**
   *
   * @param emailAddress
   * @param subject
   * @param body
   * @param mailConfig
   * @throws NamingException
   * @throws IOException
   */
  public void sendMailMessage(String emailAddress, String subject, String body, String mailConfig)
      throws MessagingException, NamingException, IOException {

    sendMailMessage(new EmailData(emailAddress, null, subject, body), mailConfig);

  }

  /**
   *
   * @param eml
   * @param mailConfig
   * @throws MessagingException
   * @throws IOException
   */
  public void sendMailMessage(EmailData eml, String mailConfig) throws MessagingException,
      NamingException, IOException {
    mlgLogger.info("EmailUtils.sendMailMessage: " + eml.toString());
    long l = getInstance().getTimeInMillis();
    Session session = (Session) new InitialContext().lookup(mailConfig);
    String emailid = "sed-" + randomUUID().toString();
    MimeMessage m = new EVIPMimeMessage(session, emailid);

    m.setContentID(emailid);
    m.setContentID(emailid);
    m.addHeader("sed-id", emailid);

    Address[] to = getAddresses(eml.getEmailAddresses());
    m.setFrom(new InternetAddress(eml.getEmailSenderAddress()));
    m.setSender(new InternetAddress(eml.getEmailSenderAddress()));
    m.setRecipients(TO, to);
    if (eml.getEmailCCAddresses() != null) {
      Address[] toCC = getAddresses(eml.getEmailCCAddresses());
      m.setRecipients(CC, toCC);
    }

    String subject = eml.getSubject();

    m.setSubject(subject.length() > 160 ? subject.substring(0, 160) : subject);
    m.setSentDate(new Date());
    Multipart multipart = new MimeMultipart();
    StringWriter swrBody = new StringWriter();
    swrBody.append(eml.getBody());

    MimeBodyPart messageBodyPart = new MimeBodyPart();
    messageBodyPart.setHeader("Content-Type", S_MIME_TXT + "; charset=\"utf-8\"");
    messageBodyPart.setContent(swrBody.toString(), S_MIME_TXT + "; charset=\"utf-8\"");
    multipart.addBodyPart(messageBodyPart);

    for (EmailAttachmentData d : eml.getAttachments()) {
      mlgLogger.info("EmailUtils.sendMailMessage: - add attachments doc  "
          + d.getFile().getAbsolutePath());
      MimeBodyPart messageattachmentPart = new MimeBodyPart();
      DataSource source = new FileDataSource(d.getFile());
      messageattachmentPart.setDataHandler(new DataHandler(source));
      messageattachmentPart.setFileName(encodeText(d.getFileName()));
      multipart.addBodyPart(messageattachmentPart);
    }
    // Put parts in message
    m.setContent(multipart);

    send(m);
    mlgLogger.info("EmailUtils.sendMailMessage: " + eml.toString() + " - END ( "
        + (getInstance().getTimeInMillis() - l) + " ms)");

  }

}
