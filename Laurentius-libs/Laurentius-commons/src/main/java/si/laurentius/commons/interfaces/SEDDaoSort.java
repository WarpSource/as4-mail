package si.laurentius.commons.interfaces;

public class SEDDaoSort {

    public static SEDDaoSort SORT_ID_DESC = new SEDDaoSort("Id", OrderType.DESC);
    public static SEDDaoSort SORT_ID_ASC = new SEDDaoSort("Id", OrderType.ASC);

    public enum OrderType {
        ASC,
        DESC;
    }
    String fieldName;
    OrderType orderType;

    public SEDDaoSort(String fieldName, OrderType orderType) {
        this.fieldName = fieldName;
        this.orderType = orderType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    /**
     * Method returns true if sort order is set to Ascending else it returns false.
     * @return
     */
    public boolean isAscending(){
        return orderType==OrderType.ASC;
    }
}
