package si.laurentius.msh.dao;

import jakarta.persistence.criteria.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.laurentius.msh.dao.enums.FilterType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

import static si.laurentius.msh.dao.enums.FilterType.*;

public class QueryCriteriaBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(QueryCriteriaBuilder.class);

    private static final List<String> METHOD_NAME_PREFIXES = Arrays.asList("set", "is");
    CriteriaBuilder criteriaBuilder;
    Class targetClass;
    Object filterObject;
    String sortField = "id";
    String sortOrder = "desc";


    public QueryCriteriaBuilder(CriteriaBuilder criteriaBuilder, Class targetClass, Object filterObject) {
        this.criteriaBuilder = criteriaBuilder;
        this.targetClass = targetClass;
        this.filterObject = filterObject;
    }

    public String getSortField() {
        return sortField;
    }

    public QueryCriteriaBuilder sortField(String sortField) {
        this.sortField = sortField;
        return this;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public QueryCriteriaBuilder sortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * Return true if method is filter candidate method.
     * Only "getters" with modifiers public no-void, starts with get/is and have no method arguments
     *
     * @param filterMethod
     * @return boolean value indicating if method is filter method.
     */
    public boolean isFilterMethod(Method filterMethod) {
        return Modifier.isPublic(filterMethod.getModifiers()) && filterMethod.getParameterCount() == 0
                && !filterMethod.getReturnType().equals(Void.TYPE) && StringUtils.startsWithAny(filterMethod.getName(), "get", "is");

    }

    /**
     * get filter value for method.
     *
     * @param filterMethod fiter object method.
     * @return value get object value
     */
    public Object getFilterValue(Method filterMethod) {
        try {
            return filterMethod.invoke(filterObject);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            LOG.warn("Can not get filter value for filter the name [{}]. Check if filter object has suitable structure! Root Cause [{}]. ",
                    filterMethod.getName(), ExceptionUtils.getRootCauseMessage(ex));
        }
        return null;
    }

    /**
     * Method removes set or is from the given method's name and returns as example:
     * <p>
     * isValid -> Valid
     * getValue -> Value
     *
     * @param filterMethod
     * @return
     */
    public String getFieldNameFromMethod(Method filterMethod) {
        String methodName = filterMethod.getName();
        Optional<String> result = METHOD_NAME_PREFIXES.stream().filter(pref -> StringUtils.startsWith(methodName, pref)).findFirst();
        return result.isEmpty() ? methodName : StringUtils.removeStart(methodName, result.get());
    }

    /**
     * Method returns true if setter method exists for the given getter method
     * @param filterMethod
     * @return true if exists else false.
     */
    public boolean hasSetterMethodValue(Method filterMethod) {
        String filterName = getFieldNameFromMethod(filterMethod);
        Method method;
        try {
            method = filterObject.getClass().getMethod("set" + filterName, new Class[]{filterMethod.getReturnType()});
        } catch (NoSuchMethodException ex) {
            LOG.debug("Setter method for filter the name [{}] does not exist.",
                    filterMethod.getName(), ExceptionUtils.getRootCauseMessage(ex));
            // method does not have setter // ignore other methods
            return false;
        } catch (SecurityException ex) {
            LOG.warn("SecurityException occurred when accessing the filter the name [{}]!",
                    filterMethod.getName(), ExceptionUtils.getRootCauseMessage(ex));
            // method does not have setter // ignore other methods
            return false;
        }
        return method != null;
    }

    public <T> CriteriaQuery<T> build() {
        return buildSearchCriteria(false);
    }

    public CriteriaQuery<Long> buildForCount() {
        return buildSearchCriteria(true);
    }


    /**
     * @param <T>
     * @param forCount
     * @return
     */
    protected <T> CriteriaQuery<T> buildSearchCriteria(boolean forCount) {
        LOG.debug("Build search criteria");
        CriteriaQuery cq = forCount ? criteriaBuilder.createQuery(Long.class) : criteriaBuilder.createQuery(targetClass);
        Root<T> om = cq.from(targetClass);
        if (forCount) {
            cq.select(criteriaBuilder.count(om));
        } else if (sortField != null) {
            if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
                cq.orderBy(criteriaBuilder.asc(om.get(sortField)));
            } else {
                cq.orderBy(criteriaBuilder.desc(om.get(sortField)));
            }
        } else {
            cq.orderBy(criteriaBuilder.desc(om.get("id")));
        }

        // set order by
        if (filterObject == null) {
            return cq;
        }
        Class cls = filterObject.getClass();
        Method[] methodList = cls.getMethods();
        List<Predicate> lstPredicate =
                Arrays.stream(methodList).
                        filter(this::isFilterMethod)
                        .map(getterMethod -> generatePredicate(getterMethod, om))
                        .filter(Objects::nonNull).collect(Collectors.toList());

        if (!lstPredicate.isEmpty()) {
            Predicate[] tblPredicate = lstPredicate.stream().toArray(Predicate[]::new);
            cq.where(criteriaBuilder.and(tblPredicate));
        }
        return cq;
    }

    protected <T> Path getEntityFilterPath(Method getterMethod, FilterType filterType, Root<T> om) {
        String ormName = getFieldNameFromMethod(getterMethod);

        if (filterType.getMethodEndingLength() > 0) {
            int size = ormName.length() - filterType.getMethodEndingLength();
            ormName = StringUtils.substring(ormName, 0, size);
        }
        // all field names are in lower case
        ormName = StringUtils.uncapitalize(ormName);
        LOG.debug("Get ORM filed name name [{}] for method [{}] and filter type [{}]", ormName, getterMethod.getName(), filterType);
        return om.get(ormName);
    }

    protected <T> Predicate generatePredicate(Method getterMethod, Root<T> om) {

        // get return parameter
        Object searchValue = getFilterValue(getterMethod);
        if (searchValue == null) {
            LOG.debug("No search value for filter method name [{}]]. Skip filter method", getterMethod.getName());
            return null;
        }

        FilterType sp = getFilterTypeFromMethodName(getterMethod, searchValue);
        if (sp == TRANSIENT) {
            LOG.debug("Transient filter method name [{}]]. Skip filter method!", getterMethod.getName());
            return null;
        }
        Path entityFilterPath = getEntityFilterPath(getterMethod, sp, om);

        switch (sp) {
            case LIST_IN: {
                return ((List) searchValue).isEmpty() ? entityFilterPath.isNull() :
                        entityFilterPath.in(((List) searchValue).toArray());
            }
            case LIST_NOT_IN: {
                return criteriaBuilder.not(entityFilterPath.in(((List) searchValue).toArray()));
            }
            case INTERVAL_FROM: {
                return criteriaBuilder.greaterThanOrEqualTo(entityFilterPath, (Comparable) searchValue);
            }
            case INTERVAL_TO: {
                return criteriaBuilder.lessThan(entityFilterPath, (Comparable) searchValue);
            }
            case VALUE:
                return criteriaBuilder.equal(entityFilterPath, searchValue);
        }
        LOG.warn("The type for filter method name [{}]] is not supported! Skip filter method!", getterMethod.getName());
        return null;
    }

    protected FilterType getFilterTypeFromMethodName(Method getterMethod, Object searchValue) {

        String fieldName = getterMethod.getName();
        if (fieldName.endsWith(LIST_IN.getMethodEnding()) && searchValue instanceof List) {
            return LIST_IN;
        }

        if (fieldName.endsWith(LIST_IN.getMethodEnding()) && searchValue instanceof List) {
            return LIST_NOT_IN;
        }
        // following filter methods must have setter method, else skip the method
        if (!hasSetterMethodValue(getterMethod)) {
            return TRANSIENT;
        }
        if (fieldName.endsWith(INTERVAL_FROM.getMethodEnding()) && searchValue instanceof Comparable) {
            return INTERVAL_FROM;
        }
        if (fieldName.endsWith(INTERVAL_TO.getMethodEnding()) && searchValue instanceof Comparable) {
            return INTERVAL_TO;
        }
        // skip empty or null string values!
        if (searchValue instanceof String && StringUtils.isEmpty((String) searchValue)) {
            return TRANSIENT;
        }
        return FilterType.VALUE;
    }
}