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

/**
 * This class represents a bean class for a range for numbers, includes details about the bounds of the range.
 * To cover all cases, double values are used
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class JSDrillDownRangeBean {
    @XmlElement(name = "label")
    private String label;
    @XmlElement(name = "from")
    private double from;
    @XmlElement(name = "to")
    private double to;
    @XmlElement(name = "count", required = false)
    private double count;
    /**
     * This constructor is for jax rs serialization/deserialization
     */
    public JSDrillDownRangeBean() {

    }

    public JSDrillDownRangeBean(String label, double from, double to, double score) {
        this.label = label;
        this.from = from;
        this.to = to;
    }

    public double getFrom() {
        return from;
    }

    public double getTo() {
        return to;
    }

    public void setTo(double to) {
        this.to = to;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setFrom(double from) {
        this.from = from;
    }

    public double getCount() {
        return count;
    }

    public void setCount(double count) {
        this.count = count;
    }
}
