/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.das.analytics.rest.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * This bean class represents the drill down information to perform drilldown operations
 * (search and count)
 */
@XmlRootElement(name = "drillDownInfo")
@XmlAccessorType(XmlAccessType.FIELD)
public class DrillDownRequestBean {

    @XmlElement(name = "tableName")
    private String tableName;
    @XmlElement(name = "categories", required = false)
    private List<DrillDownPathBean> categories;
    @XmlElement(name = "query")
    private String query;
    @XmlElement(name = "scoreFunction", required = false)
    private String scoreFunction;
    @XmlElement(name = "recordCount")
    private int recordCount;
    @XmlElement(name = "recordStart")
    private  int recordStart;
    @XmlElement(name = "ranges", required = false)
    private List<DrillDownRangeBean> ranges;
    @XmlElement(name = "rangeField", required = false)
    private String rangeField;
    @XmlElement(name = "columns", required = false)
    private List<String> columns;
    @XmlElement(name = "sortBy", required = false)
    private List<SortByFieldBean> sortBy;

    public String getTableName() {
        return tableName;
    }

    public List<DrillDownPathBean> getCategories() {
        if (categories == null) {
            return new ArrayList<>();
        }
        return categories;
    }

    public String getQuery() {
        return query;
    }

    public String getScoreFunction() {
        return scoreFunction;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public int getRecordStart() {
        return recordStart;
    }

    public List<DrillDownRangeBean> getRanges() {
        if (ranges == null) {
            return new ArrayList<>();
        }
        return ranges;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setCategories(List<DrillDownPathBean> categories) {
        this.categories = categories;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setScoreFunction(String scoreFunction) {
        this.scoreFunction = scoreFunction;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }

    public void setRecordStart(int recordStart) {
        this.recordStart = recordStart;
    }

    public void setRanges(List<DrillDownRangeBean> ranges) {
        this.ranges = ranges;
    }

    public void setRangeField(String rangeField) {
        this.rangeField = rangeField;
    }

    public String getRangeField() {
        return rangeField;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public void setSortByFields(List<SortByFieldBean> sortByFields) {
        this.sortBy = sortByFields;
    }
}
