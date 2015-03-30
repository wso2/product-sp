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

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * The Enum IndexTypeBean.
 */
@XmlType(name = "indexType")
@XmlEnum
public enum IndexTypeBean {

	/** The string. */
	@XmlEnumValue("STRING")
	STRING,

	/** The integer. */
	@XmlEnumValue("INTEGER")
	INTEGER,

	/** The long. */
	@XmlEnumValue("LONG")
	LONG,

	/** The float. */
	@XmlEnumValue("FLOAT")
	FLOAT,

	/** The double. */
	@XmlEnumValue("DOUBLE")
	DOUBLE,

	/** The boolean. */
	@XmlEnumValue("BOOLEAN")
	BOOLEAN,

    /** The boolean. */
	@XmlEnumValue("FACET")
	FACET
}
