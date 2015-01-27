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
package org.wso2.bam.analytics.rest.beans;

import javax.xml.bind.annotation.*;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Class RecordBean.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "id", "tenantId", "tableName", "timestamp", "values" })
@XmlRootElement(name = "record")
public class RecordBean {

	/** The tenant id. */
	@XmlElement(required = false)
	private int tenantId;

	/** The table name. */
	@XmlElement(required = true)
	private String tableName;

	/** The values. */
	@XmlElement(required = true)
	private Map<String, Object> values;

	/** The timestamp. */
	@XmlElement(required = false, nillable = true)
	private Long timestamp;

	/** The id. */
	@XmlElement(required = false)
	private String id;

	/**
	 * Sets the tenant id.
	 * @param tenantId
	 *            the new tenant id
	 */
	public void setTenantId(int tenantId) {
		this.tenantId = tenantId;
	}

	/**
	 * Sets the table name.
	 * @param tableName
	 *            the new table name
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * Sets the values.
	 * @param values
	 *            the values
	 */
	public void setValues(Map<String, Object> values) {
		this.values = values;
	}

	/**
	 * Sets the timestamp.
	 * @param timestamp
	 *            the new timestamp
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = new Long(timestamp);
	}

	/**
	 * Sets the id.
	 * @param id
	 *            the new id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Gets the id.
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Gets the tenant id.
	 * @return the tenant id
	 */
	public int getTenantId() {
		return tenantId;
	}

	/**
	 * Gets the table name.
	 * @return the table name
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * Gets the values.
	 * @return the values
	 */
	public Map<String, Object> getValues() {
		return values;
	}

	/**
	 * Gets the value.
	 * @param name
	 *            the name
	 * @return the value
	 */
	public Object getValue(String name) {
		return this.values.get(name);
	}

	/**
	 * Gets the timestamp.
	 * @return the timestamp
	 */
	public long getTimestamp() {
		if (timestamp == null ){
			return (new Date()).getTime();
		}
		return timestamp.longValue();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof RecordBean)) {
			return false;
		}
		RecordBean rhs = (RecordBean) obj;
		if (this.getTenantId() != rhs.getTenantId()) {
			return false;
		}
		if (!this.getTableName().equals(rhs.getTableName())) {
			return false;
		}
		if (!this.getId().equals(rhs.getId())) {
			return false;
		}
		if (this.getTimestamp() != rhs.getTimestamp()) {
			return false;
		}
		return this.getNotNullValues().equals(rhs.getNotNullValues());
	}

	/**
	 * Gets the not null values.
	 * @return the not null values
	 */
	public Map<String, Object> getNotNullValues() {
		Map<String, Object> result = new LinkedHashMap<String, Object>(this.getValues());
		Iterator<Map.Entry<String, Object>> itr = result.entrySet().iterator();
		while (itr.hasNext()) {
			if (itr.next().getValue() == null) {
				itr.remove();
			}
		}
		return result;
	}
}
