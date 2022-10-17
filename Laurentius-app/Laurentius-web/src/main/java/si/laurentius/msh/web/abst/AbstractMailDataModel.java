/*
 * Copyright 2016, Supreme Court Republic of Slovenia
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package si.laurentius.msh.web.abst;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.interfaces.SEDDaoSort;
import si.laurentius.msh.web.gui.UserSessionData;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @param <T>
 * @author Jože Rihtaršič
 */
public abstract class AbstractMailDataModel<T> extends LazyDataModel<T> {

    /**
     *
     */
    protected static final long serialVersionUID = 1L;
    SEDDaoInterface mDB;

    /**
     *
     */
    protected List<T> mDataList;

    /**
     *
     */
    protected UserSessionData messageBean;

    /**
     *
     */
    protected final Class<T> type;

    /**
     * @param type
     */
    public AbstractMailDataModel(Class<T> type) {
        this.type = type;
    }

    /**
     * @return
     */
    abstract public Object externalFilters();

    /**
     * @return
     */
    public List<T> getCurrentData() {
        return mDataList;
    }

    /**
     * @param startingAt
     * @param maxPerPage
     * @param sortMap
     * @param filters
     * @return
     */
    public List<T> getData(int startingAt, int maxPerPage, Map<String, SortMeta> sortMap, Object filters) {

        List<SEDDaoSort> sedDaoSortList = null;
        if (sortMap != null && !sortMap.isEmpty()) {
            // sort order by priority and convert it to internal sort list
            List<SortMeta> sortValues = new ArrayList<>(sortMap.values());
            Collections.sort(sortValues, Comparator.comparingInt(SortMeta::getPriority));
            sedDaoSortList = sortValues.stream()
                    .map(sortMeta ->
                            sortMeta.getOrder() == SortOrder.UNSORTED ? null : new SEDDaoSort(sortMeta.getField(),
                                    sortMeta.getOrder() == SortOrder.ASCENDING ? SEDDaoSort.OrderType.ASC : SEDDaoSort.OrderType.DESC))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return mDB.getDataList(type, startingAt, maxPerPage, sedDaoSortList, filters);
    }

    /**
     * @param startingAt
     * @param maxPerPage
     * @return
     */
    public List<T> getData(int startingAt, int maxPerPage) {
        return mDB.getDataList(type, startingAt, maxPerPage, Collections.singletonList(SEDDaoSort.SORT_ID_DESC), externalFilters());
    }

    /**
     * @return
     */
    public Class<T> getType() {
        return type;
    }

    /**
     * @return
     */
    public UserSessionData getUserSessionData() {
        return this.messageBean;
    }


    /**
     * @param startingAt
     * @param maxPerPage
     * @param filters
     * @return
     */
    @Override
    public List<T> load(int startingAt, int maxPerPage, Map<String, SortMeta> sortMap, Map<String, FilterMeta> filters) {
        // validate data
        Object filterObject = externalFilters();
        mDataList = getData(startingAt, maxPerPage, sortMap, filterObject);
        setPageSize(maxPerPage);
        return mDataList;
    }

    // must provide the setter method

    /**
     * @param messageBean
     * @param db
     */
    public void setUserSessionData(UserSessionData messageBean, SEDDaoInterface db) {
        mDB = db;
        this.messageBean = messageBean;
    }

    @Override
    public int count(Map<String, FilterMeta> map) {
        Object filterObject = externalFilters();
        return Math.toIntExact(mDB.getDataListCount(type, filterObject));
    }
}
