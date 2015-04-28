/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.das.analytics.rest.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class represents a facet object bean. facet object defines the hierarchical fieldName,
 * which can be drilled down. This can be used as a value in a record.
 * Example :
 *   Assume a record represents a book.
 *      Then the record field : value pairs will be, e.g.
 *          Price : $50.00
 *          Author : firstName LastName
 *          ISBN : 234325435445435436
 *          Published Date : "1987" , "March", "21"
 *
 * Here Publish Date will be a facet/categoryPath, since it can be drilled down to Year, then month and date
 * and categorizes by each level.
 *
 */
@XmlRootElement(name = "categoryPath")
@XmlAccessorType(XmlAccessType.FIELD)
public class DrillDownPathBean {

    @XmlElement(name = "path")
    private String[] path;
    @XmlElement(name = "fieldName")
    private  String fieldName;

    /**
     * This constructor is for jax-rs json serialization/deserialization
     */
    public DrillDownPathBean() {

    }

    public String[] getPath() {
        return path;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setPath(String[] path) {
        this.path = path;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}
