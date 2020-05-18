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
package si.laurentius.msh.web.pmode;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.RowEditEvent;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.pmode.PartyIdentitySet;
import si.laurentius.msh.pmode.PartyIdentitySetType;
import si.laurentius.msh.pmode.Protocol;
import si.laurentius.commons.interfaces.SEDCertStoreInterface;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@Named("pModePartyView")
public class PModePartyView extends AbstractPModeJSFView<PartyIdentitySet> {

  /**
   *
   */
  public static SEDLogger LOG = new SEDLogger(PModePartyView.class);

  @EJB(mappedName = SEDJNDI.JNDI_PMODE)
  PModeInterface mPModeInteface;

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  SEDLookupsInterface mLookUp;

  @EJB(mappedName = SEDJNDI.JNDI_DBCERTSTORE)
  SEDCertStoreInterface mCertBean;

  PartyIdentitySetType.PartyId selectedPartyId;
  
  PartyIdentitySetType.PartyId editablePartyId;

  /**
   *
   */
  @PostConstruct
  public void init() {

  }

  /**
   *
   */
  @Override
  public void createEditable() {

    
    PartyIdentitySet pis = new PartyIdentitySet();
    pis.setLocalPartySecurity(
              new PartyIdentitySetType.LocalPartySecurity());
    pis.setExchangePartySecurity(
              new PartyIdentitySetType.ExchangePartySecurity());
    
    PartyIdentitySetType.PartyId piName = new PartyIdentitySetType.PartyId();
    piName.setType("urn:oasis:names:tc:ebcore:partyid-type:unregistered:si-svev:sed-name");
    piName.setValueSource("name");
    PartyIdentitySetType.PartyId piAddress = new PartyIdentitySetType.PartyId();
    piAddress.setType("urn:oasis:names:tc:ebcore:partyid-type:unregistered:si-svev:sed-box");
    piAddress.setValueSource("address");
    
    pis.getPartyIds().add(piName);
    pis.getPartyIds().add(piAddress);
    setNew(pis);

  }

  @Override
  public void startEditSelected() {
    if (getSelected() != null && getSelected().getLocalPartySecurity() == null) {
      getSelected().setLocalPartySecurity(
              new PartyIdentitySetType.LocalPartySecurity());
    }
    if (getSelected() != null && getSelected().getExchangePartySecurity() == null) {
      getSelected().setExchangePartySecurity(
              new PartyIdentitySetType.ExchangePartySecurity());
    }
    super.startEditSelected(); // To change body of generated methods, choose Tools | Templates.
  }

  /**
   *
   */
  @Override
  public boolean removeSelected() {
    boolean bSuc = false;
    PartyIdentitySet srv = getSelected();
    if (srv != null) {
      mPModeInteface.removePartyIdentitySet(srv);
      bSuc = true;
    }
    return bSuc;
  }

  @Override
  public boolean validateData() {
    PartyIdentitySet sv = getEditable();
    if (sv != null && isEditableNew()) {
      if (Utils.isEmptyString(sv.getId())) {
        facesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Id is empty!",
                        "Id must not be empty!"));
        return false;
      } else if (mPModeInteface.partyIdentitySetExists(sv.getId())) {
        facesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Id alredy exists!",
                        "Id must be unique!"));
        return false;
      } else if (!sv.isIsLocalIdentity() && Utils.isEmptyString(sv.getDomain())) {
        facesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Domain is empty!",
                        "For exchange party domain must not be empty!"));
        return false;
      } else if (sv.getPartyIds().isEmpty()) {
        facesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Identifier list empty",
                        "Define at least one PartyIdentifier"));
        return false;
      }

      return true;
    }
    return true;
  }

  /**
   *
   */
  @Override
  public boolean persistEditable() {
    long l = LOG.logStart();
    boolean bsuc = false;
    PartyIdentitySet sv = getEditable();
    if (sv != null) {
      mPModeInteface.addPartyIdentitySet(sv);
      setEditable(null);
      bsuc = true;
    }
    return bsuc;
  }

  /**
   *
   */
  @Override
  public boolean updateEditable() {
    boolean bsuc = false;

    PartyIdentitySet sv = getEditable();
    if (sv != null) {
      mPModeInteface.updatePartyIdentitySet(sv);
      setEditable(null);
      bsuc = true;
    }
    return bsuc;
  }

  /**
   *
   * @return
   */
  @Override
  public List<PartyIdentitySet> getList() {
    long l = LOG.logStart();
    List<PartyIdentitySet> lst = mPModeInteface.getPartyIdentitySets();
    LOG.logEnd(l);
    return lst;

  }
  
  
  public List<PartyIdentitySet> getLocalPartyList() {
    long l = LOG.logStart();
    List<PartyIdentitySet> lstLP = new ArrayList<>();

     List<PartyIdentitySet>  lst = mPModeInteface.getPartyIdentitySets();
    for (PartyIdentitySet lp: lst){
        if (lp.isIsLocalIdentity()){
          lstLP.add(lp);
        }
    }

    LOG.logEnd(l);
    return lstLP;

  }
  
    public void setUseFourCornerModel(boolean bVal) {
        if (getEditable() != null) {
            getEditable().setUseFourCornerModel(bVal);
        }
    }

    public boolean isUseFourCornerModel() {
        return getEditable() != null && getEditable().isUseFourCornerModel() != null ? getEditable().
                isUseFourCornerModel()
                : false;
    }

  public void setEditableIdentityActive(boolean bVal) {
    if (getEditable() != null) {
      getEditable().setActive(bVal);
    }
  }

  public boolean getEditableIdentityActive() {
    return getEditable() != null && getEditable().isActive() != null ? getEditable().
            isActive()
            : true;
  }

  public void setEditableLocalIdentity(boolean bVal) {
    if (getEditable() != null) {
      getEditable().setIsLocalIdentity(bVal);
    }
  }

  public boolean getEditableLocalIdentity() {
    return getEditable() != null ? getEditable().isIsLocalIdentity() : false;
  }

  public String getListAsString(List<String> lst) {
    return lst != null ? String.join(",", lst) : "";
  }

  public PartyIdentitySetType.TransportProtocol getCurrentTransportProtocol() {
    if (getEditable() != null) {
      if (getEditable().getTransportProtocols().isEmpty()) {
        PartyIdentitySetType.TransportProtocol pt = new PartyIdentitySetType.TransportProtocol();
        pt.setId("default");
        getEditable().getTransportProtocols().add(pt);
      }
      return getEditable().getTransportProtocols().get(0);
    }
    return null;
  }



  public PartyIdentitySetType.LocalPartySecurity getCurrrentLocalPartySecurity() {
    if (getEditable() != null) {
      if (getEditable().getLocalPartySecurity() == null) {
        getEditable().setLocalPartySecurity(
                new PartyIdentitySetType.LocalPartySecurity());
      }
      return getEditable().getLocalPartySecurity();
    }
    return null;
  }

  public PartyIdentitySetType.ExchangePartySecurity getCurrrentExchangePartySecurity() {
    if (getEditable() != null) {
      if (getEditable().getExchangePartySecurity() == null) {
        getEditable().setExchangePartySecurity(
                new PartyIdentitySetType.ExchangePartySecurity());
      }
      return getEditable().getExchangePartySecurity();
    }
    return null;
  }

  public void createPartyId() {
    if (getEditable() != null) {
      PartyIdentitySetType.PartyId ps = new PartyIdentitySetType.PartyId();
      ps.setType("type");
      ps.setValueSource("address");
      getEditable().getPartyIds().add(ps);
    }
  }

  public void removeSelectedPartyId() {
     if (getEditable()!= null && getSelectedPartyId() != null) {
       getEditable().getPartyIds().remove(getSelectedPartyId());
    }

  }

  public void onCellEditTableComplete(CellEditEvent cee) {
    
    LOG.formatedlog("Cell edit rindex %d, row key %s, new val %s ", cee.
            getRowIndex(),
            cee.getRowKey(), cee.getNewValue());

  }

  public PartyIdentitySetType.PartyId getSelectedPartyId() {
    return selectedPartyId;
  }

  public void setSelectedPartyId(PartyIdentitySetType.PartyId spi) {
    LOG.formatedlog("Select partyId: %d", getPartyIdIndex(spi));
    
    this.selectedPartyId = spi;
  }

  public int getPartyIdIndex(PartyIdentitySetType.PartyId pi) {
    if (getEditable() != null && pi != null && getEditable().getPartyIds().
            contains(pi)) {
      return getEditable().getPartyIds().indexOf(pi);
    }
    return -1;
  }

  @Override
  public String getSelectedDesc() {
    if (getSelected() != null) {
      return getSelected().getId();
    }
    return null;
  }
  
  public String getIdentifersAsString(PartyIdentitySetType.PartyId pi){    
    return pi!=null && pi.getIdentifiers()!= null?String.join(",", pi.getIdentifiers()):"";
  }
  
  public String getCurrentIdentifersAsString(){    
     return getSelectedPartyId()!=null? 
             getIdentifersAsString(getSelectedPartyId()):null;
    
  }
  
  public void setCurrentIdentifersAsString(String val){    
    PartyIdentitySetType.PartyId pi =  getSelectedPartyId();
     if (pi!=null){
        pi.getIdentifiers().clear();
        String[] lst = val.split(",");
        for (String i: lst){
          String idt =  i.trim();
          if (!idt.isEmpty()){
            pi.getIdentifiers().add(idt);
          }
        }
     }
    
  }
   public void onRowEdit(RowEditEvent event) {
     PartyIdentitySetType.PartyId pi = ((PartyIdentitySetType.PartyId) event.getObject());
     
    }
     
    public void onRowCancel(RowEditEvent event) {
      PartyIdentitySetType.PartyId pi = ((PartyIdentitySetType.PartyId) event.getObject());
    }
  

}
