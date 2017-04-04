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

import org.wso2.carbon.event.simulator.core.internal.generator.random.util.PropertyBasedAttrGenerator;

import java.util.List;

/**
 * PropertyBasedAttributeDTO represents the Random data generator which generates meaning full data
 * <p>
 * Eg for json string for configuration
 * {
 * "type": "PROPERTY_BASED",
 * "property": "FULL_NAME",
 * }
 *
 * @see "https://www.mockaroo.com/"
 */

public class PropertyBasedAttributeDTO implements RandomAttributeDTO {

    /**
     * Sub property of each main module
     * <p>
     * Eg : Full name is one of the property for Contact
     */
    private PropertyBasedAttrGenerator.PropertyType property;
    private List<Object> data;

    public PropertyBasedAttributeDTO() {
    }

    public PropertyBasedAttrGenerator.PropertyType getProperty() {
        return property;
    }

    public void setProperty(PropertyBasedAttrGenerator.PropertyType property) {
        this.property = property;
    }

    public List<Object> getData() {
        return data;
    }

    public void setData(List<Object> data) {
        this.data = data;
    }
}
