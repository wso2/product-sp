/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.event.simulator.core.internal.bean;

import org.wso2.siddhi.query.api.definition.Attribute;

/**
 * PrimitiveBasedAttributeDTO represents configuration for  the Random data generator based on primitive data types
 * such as String,Integer,Float,Double,String,Boolean
 * It extends RandomAttributeDTO class
 * <p>
 * Eg for json string for configuration
 * <p>
 * <p>
 * {
 * "type": "PRIMITIVE_BASED",
 * "min": "2",
 * "max": "200",
 * "length": "2",
 * }
 * </p>
 */
public class PrimitiveBasedAttributeDTO implements RandomAttributeDTO {
    /**
     * Type of data to be generated
     */
    private Attribute.Type attrType;
    /**
     * Minimum value for numeric values to be generate.
     * Min value is stored as a String because it may be needed to be parsed into Int, Long, and Float types.
     */
    private String min = null;
    /**
     * Maximum value for numeric values to be generated.
     * Max value is stored as a String because it may be needed to be parsed into Int, Long, and Float types.
     */
    private String max = null;
    /**
     * If attribute type is string length indicates length of the string to be generated
     * If attribute type is Float or Double length indicates no of digits after the decimal point
     */
    private Integer length = null;

    /**
     * Initialize PrimitiveBasedAttributeDTO with parent class
     */
    public PrimitiveBasedAttributeDTO() {
    }

    public Attribute.Type getAttrType() {
        return attrType;
    }

    public void setAttrType(Attribute.Type attrType) {
        this.attrType = attrType;
    }

    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

}
