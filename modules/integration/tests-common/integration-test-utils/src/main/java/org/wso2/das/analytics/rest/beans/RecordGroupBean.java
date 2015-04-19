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

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The Class RecordGroupBean.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "locations", "records" })
@XmlRootElement(name = "recordGroup")
public class RecordGroupBean {

	/** The locations. */
	@XmlElement(required = true)
	private String[] locations;

	/** The records. */
	@XmlElement(required = true)
	private List<RecordBean> records;

	/**
	 * Gets the locations.
	 * @return the locations
	 */
	public String[] getLocations() {
		return locations;
	}

	/**
	 * Sets the locations.
	 * @param locations
	 *            the new locations
	 */
	public void setLocations(String[] locations) {
		this.locations = locations;
	}

	/**
	 * Gets the records.
	 * @return the records
	 */
	public List<RecordBean> getRecords() {
		return records;
	}

	/**
	 * Sets the records.
	 * @param records
	 *            the new records
	 */
	public void setRecords(List<RecordBean> records) {
		this.records = records;
	}

}
