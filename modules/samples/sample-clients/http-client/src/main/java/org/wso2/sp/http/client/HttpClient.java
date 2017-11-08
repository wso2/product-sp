/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.sp.http.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * This is a sample HTTP client to publish events to HTTP/HTTPS endpoint
 */
public class HttpClient {

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(HttpClient.class);

    public static void main(String[] args) throws InterruptedException, KeyManagementException {
        setCarbonHome();
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 5005));
        String event1 = "name:\"John\",\n" +
                        "age:20,\n" +
                        "country:\"SL\"";
        String event2 = "name:\"Mike\",\n" +
                        "age:20,\n" +
                        "country:\"USA\"";
        httpPublishEvent(event1, baseURI, "/inputStream", false, "text"
        );
        httpPublishEvent(event2, baseURI, "/inputStream", false, "text"
        );
        Thread.sleep(500);
        event1 = "name:\"Jane\",\n" +
                 "age:20,\n" +
                 "country:\"India\"";
        event2 = "name:\"Donna\",\n" +
                 "age:20,\n" +
                 "country:\"Aus\"";
        httpsPublishEvent(event1, "https://localhost:8005/inputStream", false,
                "text/plain");
        httpsPublishEvent(event2, "https://localhost:8005/inputStream", false,
                "text/plain");
        Thread.sleep(100);
    }

    private static void setCarbonHome() {
        Path carbonHome = Paths.get("");
        carbonHome = Paths.get(carbonHome.toString(), "src", "main", "java", "resources");
        System.setProperty("carbon.home", carbonHome.toString());
        logger.info("Carbon Home Absolute path set to: " + carbonHome.toAbsolutePath());
    }

    private static void httpsPublishEvent(String event, String baseURI, Boolean auth, String mapping) throws
            KeyManagementException {
        try {
            System.setProperty("javax.net.ssl.trustStore", System.getProperty("carbon.home") + "/" +
                    "client-truststore.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
            Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
            char[] passphrase = "wso2carbon".toCharArray(); //password
            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(new FileInputStream(System.getProperty("carbon.home") + "/" +
                    "client-truststore.jks"), passphrase); //path
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keystore);
            SSLContext context = SSLContext.getInstance("TLS");
            TrustManager[] trustManagers = tmf.getTrustManagers();
            context.init(null, trustManagers, null);
            SSLSocketFactory sf = context.getSocketFactory();
            URL url = new URL(baseURI);
            HttpsURLConnection httpsCon = (HttpsURLConnection) url.openConnection();
            httpsCon.setSSLSocketFactory(sf);
            httpsCon.setRequestMethod("POST");
            httpsCon.setRequestProperty("Content-Type", mapping);
            httpsCon.setRequestProperty("HTTP_METHOD", "POST");
            if (auth) {
                httpsCon.setRequestProperty("Authorization",
                        "Basic " + java.util.Base64.getEncoder().encodeToString(("admin" + ":" + "admin").getBytes()));
            }
            httpsCon.setDoOutput(true);
            try (OutputStreamWriter out = new OutputStreamWriter(httpsCon.getOutputStream())) {
                out.write(event);
            }
            logger.info("Event response code " + httpsCon.getResponseCode());
            logger.info("Event response message " + httpsCon.getResponseMessage());
            httpsCon.disconnect();
        } catch (IOException e) {
            logger.error("IO Error", e);
        } catch (NoSuchAlgorithmException e) {
            logger.error("NoSuchAlgorithmException Error", e);
        } catch (CertificateException e) {
            logger.error("CertificateException Error", e);
        } catch (KeyStoreException e) {
            logger.error("KeyStoreException Error", e);
        }
    }

    private static void httpPublishEvent(String event, URI baseURI, String path, Boolean auth, String mapping) {
        try {
            HttpURLConnection urlConn = null;
            try {
                urlConn = HttpServerUtil.request(baseURI, path, "POST", true);
            } catch (IOException e) {
                logger.error("IOException occurred while running the HttpsSourceTestCaseForSSL", e);
            }
            if (auth) {
                HttpServerUtil.setHeader(urlConn, "Authorization",
                        "Basic " + java.util.Base64.getEncoder().encodeToString(("admin" + ":" + "admin")
                                .getBytes()));
            }
            HttpServerUtil.writeContent(urlConn, event);
            assert urlConn != null;
            logger.info("Event response code " + urlConn.getResponseCode());
            logger.info("Event response message " + urlConn.getResponseMessage());
            urlConn.disconnect();
        } catch (IOException e) {
            logger.error("IOException occurred while running the HttpsSourceTestCaseForSSL", e);
        }
    }

    private static class HttpServerUtil {

        private HttpServerUtil() {
        }

        static void writeContent(HttpURLConnection urlConn, String content) throws IOException {
            try (OutputStreamWriter out = new OutputStreamWriter(
                    urlConn.getOutputStream())) {
                out.write(content);
            }
        }

        static HttpURLConnection request(URI baseURI, String path, String method, boolean keepAlive)
                throws IOException {
            URL url = baseURI.resolve(path).toURL();
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            if (method.equals("POST") || method.equals("PUT")) {
                urlConn.setDoOutput(true);
            }
            urlConn.setRequestMethod(method);
            if (!keepAlive) {
                urlConn.setRequestProperty("Connection", "Keep-Alive");
            }
            return urlConn;
        }

        static void setHeader(HttpURLConnection urlConnection, String key, String value) {
            urlConnection.setRequestProperty(key, value);
        }

    }
}
