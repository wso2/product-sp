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
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class represents the the column definition information
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ColumnDefinitionBean {

    @XmlElement(name = "type")
    private ColumnTypeBean type;

    @XmlElement(name = "isScoreParam", required = false)
    private boolean isScoreParam;

    @XmlElement(name = "isIndex", required = false)
    private boolean isIndex;

    @XmlElement(name = "isFacet", required = false)
    private boolean isFacet;

    public ColumnTypeBean getType() {
        return type;
    }

    public void setType(ColumnTypeBean type) {
        this.type = type;
    }

    public boolean isScoreParam() {
        return isScoreParam;
    }

    public void setScoreParam(boolean isScoreParam) {
        this.isScoreParam = isScoreParam;
    }

    public boolean isIndex() {
        return isIndex;
    }

    public void setIndex(boolean isIndex) {
        this.isIndex = isIndex;
    }

    public boolean isFacet() {
        return isFacet;
    }

    public void setFacet(boolean isFacet) {
        this.isFacet = isFacet;
    }
}
