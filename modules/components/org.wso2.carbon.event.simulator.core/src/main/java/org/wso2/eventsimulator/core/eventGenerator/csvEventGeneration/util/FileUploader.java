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
package org.wso2.eventsimulator.core.eventGenerator.csvEventGeneration.util;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.wso2.eventsimulator.core.eventGenerator.csvEventGeneration.bean.FileDto;
import org.wso2.eventsimulator.core.eventGenerator.util.exceptions.EventSimulationException;
import org.wso2.eventsimulator.core.eventGenerator.util.exceptions.ValidationFailedException;
import org.wso2.msf4j.formparam.FileInfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class used to upload and delete the CSV file which is uploaded by user.
 */

public class FileUploader {
    public static final String DIRECTORY_NAME = "eventSimulator";
    private static final Logger log = Logger.getLogger(FileUploader.class);

    /**
     * FileUploader Object which has private static access to create singleton object
     *
     * @link org.wso2.eventsimulator.core.eventGenerator.csvEventGeneration.util.FileUploader#getFileUploaderInstance()
     */
    private static final FileUploader fileUploader = new FileUploader(FileStore.getFileStore());

    /**
     * FileStore object which holds details of uploaded file
     */
    private FileStore fileStore;

    /**
     * Initialize the FileUploader with FileStore property
     *
     * @param fileStore FileStore Object
     */
    private FileUploader(FileStore fileStore) {
        this.fileStore = fileStore;
    }

    /**
     * Method Singleton FileUploader object
     *
     * @return fileUploader
     */
    public static FileUploader getFileUploaderInstance() {
        return fileUploader;
    }

    /**
     * Method to upload a CSV file.
     *
     * @param fileInfo    FileInfo Bean supported by MSF4J
     * @param inputStream InputStream Of file
     * @throws ValidationFailedException throw exceptions if csv file validation failure
     * @throws EventSimulationException  throw exceptions if csv file copying
     * @see FileInfo
     */

    public void uploadFile(FileInfo fileInfo, InputStream inputStream) throws
            ValidationFailedException, EventSimulationException {

        String fileName = fileInfo.getFileName();
        // Validate file extension
        try {
            if (validateFile(fileName)) {

                try {
                    /*
                    * check whether the file already exists.
                    * if so log it exists.
                    * else, add the file
                    * */
                    if (fileStore.checkExists(fileName)) {
                        log.error("File '" + fileName + "' already exists in " +
                                (Paths.get(System.getProperty("java.io.tmpdir"), DIRECTORY_NAME)).toString());
                    } else {
                        FileDto fileDto = new FileDto(fileInfo);
                        Files.copy(inputStream,
                                Paths.get(System.getProperty("java.io.tmpdir"), DIRECTORY_NAME, fileName));
                        fileStore.addFile(fileDto);
                    }
                } finally {
                    IOUtils.closeQuietly(inputStream);
                }
            }
        } catch (ValidationFailedException e) {
            log.error("CSV file Extension validation failure : " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("Error while Copying the file " + fileName + " : " + e.getMessage(), e);
        }

        if (log.isDebugEnabled()) {
            log.debug("Successfully uploaded CSV file '" + fileName + "'");
        }
    }

    /**
     * Method to delete an uploaded file.
     *
     * @param fileName File Name of uploaded CSV file
     */
    public void deleteFile(String fileName) {
        try {
            if (fileStore.checkExists(fileName)) {
                fileStore.removeFile(fileName);
                Files.deleteIfExists(Paths.get(System.getProperty("java.io.tmpdir"), DIRECTORY_NAME, fileName));
            }
        } catch (IOException e) {
            log.error("Error while deleting the file " + e.getMessage(), e);
        }

    }


    /**
     * Method to validate CSV file Extension
     *
     * @param fileName File name
     * @return true if CSV file extension is in correct format
     * @throws ValidationFailedException throw exceptions if csv file validation failure
     * @link FileUploader#validateFileExtension(java.lang.String)
     */
    private boolean validateFile(String fileName) throws ValidationFailedException {
        if (!validateFileExtension(fileName)) {
            throw new ValidationFailedException(fileName + " is found : but '.csv' is required as " +
                    "databaseFeedSimulation file extension");
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
