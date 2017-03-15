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

package org.wso2.eventsimulator.core.eventGenerator.randomEventGeneration.bean;

import org.wso2.siddhi.query.api.definition.Attribute;

/**
 * PrimitiveBasedAttribute represents the Random data generator based on primitive data types
 * such as String,Integer,Float,Double,String,Boolean
 * It extends FeedSimulationStreamAttributeDto abstract class
 * <p>
 * Constant value to represent to this type is "PRIMITIVEBASED"
 * <p>
 * Eg for json string for configuration
 * <p>
 * <p>
 * {
 * "type": "PRIMITIVEBASED",
 * "min": "2",
 * "max": "200",
 * "length": "2",
 * }
 * </p>
 */
public class PrimitiveBasedAttributeDto extends RandomAttributeDto {

    /**
     * Type of data to be generated
     */
    private Attribute.Type attrType;
    /**
     * Minimum value for numeric values to be generate
     */
    private String min = null;

    /**
     * Maximum value for numeric values to be generated
     */
    private String max = null;

    /**
     * If attribute type is string length indicates length of the string to be generated
     * If attribute type is Float or Double length indicates no of digits after the decimal point
     */
    private Integer length = null;

    /**
     * Initialize PrimitiveBasedAttribute with parent class
     */
    public PrimitiveBasedAttributeDto() {
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
