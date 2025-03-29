package si.laurentius.msh.dao.enums;

import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
/**
 * This enumeration defines the filter Type for querying the database.
 *
 * @author Joze Rihtarsic
 * @since 2.1 (as4mail)
 */
public enum FilterType {
    VALUE("", Arrays.asList(String.class, BigInteger.class)),
    INTERVAL_FROM("From", Collections.singletonList(Comparable.class)),
    INTERVAL_TO("To", Collections.singletonList(Comparable.class)),
    LIST_IN("List", Collections.singletonList(List.class)),
    LIST_NOT_IN("NotInList", Collections.singletonList(List.class)),
    TRANSIENT("", Collections.emptyList());

    String methodEnding;
    List<Class> classTypes;

    FilterType(String methodEnding, List<Class> classTypes) {
        this.methodEnding = methodEnding;
        this.classTypes = classTypes;
    }

    public String getMethodEnding() {
        return methodEnding;
    }

    public  int getMethodEndingLength() {
        return StringUtils.length(methodEnding);
    }

    public List<Class> getClassType() {
        return classTypes;
    }

    public boolean isType(String fieldName, Class paramClass) {
        return StringUtils.endsWith(fieldName, methodEnding) && classTypes.contains(paramClass);
    }
}

