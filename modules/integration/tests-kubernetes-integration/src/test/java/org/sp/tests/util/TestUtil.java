/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.sp.tests.util;


import io.netty.handler.codec.http.HttpMethod;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;


/**
 * TestUtil class
 */
public class TestUtil {
    private static final Logger logger = Logger.getLogger(TestUtil.class);

    public static HTTPResponse sendHRequest(String body, URI baseURI, String path, String contentType,
                                            String methodType, String userName, String password)
            throws IOException {

        HttpURLConnection urlConn = null;
        try {
            urlConn = TestUtil.generateRequest(baseURI, path, methodType, false);

            TestUtil.setHeader(urlConn, "Authorization", "Basic " + java.util.Base64.getEncoder().
                    encodeToString((userName + ":" + password).getBytes()));
            if (contentType != null) {
                TestUtil.setHeader(urlConn, "Content-Type", contentType);
            }
            TestUtil.setHeader(urlConn, "HTTP_METHOD", methodType);
            if (methodType.equals(HttpMethod.POST.name()) || methodType.equals(HttpMethod.PUT.name())) {
                TestUtil.writeContent(urlConn, body);
            }
            assert urlConn != null;

            HTTPResponse httpResponseMessage = new HTTPResponse(urlConn.getResponseCode(),
                    urlConn.getContentType(), TestUtil.getResponseMessage(urlConn));
            urlConn.disconnect();
            return httpResponseMessage;
        } catch (IOException e) {
            throw new IOException("Error generating request to " + baseURI + path, e);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static String getResponseMessage(HttpURLConnection urlConn) {
        StringBuilder sb;
        String value = null;
        try {

            BufferedReader br = null;
            if (200 <= urlConn.getResponseCode() && urlConn.getResponseCode() <= 299) {

                if (urlConn.getContentLength() > 0) {
                    br = new BufferedReader(new InputStreamReader((urlConn.getInputStream())));
                }

            } else {
                br = new BufferedReader(new InputStreamReader((urlConn.getErrorStream())));
            }

            sb = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
            /*while (true) {
                final String line = br.readLine();
                if (line == null) break;
                    sb.append(line);
                            }*/
            value = sb.toString();
        } catch (IOException e) {
            TestUtil.handleException("IOException occurred while getting the response message: ", e);
        } catch (NullPointerException ne) {
            sb = new StringBuilder();
            sb.append("Resource Not Found");
        }
        return value;

    }

    private static void writeContent(HttpURLConnection urlConn, String content) throws IOException {
        OutputStreamWriter out = new OutputStreamWriter(urlConn.getOutputStream());
        out.write(content);
        out.close();
    }

    private static HttpURLConnection generateRequest(URI baseURI, String path, String method, boolean keepAlive)
            throws IOException {
        URL url = baseURI.resolve(path).toURL();
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        urlConn.setRequestMethod(method);
        if (method.equals(HttpMethod.POST.name()) || method.equals(HttpMethod.PUT.name())) {
            urlConn.setDoOutput(true);
        }
        if (keepAlive) {
            urlConn.setRequestProperty("Connection", "Keep-Alive");
        }
        return urlConn;
    }

    private static void setHeader(HttpURLConnection urlConnection, String key, String value) {
        urlConnection.setRequestProperty(key, value);
    }

    public static void handleException(String msg, Exception ex) {
        logger.error(msg, ex);
    }

    public static void waitThread(long timeInMilliseconds) {
        try {
            Thread.sleep(timeInMilliseconds);
        } catch (InterruptedException e) {
            TestUtil.handleException("IO Exception when thread sleep : ", e);
        }
    }

}
