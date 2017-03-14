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
package org.wso2.eventsimulator.core.simulator.bean;

import org.wso2.eventsimulator.core.simulator.csvFeedSimulation.core.FileUploader;
import org.wso2.eventsimulator.core.simulator.csvFeedSimulation.core.FileDto;
import org.wso2.msf4j.formparam.FileInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * FileStore is act as databaseFeedSimulation In Memory for uploaded CSV files.
 * File Store creates databaseFeedSimulation singleton Object.
 * FileUploader uses this to store all file details
 *
 * @see FileUploader
 */
public class FileStore {
    /**
     * Concurrent HashMap to hold the details of uploaded CSV files
     * It holds the data as key value pair
     * key: fileName
     * value: FileDto which holds file information
     *
     * @see FileDto
     */
    private ConcurrentHashMap<String, FileDto> fileInfoMap = new ConcurrentHashMap<>();

    /**
     * FileStore object which has private access to create singleton Object
     *
     * @link org.wso2.carbon.event.simulator.bean.FileStore#getFileStore()
     */
    private static FileStore fileStore;

    private FileStore() {

       try {
           new File(Paths.get(System.getProperty("java.io.tmpdir"),FileUploader.DIRECTORY_NAME).toString()).mkdirs();
           List<File> filesInFolder = Files.walk(Paths.get(System.getProperty("java.io.tmpdir"),FileUploader.DIRECTORY_NAME)).filter(Files::isRegularFile).map(Path::toFile).collect(Collectors.toList());
           for (File file : filesInFolder) {
               FileInfo f = new FileInfo();
               f.setContentType("text/csv");
               f.setFileName(file.getName());
               fileInfoMap.put(file.getName(), new FileDto(f));
           }
       } catch (IOException e) {
           e.getMessage();
       }
    }

    /**
     * Method to create Singleton Object of FileStore
     *
     * @return fileStore
     */
    public static FileStore getFileStore() {
        if (fileStore == null) {
            synchronized (FileStore.class) {
                if (fileStore == null) {
                    fileStore = new FileStore();
                }
            }
        }
        return fileStore;
    }

    /**
     * Get the fileInfoMap which holds the details of uploaded CSV Files
     *
     * @return fileInfoMap
     */
    public ConcurrentHashMap<String, FileDto> getFileInfoMap() {
        return fileInfoMap;
    }


    /**
     * Method to add file data into in memory
     *
     * @param fileDto FileDto Object which holds the details of file
     */
    public void addFile(FileDto fileDto) {
        fileInfoMap.put(fileDto.getFileInfo().getFileName(), fileDto);
    }

    /**
     * Method to remove the file from in memory
     *
     * @param fileName File Name of uploaded CSV file
     * @throws IOException it throws IOException if anything occurred while
     *                     delete the file from temp directory and in memory
     */
    public void removeFile(String fileName) throws IOException {
        // delete the file from directory
        Files.deleteIfExists(Paths.get(System.getProperty("java.io.tmpdir"), FileUploader.DIRECTORY_NAME, fileName));
        //delete the file from in memory
        fileInfoMap.remove(fileName);
    }

    /**
     * Method to check that the File Name is already exists in directory when user trying to
     * upload the file with same name
     *
     * @param fileName File name of the file
     * @return true if exist false if not exist
     */
    public Boolean checkExists(String fileName) {
        return fileInfoMap.containsKey(fileName);
    }
}
