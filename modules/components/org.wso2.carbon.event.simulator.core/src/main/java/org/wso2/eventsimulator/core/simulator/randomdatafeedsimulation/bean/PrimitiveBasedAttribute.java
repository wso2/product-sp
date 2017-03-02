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
package org.wso2.eventsimulator.core.simulator.randomdatafeedsimulation.bean;

/**
 * PrimitiveBasedAttribute represents the Random data generator based on primitive data types
 * such as String,Integer,Float,Double,Long,Boolean
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
public class PrimitiveBasedAttribute extends FeedSimulationStreamAttributeDto {
    /**
     * Minimum value for numeric values to be generate
     */
    private Object min;

    /**
     * Maximum value for numeric values to be generated
     */
    private Object max;

    /**
     * If attribute type is string length indicates length of the string to be generated
     * If attribute type is Float or Double length indicates no of digits after the decimal point
     */
    private int length;

    /**
     * Initialize PrimitiveBasedAttribute with parent class
     *
     * @param type   Random Data Generator type
     * @param min    Minimum Value
     * @param max    Maximum Value
     * @param length Length
     */
    public PrimitiveBasedAttribute(String type, Object min, Object max, int length) {
        super();
        this.min = min;
        this.max = max;
        this.length = length;
    }

    public Object getMin() {
        return min;
    }

    public void setMin(Object min) {
        this.min = min;
    }

    public Object getMax() {
        return max;
    }

    public void setMax(Object max) {
        this.max = max;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

}
