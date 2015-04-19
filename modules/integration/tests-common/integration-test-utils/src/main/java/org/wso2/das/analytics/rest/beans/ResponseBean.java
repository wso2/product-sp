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
 * The Class ResponseBean.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "status", "message" })
@XmlRootElement(name = "response")
public class ResponseBean {

	/** The status. */
	@XmlElement(required = true)
	private String status;

	/** The message. */
	@XmlElement(required = false)
	private String message;

	/**
	 * Instantiates a new response bean.
	 */
	public ResponseBean() {
	}

	/**
	 * Instantiates a new response bean.
	 * @param status the status
	 * @param message the message
	 */
	public ResponseBean(String status, String message) {
		this.status = status;
		this.message = message;
	}

	/**
	 * Instantiates a new response bean.
	 * @param status the status
	 */
	public ResponseBean(String status) {
		this.status = status;
	}

	/**
	 * Gets the status.
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Sets the status.
	 * @param status the new status
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * Gets the message.
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets the message.
	 * @param message the new message
	 */
	public void setMessage(String message) {
		this.message = message;
	}
}
