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

/**
 * PropertyBasedAttributeDTO represents the Random data generator which generates meaning full data
 * <p>
 * Eg for json string for configuration
 * {
 * "type": "PROPERTY_BASED",
 * "category": "Contact",
 * "property": "Full Name",
 * }
 *
 * @see "https://www.mockaroo.com/"
 */

public class PropertyBasedAttributeDTO extends RandomAttributeDTO {

    /**
     * Sub property of each main module
     * <p>
     * Eg : Full name is one of the property for Contact
     */
    private String property;

    public PropertyBasedAttributeDTO() {
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

}
