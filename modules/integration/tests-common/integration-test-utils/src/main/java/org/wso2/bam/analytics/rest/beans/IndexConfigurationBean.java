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

package org.wso2.bam.analytics.rest.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents the index configuration bean which contains the index column definition and
 * scoring parameters for scoring function.
 */
@XmlRootElement(name = "indexConfiguration")
@XmlAccessorType(XmlAccessType.FIELD)
public class IndexConfigurationBean {

    @XmlElement(name = "indices", required = true)
    private Map<String, IndexTypeBean> indices;

    @XmlElement(name = "scoreParams", required = false)
    private List<String> scoreParams;

    public Map<String, IndexTypeBean> getIndices() {
        if (indices == null) {
            return new HashMap<String, IndexTypeBean>(0);
        }
        return indices;
    }

    public void setIndices(Map<String, IndexTypeBean> indices) {
        this.indices = indices;
    }

    public List<String> getScoreParams() {
        if (scoreParams == null) {
            return new ArrayList<String>(0);
        }
        return scoreParams;
    }

    public void setScoreParams(List<String> scoreParams) {
        this.scoreParams = scoreParams;
    }
}
