/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.laurentius.plg.web;

import java.io.Serializable;
import java.util.Locale;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@Named("LocaleManager")
public class LocaleManager implements Serializable{

  private Locale locale;

  /**
   *
   * @return
   */
  public String getLanguage() {
    return locale.getLanguage();
  }

  /**
   *
   * @return
   */
  public Locale getLocale() {
    return locale;
  }

  /**
     *
     */
  @PostConstruct
  public void init() {
    locale = FacesContext.getCurrentInstance().getExternalContext().getRequestLocale();
  }

  /**
   *
   * @param language
   */
  public void setLanguage(String language) {
    locale = new Locale(language);
    FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
  }

}
