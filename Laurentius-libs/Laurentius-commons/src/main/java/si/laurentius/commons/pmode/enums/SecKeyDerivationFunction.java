/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.commons.pmode.enums;

import java.util.Objects;

/**
 *
 * @author sluzba
 */
public enum SecKeyDerivationFunction {
  ECDH_ES("http://www.w3.org/2021/04/xmldsig-more#hkdf", "HMacKDF"),
  X25519("http://www.w3.org/2009/xmlenc11#ConcatKDF", "ConcatKDF"),
  ;

  String mstrVal;
  String mstrDesc;


  private SecKeyDerivationFunction(String val, String strDesc) {
    mstrVal = val;
    mstrDesc = strDesc;
  }

  /**
   *
   * @return
   */
  public String getValue() {

    return mstrVal;
  }

  /**
   *
   * @return
   */
  public String getDesc() {
    return mstrDesc;
  }

  public static SecKeyDerivationFunction getByValue(String value) {
    for (SecKeyDerivationFunction pr :values()) {
      if (Objects.equals(pr.getValue(), value)) {
        return pr;
      }
    }
    return null;

  }

}
