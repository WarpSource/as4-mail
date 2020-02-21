package si.jrc.msh.plugin.tc.web.dlg;



import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import org.primefaces.PrimeFaces;
import si.laurentius.commons.utils.SEDLogger;


/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@Named( "dialogProgress")
public class DialogProgress implements Serializable{

  private static final SEDLogger LOG = new SEDLogger(DialogProgress.class);
  
  private static final String CB_PARAM_PROC_STOP="stopped";

 
  ProcessAbstract currentProcess;

  public ProcessAbstract getCurrentTestCase() {
    return currentProcess;
  }

  public void setProcess(ProcessAbstract currentJSFView) {
    this.currentProcess = currentJSFView;
  }

  public void stopExecuting() {
    if (currentProcess != null) {
      currentProcess.setStop(true);
      
      addCallbackParam(CB_PARAM_PROC_STOP, true);
    } else {
      LOG.logWarn("Remove selected row, but no view currentJSFView is setted!",
              null);
    }
  }

  
  
  public Integer getTestProgress() {
    return currentProcess!=null?currentProcess.getProgress():0;
  }
  
  

  public String getMessage() {
    return currentProcess != null? currentProcess.getProcessMessage():"";
  }
  
  public String getTitle() {
    return currentProcess != null? currentProcess.getProcessTitle():"";
  }
  
   public String getUpdateTarget() {
    return "";
  }


 
  public void addCallbackParam(String val, boolean bval) {
      PrimeFaces.current().ajax().addCallbackParam(val, bval);
  }
  

}
