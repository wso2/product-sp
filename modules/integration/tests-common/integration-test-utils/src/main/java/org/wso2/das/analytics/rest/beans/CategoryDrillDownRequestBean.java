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

/**
 * This class contains the details about category drilldown. This class is used  as an input to get
 * the subcategories of a facet field when using drillDownCategories API
 */
@XmlRootElement(name = "categoryDrillDownRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class CategoryDrillDownRequestBean {

    @XmlElement(name = "tableName")
    private String tableName;
    @XmlElement(name = "fieldName")
    private String fieldName;
    @XmlElement(name = "categoryPath", required = false)
    private String[] categoryPath;
    @XmlElement(name = "query", required = false)
    private String query;
    @XmlElement(name = "scoreFunction", required = false)
    private String scoreFunction;

    public String getTableName() {
        return tableName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getQuery() {
        return query;
    }

    public String getScoreFunction() {
        return scoreFunction;
    }

    public String[] getCategoryPath() {
        return categoryPath;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public void setCategoryPath(String[] categoryPath) {
        this.categoryPath = categoryPath;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setScoreFunction(String scoreFunction) {
        this.scoreFunction = scoreFunction;
    }
}
