/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.wso2.eventsimulator.core.simulator.bean.FileStore;
import org.wso2.eventsimulator.core.simulator.exception.EventSimulationException;
import org.wso2.eventsimulator.core.simulator.exception.ValidationFailedException;
import org.wso2.msf4j.formparam.FileInfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class used to deploy and undeploy the CSV file which is uploaded by user.
 * File deployer class creates singleton object because It holds only uploaded file details as databaseFeedSimulation
 * FileStore
 *
 * @see FileStore
 */

public class FileUploader {
    private static final Logger log = Logger.getLogger(FileUploader.class);

    /**
     * FileStore object which holds In memory for uploaded file details
     */
    private FileStore fileStore;

    /**
     * FileUploader Object which has private static access to create singleton object
     *
     * @link org.wso2.carbon.event.simulator.csvFeedSimulation.core.FileUploader#getFileUploaderInstance()
     */
    private static FileUploader fileDeployer;

    /**
     * Method Singleton FileUploader object
     *
     * @return fileDeployer
     */
    public static FileUploader getFileUploaderInstance() {
        if (fileDeployer == null) {
            synchronized (FileUploader.class) {
                if (fileDeployer == null) {
                    fileDeployer = new FileUploader(FileStore.getFileStore());
                }
            }
        }
        return fileDeployer;
    }

    /**
     * Initialize the FileUploader with FileStore property
     *
     * @param fileStore FileStore Object
     */
    private FileUploader(FileStore fileStore) {
        this.fileStore = fileStore;
    }

    /**
     * Method to deploy the uploaded file. It calls processDeploy method with in that
     *
     * @param fileInfo    FileInfo Bean supports by MSF4J
     * @param inputStream InputStream Of file
     * @throws ValidationFailedException throw exception if csv file validation failure
     * @throws EventSimulationException  throw exception if csv file copying
     * @link org.wso2.carbon.event.simulator.csvFeedSimulation.core.FileUploader#processDeploy(FileInfo, InputStream)
     * @see FileInfo
     */

    public void uploadFile(FileInfo fileInfo, InputStream inputStream) throws ValidationFailedException, EventSimulationException {
        String fileName = fileInfo.getFileName();
        // Validate file extension
        try {
            if (validateFile(fileName)) {
                //Check if file is already exist. if so existing file will be delete by giving warning
                //and new file wile be add to the map
                try {

                    if (fileStore.checkExists(fileName)) {
                        fileStore.removeFile(fileInfo.getFileName());
                        //todo remove warn
//                        log.warn("File is already exists: " + fileInfo.getFileName());
                    }
                    FileDto fileDto = new FileDto(fileInfo);
                    Files.copy(inputStream, Paths.get(System.getProperty("java.io.tmpdir"), fileInfo.getFileName()));
                    fileStore.addFile(fileDto);
                    log.info("CSV file deployed successfully :" + fileInfo.getFileName());
                } finally {
                    IOUtils.closeQuietly(inputStream);
                }
            }
        } catch (ValidationFailedException e) {
            log.error("CSV file Extension validation failure : " + e.getMessage());
            throw new ValidationFailedException("CSV file Extension validation failure :" + e.getMessage());
        } catch (IOException e) {
            log.error("Error while Copying the file " + fileName + " : " + e.getMessage());
            throw new EventSimulationException("Error while Copying the file " + e.getMessage());
        }
    }

    /**
     * Method to un deploy the uploaded file. It calls processUndeploy method with in that
     *
     * @param fileName File Name of uploaded CSV file
     * @throws EventSimulationException throw exception if any error occurred during delete the file
     * @link processUndeploy
     */
    public void deleteFile(String fileName) throws EventSimulationException {
        try {
            if (fileStore.checkExists(fileName)) {
                fileStore.removeFile(fileName);
            }
        } catch (IOException e) {
            log.error("Error while deleting the file " + e.getMessage());
            throw new EventSimulationException("Error while deleting the file " + fileName + " " + e.getMessage());
        }

    }


    /**
     * Method to validate CSV file Extension
     *
     * @param fileName File name
     * @return true if CSV file extension is in correct format
     * @throws ValidationFailedException throw exception if csv file validation failure
     * @link org.wso2.carbon.event.simulator.csvFeedSimulation.core.FileUploader#validateFileExtension(java.lang.String)
     */
    private boolean validateFile(String fileName) throws ValidationFailedException {
        if (!validateFileExtension(fileName)) {
            throw new ValidationFailedException(fileName + " is found : " + "but '.csv'" + " is required as databaseFeedSimulation file extension");
        }
        return true;
    }

    /**
     * Method to validate CSV file Extension. It uses regular expression to validate .CSV extension
     *
     * @param fileName File Name
     * @return true if CSV file extension is in correct format
     */
    private boolean validateFileExtension(String fileName) {
        Pattern fileExtensionPattern = Pattern.compile("([^\\s]+(\\.(?i)(csv))$)");
        Matcher matcher = fileExtensionPattern.matcher(fileName);
        return matcher.matches();
    }
}
