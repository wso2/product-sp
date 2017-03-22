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
package org.wso2.event.simulator.core.bean;

import org.wso2.msf4j.formparam.FileInfo;

/**
 * CSVSimulationDto returns the configuration for file simulation
 */
public class CSVSimulationDto extends StreamConfigurationDto {
    /**
     * File Name
     */
    private String fileName;

    /**
     * FileInfo Bean supported by MSF4J
     */
    private FileInfo fileInfo;

    /**
     * Delimiter that is used in CSV file to separate values
     */
    private String delimiter;


    /**
     * Flag to indicate whether the CSV records are ordered by timestamp or not
     */
    private boolean isOrdered = true;

    public CSVSimulationDto() {
        super();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public boolean getIsOrdered() {
        return isOrdered;
    }

    public void setIsOrdered(boolean ordered) {
        isOrdered = ordered;
    }
}
