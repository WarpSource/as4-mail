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

import java.beans.Expression;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class EqualPlugin extends Plugin {
    public static final Logger LOG = LoggerFactory.getLogger(EqualPlugin.class);

    @Override
    public String getOptionName() {
        return "jaxb-equals";
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
        final JMethod equals = theClass.method(JMod.PUBLIC, codeModel.BOOLEAN,
                "equals");

        equals.annotate(Override.class);
        final JVar object = equals.param(Object.class, "object");
        final JBlock body = equals.body();
        boolean hasSupperClass = hasSuperClass(classOutline);

        MultiAndOp multiAndOp = new MultiAndOp();
        if (hasSupperClass){
            multiAndOp.addExpression(JExpr._super().invoke("equals").arg(object));
        }
        else {
            // if the same reference
            body._if(JExpr._this().eq(object))._then()._return(JExpr.TRUE);
            // is the same class type
            JExpression objectIsNull = object.eq(JExpr._null());
            JExpression notTheSameType = JExpr._this().invoke("getClass")
                    .ne(object.invoke("getClass"));
            body._if(JOp.cor(objectIsNull, notTheSameType))._then()
                    ._return(JExpr.FALSE);
        }

        JExpression castResult = JExpr.cast(theClass, object);
        JVar thatObject = null;
        for (FieldOutline fieldOutline : classOutline.getDeclaredFields()) {
            if (!isIgnored(fieldOutline)) {
                String fieldName = fieldOutline.getPropertyInfo().getName(
                        false);
                if (thatObject == null) {
                    thatObject = body.decl(theClass, "thatObject", castResult);
                }
                JInvocation arg = codeModel
                        .ref(Objects.class).staticInvoke("equals")
                        .arg(JExpr._this().ref(fieldName))
                        .arg(thatObject.ref(fieldName));
                multiAndOp.addExpression(arg);
            }
        }

        if (!multiAndOp.isEmpty()){
            body._return(multiAndOp);
        } else {
            body._return(JExpr.TRUE);
        }

    }
    public boolean isIgnored(FieldOutline fieldOutline) {
        boolean ignore =  fieldOutline.getRawType().isArray() ||  fieldOutline.getPropertyInfo().isCollection();
        LOG.debug("Ignore method: [{}], result [{}]", fieldOutline.getPropertyInfo().displayName(), ignore);
        return ignore;
    }

    public boolean hasSuperClass(ClassOutline classOutline) {
        return classOutline.getSuperClass()!=null;
    }

    public boolean isIgnored(ClassOutline classOutline) {
        LOG.info("Ignore class: [{}]", classOutline.getImplClass().fullName());
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
