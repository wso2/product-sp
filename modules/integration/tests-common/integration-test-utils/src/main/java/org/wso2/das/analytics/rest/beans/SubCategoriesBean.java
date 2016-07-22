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
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

/**
 * This class Contains Sub-categories of a given facet field/column. "path" represents the hierarchical
 * path to the categories and "categories" contains the list of categories with their scores.
 */
@XmlRootElement(name = "subCategories")
@XmlAccessorType(XmlAccessType.FIELD)
public class SubCategoriesBean {

    private String[] categoryPath;
    private int categoryCount;
    private Map<String, Double> categories;

    /**
     * This constructor is used by JAX-RS
     */
    public SubCategoriesBean() {

    }
    public SubCategoriesBean(String[] path,
                             Map<String, Double> categories) {
        this.categoryPath = path;
        this.categories = categories;
    }

    public SubCategoriesBean(String[] categoryPath, int categoryCount,
                             Map<String, Double> categories) {
        this.categoryPath = categoryPath;
        this.categoryCount = categoryCount;
        this.categories = categories;
    }

    public void setCategories(Map<String, Double> categories) {
        this.categories = categories;
    }

    public void setCategoryPath(String[] categoryPath) {
        this.categoryPath = categoryPath;
    }

    public int getCategoryCount() {
        return categoryCount;
    }

    public void setCategoryCount(int categoryCount) {
        this.categoryCount = categoryCount;
    }

    public String[] getCategoryPath() {
        return categoryPath;
    }

    public Map<String, Double> getCategories() {
        return categories;
    }
}
