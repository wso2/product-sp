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

import org.wso2.eventsimulator.core.simulator.randomdatafeedsimulation.util.RandomDataGenerator;

/**
 * FeedSimulationStreamAttributeDto represents the Attribute configuration details of an input stream which
 * attributes values to be generated randomly
 * <p>
 * <p>
 * It is an abstract class. CustomBasedAttribute, PrimitiveBasedAttribute, PropertyBasedAttributeDto,
 * RegexBasedAttributeDto extends this parent class.
 * <p>
 * </P>
 *
 * @see CustomBasedAttribute
 * @see PrimitiveBasedAttribute
 * @see PropertyBasedAttributeDto
 * @see RegexBasedAttributeDto
 */
public abstract class FeedSimulationStreamAttributeDto {
    /**
     * Random data generator type of an attribute
     * It's value can be
     * 1. PRIMITIVE_BASED
     * 2. PROPERTY_BASED
     * 3. REGEX_BASED
     * 4. CUSTOM_DATA_BASED
     * This value is chosen by user.
     */
    private RandomDataGeneratorType type;
//    todo 01/03/2017 make type an enum
    public enum RandomDataGeneratorType {PRIMITIVE_BASED,PROPERTY_BASED,REGEX_BASED,CUSTOM_DATA_BASED}

    /**
     * Initialize FeedSimulationStreamAttributeDto
     */
    public FeedSimulationStreamAttributeDto() {
    }

    public RandomDataGeneratorType getType() {
        return type;
    }

    public void setType(RandomDataGeneratorType type) {
        this.type = type;
    }


}
