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
public enum SecSignatureAlgorithm {
  RSA_SHA1("http://www.w3.org/2000/09/xmldsig#rsa-sha1", "RSA-SHA1"),
  RSA_SHA256("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", "RSA-SHA256"),
  RSA_SHA512("http://www.w3.org/2001/04/xmldsig-more#rsa-sha512", "RSA-SHA512"),
  ECDSA_SHA1("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha1", "SHA1withECDSA"),
  ECDSA_SHA224("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha224","SHA224withECDSA"),
  ECDSA_SHA256("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256","SHA256withECDSA"),
  ECDSA_SHA384("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha384","SHA384withECDSA"),
  ECDSA_SHA512("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha512","SHA512withECDSA"),
  ED25519("http://www.w3.org/2021/04/xmldsig-more#eddsa-ed25519","ED25519"),
  ED448("http://www.w3.org/2021/04/xmldsig-more#eddsa-ed448","ED448");


  String mstrVal;
  String mstrDesc;

  private SecSignatureAlgorithm(String val, String strDesc) {
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


  public static SecSignatureAlgorithm getByValue(String value){
    for(SecSignatureAlgorithm pr: values()){
      if (Objects.equals(pr.getValue(), value)){
        return pr;
      }
    }
    return null;
  
  }
  
}
