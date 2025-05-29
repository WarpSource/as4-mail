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
package si.jrc.msh.interceptor;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.phase.Phase;
import si.laurentius.commons.cxf.EBMSConstants;
import si.laurentius.commons.cxf.SoapUtils;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.enums.SEDInboxMailStatus;
import si.laurentius.commons.enums.SEDMailPartSource;
import si.laurentius.commons.enums.SEDOutboxMailStatus;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.xml.XMLUtils;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.inbox.payload.MSHInPart;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.outbox.payload.MSHOutPart;

import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.soap.SOAPPart;
import java.io.File;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public class EBMSOutPartPersisterInterceptor extends AbstractEBMSInterceptor {

    protected final SEDLogger LOG = new SEDLogger(EBMSOutPartPersisterInterceptor.class);

    public EBMSOutPartPersisterInterceptor() {
        // add after WSS4J 
        super(Phase.WRITE_ENDING);
    }

    /**
     * @param t
     */
    @Override
    public void handleFault(SoapMessage t) {
        // ignore
    }

    /**
     * @param message
     */
    @Override
    public void handleMessage(SoapMessage message) {
        long l = LOG.logStart(message);
        MSHOutMail outMail = SoapUtils.getMSHOutMail(message);
        MSHInMail inMail = SoapUtils.getMSHInMail(message);

        SOAPMessage request = message.getContent(SOAPMessage.class);

        try {
            MimeValue soapPartMime = MimeValue.MIME_XML;

            if (outMail != null) {

                File f = storeSoapPart(request.getSOAPPart(), soapPartMime, EBMSConstants.SOAP_PART_REQUEST_PREFIX);
                MSHOutPart p = new MSHOutPart();
                p.setIsSent(Boolean.TRUE);
                p.setIsReceived(Boolean.FALSE);
                p.setEbmsId(outMail.getMessageId());
                p.setMimeType(soapPartMime.getMimeType());
                p.setDescription("SOAP Part");
                p.setName("SOAPPart");
                p.setSource(SEDMailPartSource.EBMS.getValue());
                p.setFilename(f.getName());
                p.setFilepath(StorageUtils.getRelativePath(f));
                getDAO().addOutMailPayload(outMail, Collections.singletonList(p), SEDOutboxMailStatus.valueOf(outMail.getStatus()), "Request SOAP envelope", "", "");
            } else if (inMail != null) {
                File f = storeSoapPart(request.getSOAPPart(), soapPartMime, EBMSConstants.SOAP_PART_RESPONSE_PREFIX);
                MSHInPart p = new MSHInPart();
                p.setIsSent(Boolean.TRUE);
                p.setIsReceived(Boolean.FALSE);
                p.setEbmsId(inMail.getMessageId());
                p.setMimeType(soapPartMime.getMimeType());
                p.setDescription("SOAP Part");
                p.setName("SOAPPart");
                p.setSource(SEDMailPartSource.EBMS.getValue());
                p.setFilename(f.getName());
                p.setFilepath(StorageUtils.getRelativePath(f));

                getDAO().addInMailPayload(inMail, Collections.singletonList(p), SEDInboxMailStatus.valueOf(inMail.getStatus()), "Response soap envelope", "", "");
            }
        } catch (StorageException ex) {
            Logger.getLogger(EBMSOutInterceptor.class.getName()).log(Level.SEVERE, null, ex);
        }
        LOG.logEnd(l);
    }

    private File storeSoapPart(SOAPPart sp, MimeValue mv, String prefix) throws StorageException {
        return XMLUtils.serializeSoapPart(sp, prefix, mv);
    }

}
