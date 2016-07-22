/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.das.analytics.rest.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class represents the bean class to hold the field name, alias and the aggregate function.
 * Aggregate function will be calculated over the given field name.
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AggregateFieldBean {
    private String[] fields;
    private String fieldName;
    private String aggregate;
    private String alias;

    public AggregateFieldBean() {

    }

    public AggregateFieldBean(String[] fields, String aggregate, String alias) {
        this.fields = fields;
        this.aggregate = aggregate;
        this.alias = alias;
    }

    public String[] getFields() {
        return fields;
    }

    public void setFields(String[] fields) {
        this.fields = fields;
    }

    public String getAggregate() {
        return aggregate;
    }

    public void setAggregate(String aggregate) {
        this.aggregate = aggregate;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}
