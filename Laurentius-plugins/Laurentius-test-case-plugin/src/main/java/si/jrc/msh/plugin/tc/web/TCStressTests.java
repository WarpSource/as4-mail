/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.jrc.msh.plugin.tc.web;

import java.io.Serializable;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import org.primefaces.PrimeFaces;
import si.jrc.msh.plugin.tc.web.dlg.DialogProgress;
import si.jrc.msh.plugin.tc.web.tc.ProcessLAOM;
import si.jrc.msh.plugin.tc.web.tc.ProcessLM;
import si.laurentius.commons.utils.SEDLogger;

@SessionScoped
@Named("tcStressTests")
public class TCStressTests extends TestCaseAbstract implements Serializable {

    private static final SEDLogger LOG = new SEDLogger(TCStressTests.class);

    private final ProcessLAOM testLAOM = new ProcessLAOM(this);
    private final ProcessLM testLM = new ProcessLM(this);

    public ProcessLAOM getTestLAOM() {
        return testLAOM;
    }

    public ProcessLM getTestLM() {
        return testLM;
    }

    public void executeLAOM() {

        // show progress dialog
        DialogProgress dlg = getDlgProgress();
        dlg.setProcess(testLAOM);
        PrimeFaces context = PrimeFaces.current();
        context.ajax().update(":dlgProgress:dlgProgressForm:pnlProgress");
        context.executeScript("PF('dlgPrgBar').start();");
        context.executeScript("PF('dialogProgress').show();");
        testLAOM.executeStressTest();
    }

    public void executeLM() {
        testLM.setProcessMessage("");
        testLM.setProgress(0);
        if (!validateData()) {
            return;
        }

        // show progress dialog
        DialogProgress dlg = getDlgProgress();
        dlg.setProcess(testLM);
        PrimeFaces context = PrimeFaces.current();
        context.executeScript("PF('dlgPrgBar').start();");
        context.executeScript("PF('dialogProgress').show();");
        context.ajax().update(":dlgProgress:dlgProgressForm:pnlProgress");
        testLM.executeStressTest();
    }

}
