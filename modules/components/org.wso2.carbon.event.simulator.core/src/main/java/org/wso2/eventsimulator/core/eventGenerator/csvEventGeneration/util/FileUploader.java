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
import org.wso2.eventsimulator.core.eventGenerator.util.exceptions.FileAlreadyExistsException;
import org.wso2.eventsimulator.core.eventGenerator.util.exceptions.FileDeploymentException;
import org.wso2.eventsimulator.core.eventGenerator.util.exceptions.ValidationFailedException;
import org.wso2.msf4j.formparam.FileInfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

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
     * @throws FileAlreadyExistsException if the file exists in 'temp/eventSimulator' directory
     * @throws FileDeploymentException if an IOException occurs while copying uploaded stream to 'temp/eventSimulator'
     * directory
     * @see FileInfo
     */

    public void uploadFile(FileInfo fileInfo, InputStream inputStream)
            throws ValidationFailedException, FileAlreadyExistsException, FileDeploymentException {

        String fileName = fileInfo.getFileName();
        // Validate file extension
        try {
            if (validateFile(fileInfo)) {
                    /*
                    * check whether the file already exists.
                    * if so log it exists.
                    * else, add the file
                    * */
                if (fileStore.checkExists(fileName)) {
                    log.error("File '" + fileName + "' already exists in " +
                            (Paths.get(System.getProperty("java.io.tmpdir"), DIRECTORY_NAME)).toString());
                    throw new FileAlreadyExistsException("File '" + fileName + "' already exists in " +
                            (Paths.get(System.getProperty("java.io.tmpdir"), DIRECTORY_NAME)).toString());

                } else {
                    Files.copy(inputStream,
                            Paths.get(System.getProperty("java.io.tmpdir"), DIRECTORY_NAME, fileName));

                    if (log.isDebugEnabled()) {
                        log.debug("Copied content of file '" + fileName + "' to " +
                                (Paths.get(System.getProperty("java.io.tmpdir"), DIRECTORY_NAME)).toString());
                    }
                    fileStore.addFile(fileInfo);
                }
            } else {
                throw new ValidationFailedException("File '" + fileInfo.getFileName() + " has an invalid content type."
                        + " Please upload a valid CSV file .");
            }
        } catch (IOException e) {
            log.error("Error occurred while copying the file '" + fileName + "' to location '" +
                    Paths.get(System.getProperty("java.io.tmpdir"), DIRECTORY_NAME, fileName).toString() + "' : ", e);
            throw new FileDeploymentException("Error occurred while copying the file '" + fileName + "' to location '" +
                    Paths.get(System.getProperty("java.io.tmpdir"), DIRECTORY_NAME, fileName).toString() + "' : ", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        if (log.isDebugEnabled()) {
            log.debug("Successfully uploaded CSV file '" + fileName + "'");
        }
    }

    /**
     * Method to delete an uploaded file.
     *
     * @param fileName File Name of uploaded CSV file
     * @throws FileDeploymentException if an IOException occurs while deleting file
     */
    public void deleteFile(String fileName) throws FileDeploymentException {
        try {
            if (fileStore.checkExists(fileName)) {
                fileStore.removeFile(fileName);
                Files.deleteIfExists(Paths.get(System.getProperty("java.io.tmpdir"), DIRECTORY_NAME, fileName));

                if (log.isDebugEnabled()) {
                    log.debug("Deleted file '" + fileName + "'");
                }
            }
        } catch (IOException e) {
            log.error("Error occurred while deleting the file '" + fileName + "' : ", e);
            throw new FileDeploymentException("Error occurred while deleting the file '" + fileName + "' : ", e);
        }

    }


    /**
     * Method to validate CSV file Extension
     *
     * @param fileInfo file info of uploaded file
     * @return true if CSV file extension is in correct format
     */
    private boolean validateFile(FileInfo fileInfo) {

        return ((fileInfo.getContentType().compareTo("text/csv")) == 0);
    }
}
