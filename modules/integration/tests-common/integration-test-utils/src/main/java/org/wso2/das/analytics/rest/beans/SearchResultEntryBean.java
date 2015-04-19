/**
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
import javax.xml.bind.annotation.XmlType;

/**
 * The Class SearchResultEntryBean.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
                      "id",
                      "score"
})

@XmlRootElement( name = "SearchResultEntry")
public class SearchResultEntryBean implements Comparable<SearchResultEntryBean> {
	
	/** The id. */
	@XmlElement(required = true)
	private String id;
	
	/** The score. */
	@XmlElement(required = true)
	private float score;

	/**
	 * Sets the id.
	 * @param id the new id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Sets the score.
	 * @param score the new score
	 */
	public void setScore(float score) {
		this.score = score;
	}
	
	/**
	 * Gets the id.
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Gets the score.
	 * @return the score
	 */
	public float getScore() {
		return score;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(SearchResultEntryBean obj) {
		if (this.score > obj.score) {
			return 1;
		} else if (this.score < obj.score) {
			return -1;
		} else {
			return 0;
		}
    }
}
