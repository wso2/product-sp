/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * FeedSimulationStreamAttributeDto represents the Attribute configuration details of an input stream which
 * attributes values to be generated randomly
 * <p>
 * <p>
 * It is an abstract class. CustomBasedAttribute, PrimitiveBasedAttribute, PropertyBasedAttributeDto,
 * RegexBasedAttributeDto are extends this parent class.
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
     * 1. PRIMITIVEBASED
     * 2. PROPERTYBASED
     * 3. REGEXBASEDATTRIBUTE
     * 4. CUSTOMDATABASEDATTRIBUTE
     * This value is chosen by user.
     */
    private String type;
//    private enum RandomDataGeneratorType {PRIMITIVEBASED,PROPERTYBASED,REGEXBASED,CUSTOMDATABASED}

    /**
     * Initialize FeedSimulationStreamAttributeDto
     */
    public FeedSimulationStreamAttributeDto() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


}
