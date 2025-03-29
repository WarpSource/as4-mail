/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.ejb;

import java.util.concurrent.atomic.AtomicBoolean;
import jakarta.ejb.AccessTimeout;
import jakarta.ejb.Local;
import jakarta.ejb.Lock;
import static jakarta.ejb.LockType.READ;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import si.laurentius.commons.interfaces.SEDNetworkUtilsInterface;
import si.laurentius.commons.utils.SEDLogger;

/**
 *
 * @author sluzba
 */
@Startup
@Singleton
@Local(SEDNetworkUtilsInterface.class)
@AccessTimeout(value = 60000)
public class SEDNetworkUtilsBean implements SEDNetworkUtilsInterface {

  protected static final SEDLogger LOG = new SEDLogger(SEDNetworkUtilsBean.class);

  private final AtomicBoolean networkOn = new AtomicBoolean(false);
  private final AtomicBoolean internetOn = new AtomicBoolean(false);

  @Override
  @Lock(READ)
  public Boolean isConnectedToNetwork() {
    return networkOn.get();
  }

  @Override
  @Lock(READ)
  public Boolean isConnectedToInternet() {
    return internetOn.get();
  }

  public void setConnectedToNetwork(boolean bVal) {
    networkOn.set(bVal);
  }

  public void setConnectedToInternet(boolean bVal) {
    internetOn.set(bVal);
  }
}
