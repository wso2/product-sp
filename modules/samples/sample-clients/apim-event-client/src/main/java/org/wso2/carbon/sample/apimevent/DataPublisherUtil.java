/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.sample.apimevent;

import java.io.File;

/**
 * Datapublisher Util Implementataion.
 */
public class DataPublisherUtil {

    public static final String RESOURCES = "resources";
    public static final String MAIN = "main";
    public static final String SRC = "src";
    public static final String MODULES = "modules";
    public static final String SAMPLES = "samples";
    public static final String SAMPLECLIENTS = "sample-clients";
    public static final String APIMEVENTCLIENT = "apimevent-client";
    public static final String TEST = "test";
    public static final String TRUSTSTORENAME = "javax.net.ssl.trustStore";
    public static final String TRUSTSTOREPASSWORD = "javax.net.ssl.trustStorePassword";
    public static final String KEYSTORELOCATION = "Security.KeyStore.Location";
    public static final String KEYSTOREPASSWORD = "Security.KeyStore.Password";
    public static final String CLIENTTRUSTSTOREFILE = "client-truststore.jks";
    public static final String KEYSTOREFILE = "wso2carbon.jks";
    public static final String PASSWORD = "wso2carbon";

    /**
     * Sets trust store parameters for the event client
     */
    public static void setTrustStoreParams() {
        File filePath = new File(SRC + File.separator + MAIN + File.separator + RESOURCES);
        if (!filePath.exists()) {
            filePath = new File(MODULES + File.separator + SAMPLES + File.separator +
                    SAMPLECLIENTS + File.separator + APIMEVENTCLIENT + File.separator + SRC +
                    File.separator + MAIN + File.separator + RESOURCES);
        }
        if (!filePath.exists()) {
            filePath = new File(RESOURCES);
        }
        if (!filePath.exists()) {
            filePath = new File(TEST + File.separator + RESOURCES);
        }
        String trustStore = filePath.getAbsolutePath();
        System.setProperty(TRUSTSTORENAME, trustStore + File.separator + CLIENTTRUSTSTOREFILE);
        System.setProperty(TRUSTSTOREPASSWORD, PASSWORD);
    }

    /**
     * Sets key store parameters for the event client
     */
    public static void setKeyStoreParams() {
        File filePath = new File(SRC + File.separator + MAIN + File.separator + RESOURCES);
        if (!filePath.exists()) {
            filePath = new File(MODULES + File.separator + SAMPLES + File.separator +
                    SAMPLECLIENTS + File.separator + APIMEVENTCLIENT + File.separator + SRC +
                    File.separator + MAIN + File.separator + RESOURCES);
        }
        if (!filePath.exists()) {
            filePath = new File(RESOURCES);
        }
        if (!filePath.exists()) {
            filePath = new File(TEST + File.separator + RESOURCES);
        }
        String keyStore = filePath.getAbsolutePath();
        System.setProperty(KEYSTORELOCATION, keyStore + File.separator + KEYSTOREFILE);
        System.setProperty(KEYSTOREPASSWORD, PASSWORD);
    }

    /**
     * Returns the File path of the DataAgent Config
     *
     * @param fileName name of the file
     * @return complete file path
     */
    public static String getDataAgentConfigPath(String fileName) {
        File filePath = new File(SRC + File.separator + MAIN + File.separator + RESOURCES);
        if (!filePath.exists()) {
            filePath = new File(MODULES + File.separator + SAMPLES + File.separator +
                    SAMPLES + File.separator + APIMEVENTCLIENT + File.separator + SRC +
                    File.separator + MAIN + File.separator + RESOURCES);
        }
        if (!filePath.exists()) {
            filePath = new File(RESOURCES);
        }
        if (!filePath.exists()) {
            filePath = new File(TEST + File.separator + RESOURCES);
        }
        return filePath.getAbsolutePath() + File.separator + fileName;
    }

}
