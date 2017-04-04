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

import java.util.Arrays;

/**
 * CustomBasedAttributeDTO represents the Random data generator based on custom data list
 * It has a data list which is given by user
 * <p>
 * <p>
 * Eg: If user want to generate value for an attribute WSO2 products
 * then user can give list of products as
 * "list": "CEP,ESB,DAS" with in json body
 * </p>
 * <p>
 * <p>
 * Eg for json string for configuration
 * {
 * "type": "CUSTOM_DATA",
 * "list": {"CEP,ESB","DAS"}
 * }
 * </p>
 */
public class CustomBasedAttributeDTO implements RandomAttributeDTO {

    /**
     * List of custom data value given by user
     */
    private Object[] customDataList = null;

    public CustomBasedAttributeDTO() {
    }

    public Object[] getCustomDataList() {
        return (customDataList != null) ? Arrays.copyOf(customDataList, customDataList.length) : new String[0];
    }

    public void setCustomData(Object[] customDataList) {
        this.customDataList = Arrays.copyOf(customDataList, customDataList.length);

    }
}
