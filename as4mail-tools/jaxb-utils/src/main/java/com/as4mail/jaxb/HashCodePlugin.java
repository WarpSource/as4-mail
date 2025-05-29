package com.as4mail.jaxb;

import com.sun.codemodel.*;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.Outline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class HashCodePlugin extends Plugin {
    public static final Logger LOG = LoggerFactory.getLogger(HashCodePlugin.class);

    @Override
    public String getOptionName() {
        return "jaxb-hashcode";
    }

    @Override
    public String getUsage() {
        return null;
    }

    @Override
    public boolean run(Outline outline, Options options, ErrorHandler errorHandler) throws SAXException {
        LOG.info("run outline" , outline);
        for (final ClassOutline classOutline : outline.getClasses()) {
            if (!isIgnored(classOutline)) {
                processClassOutline(classOutline);
            }
        }
        return true;

    }

    protected void processClassOutline(ClassOutline classOutline) {
        final JDefinedClass theClass = classOutline.implClass;

        generateEquals(classOutline, theClass);
    }

    protected void generateEquals(ClassOutline classOutline,
                                            final JDefinedClass theClass) {

        final JCodeModel codeModel = theClass.owner();
        final JMethod equals = theClass.method(JMod.PUBLIC, codeModel.INT,
                "hashCode");
        equals.annotate(Override.class);
        final JBlock body = equals.body();

        if (classOutline.getDeclaredFields()==null || classOutline.getDeclaredFields().length<1) {
            body._return(JExpr._super().invoke("hashCode"));
        } else {
            JInvocation invocation  = codeModel
                    .ref(Objects.class).staticInvoke("hash");
            Arrays.stream(classOutline.getDeclaredFields()).map(fieldOutline -> fieldOutline.getPropertyInfo().getName(
                    false)).map(fieldName -> JExpr._this().ref(fieldName)).forEachOrdered(invocation::arg);
            body._return(invocation);

        }
    }


    public boolean isIgnored(ClassOutline classOutline) {
        LOG.info("Ignore class: [{}]", classOutline);
        return false;
    }

    static private class MultiAndOp extends JExpressionImpl {

        List<JExpression> expressions;

        public void addExpression(JExpression expression){
            if (expressions == null){
                expressions = new ArrayList<>();
            }
            expressions.add(expression);
        }
        @Override
        public void generate(JFormatter f) {
            if (expressions == null || expressions.isEmpty()){
                return;
            }
            boolean notFirst = false;
            for (JExpression ex: expressions){
                if (notFirst){
                    f.p(" && ");
                } else {
                    notFirst =true;
                }
                f.g(ex);
            }
        }
        public boolean isEmpty(){
            return this.expressions==null || expressions.isEmpty();
        }
    }


}
