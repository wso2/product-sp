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

package org.wso2.eventsimulator.core.simulator.csvFeedSimulation.core;

import org.wso2.msf4j.formparam.FileInfo;

import java.io.InputStream;

/**
 * This class has details of databaseFeedSimulation file
 */
public class FileDto {
    /**
     * FileInfo Bean supports by MSF4J
     */
    private FileInfo fileInfo;

    /**
     * Initialize FileDto
     *
     * @param fileInfo FileInfo Bean supports by MSF4J
     */
    public FileDto(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }


}
