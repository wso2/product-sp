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

import org.wso2.eventsimulator.core.util.RandomDataGeneratorConstants;

/**
 * PropertyBasedAttributeDto represents the Random data generator which generates meaning full data
 * <p>
 * Constant value to represent to this type is "PROPERTYBASED"
 * <p>
 * Eg for json string for configuration
 * {
 * "type": "PROPERTYBASED",
 * "category": "Contact",
 * "property": "Full Name",
 * }
 *
 * @See <databaseFeedSimulation href="https://www.mockaroo.com/">www.mockaroo.com</databaseFeedSimulation>
 */

public class PropertyBasedAttributeDto extends FeedSimulationStreamAttributeDto {
    /**
     * Main module
     * <p>
     * It has "Calendar, Contact, Finance, Internet, User Agent, Location, Mobile, Words" main modules
     *
     * @see RandomDataGeneratorConstants
     */
    private String category;

    /**
     * Sub property of each mail module
     * <p>
     * Eg : Full name is one of the property for Contact
     *
     * @see RandomDataGeneratorConstants
     */
    private String property;

    public PropertyBasedAttributeDto(String type, String category, String property) {
        super();
        this.category = category;
        this.property = property;
    }

    public PropertyBasedAttributeDto() {
        super();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    @Override
    public String toString(){
        String configuration = "Type : Property based, category : " + category + ", property : " + property;
        return configuration;
    }

}
