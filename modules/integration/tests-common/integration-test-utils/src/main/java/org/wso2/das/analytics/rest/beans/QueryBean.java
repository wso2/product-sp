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
import java.util.List;

/**
 * The Class QueryBean.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"tableName", "columns", "query", "start", "count" })
@XmlRootElement(name = "query")
public class QueryBean {
	
	/** The table name. */
	@XmlElement(required = true)
	private String tableName;

	/** The columns. */
	@XmlElement(required = false)
	private List<String> columns;
	
	/** The query. */
	@XmlElement(required = false)
	private String query;
	
	/** The start. */
	@XmlElement(required = false)
	private int start;
	
	/** The count. */
	@XmlElement(required = false)
	private int count;

	/**
	 * Gets the table name.
	 * @return the table name
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * Sets the table name.
	 * @param tableName the new table name
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * Gets the columns.
	 * @return the columns
	 */
	public List<String> getColumns() {
		return columns;
	}

	/**
	 * Sets the columns.
	 * @param columns the columns
	 */
	public void setColumns(List<String> columns) {
		this.columns = columns;
	}

	/**
	 * Gets the query.
	 * @return the query
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * Sets the query.
	 * @param query the new query
	 */
	public void setQuery(String query) {
		this.query = query;
	}

	/**
	 * Gets the start.
	 * @return the start
	 */
	public int getStart() {
		return start;
	}

	/**
	 * Sets the start.
	 * @param start the new start
	 */
	public void setStart(int start) {
		this.start = start;
	}

	/**
	 * Gets the count.
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * Sets the count.
	 * @param count
	 *            the new count
	 */
	public void setCount(int count) {
		this.count = count;
	}
}
