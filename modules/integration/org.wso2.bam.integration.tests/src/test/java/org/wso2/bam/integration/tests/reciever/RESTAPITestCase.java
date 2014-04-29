package org.wso2.bam.integration.tests.reciever;


import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.hive.stub.HiveExecutionServiceHiveExecutionException;
import org.wso2.carbon.analytics.hive.stub.HiveExecutionServiceStub;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.fail;
import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class RESTAPITestCase {
    private static LoginLogoutUtil util = new LoginLogoutUtil();
    private static final String HIVE_SERVICE = "/services/HiveExecutionService";
    private static HiveExecutionServiceStub hiveStub;

    private static String DATA_RECEIVER;
    private static String restAppParentURL;

    private static String streamsURL;

    private static String streamURL;
    private static String events1;
    private static String events2;
    private static String events3;
    private static String events4;
    private static String events5;
    private static String streamdefn1;
    private static String streamdefn2;
    private static String streamdefn3;
    private static String streamdefn4;
    private static String streamdefn5;


    private static String stockQuoteVersion1URL;
    private static String stockQuoteVersion2URL;
    private static String stockQuoteVersion3URL;
    private static String stockQuoteVersion4URL;
    private static String stockQuoteVersion5URL;

    private static DefaultHttpClient client;
    private static int httpsPort = 9443;
    private static int httpPort = 9763;

    private static String host = "localhost";

    @BeforeClass(groups = {"wso2.bam"})
    private static void init() {

        DATA_RECEIVER = "/datareceiver/1.0.0";

//        host = FrameworkSettings.HOST_NAME;
//        httpsPort = Integer.parseInt(FrameworkSettings.HTTPS_PORT);


        restAppParentURL = "https://"+ host + ":" + httpsPort + DATA_RECEIVER;
        streamsURL = restAppParentURL + "/streams";

        streamURL = restAppParentURL + "/stream";
        stockQuoteVersion1URL = streamURL + "/stockquote.stream/1.0.2/";

        stockQuoteVersion2URL = streamURL + "/stockquote.stream.2/2.0.0";

        stockQuoteVersion3URL = streamURL + "/labit.stream/0.0.3";
        stockQuoteVersion4URL = streamURL + "/eu.ima.event.stream/1.2.0";
        stockQuoteVersion5URL = streamURL + "/eu.ima.event.stream.2/1.3.0";

        try {
            events1 = IOUtils.toString(RESTAPITestCase.class.getClassLoader().getResourceAsStream("rest/events1.json"));
            events2 = IOUtils.toString(RESTAPITestCase.class.getClassLoader().getResourceAsStream("rest/events2.json"));
            events3 = IOUtils.toString(RESTAPITestCase.class.getClassLoader().getResourceAsStream("rest/events3.json"));
            events4 = IOUtils.toString(RESTAPITestCase.class.getClassLoader().getResourceAsStream("rest/events4.json"));
            events5 = IOUtils.toString(RESTAPITestCase.class.getClassLoader().getResourceAsStream("rest/events5.json"));

            streamdefn1 = IOUtils.toString(
                    RESTAPITestCase.class.getClassLoader().getResourceAsStream("rest/streamdefn1.json"));
            streamdefn2 = IOUtils.toString(
                    RESTAPITestCase.class.getClassLoader().getResourceAsStream("rest/streamdefn2.json"));
            streamdefn3 = IOUtils.toString(
                    RESTAPITestCase.class.getClassLoader().getResourceAsStream("rest/streamdefn3.json"));
            streamdefn4 = IOUtils.toString(
                    RESTAPITestCase.class.getClassLoader().getResourceAsStream("rest/streamdefn4.json"));
            streamdefn5 = IOUtils.toString(
                    RESTAPITestCase.class.getClassLoader().getResourceAsStream("rest/streamdefn5.json"));

            TrustManager easyTrustManager = new X509TrustManager() {
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] x509Certificates,
                        String s)
                        throws java.security.cert.CertificateException {
                }

                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] x509Certificates,
                        String s)
                        throws java.security.cert.CertificateException {
                }

                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{easyTrustManager}, null);
            SSLSocketFactory sf = new SSLSocketFactory(sslContext);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            Scheme httpsScheme = new Scheme("https", sf, httpsPort);


            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, "utf-8");
            params.setBooleanParameter("http.protocol.expect-continue", false);

            client = new DefaultHttpClient(params);
            client.getConnectionManager().getSchemeRegistry().register(httpsScheme);

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public static void main(String[] args)  {
        try {
            init();
//            securityCheck();
            publishRESTStreamDefnsAndEvents();



        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }




    //@Test (groups = {"wso2.bam"}, dependsOnMethods = "publishRESTStreamDefnsAndEvents")
    public static void securityCheck() {

        try {
            String errorMsg = "Unexpected status code returned.";
            int unexpectedStatusCode = 202;

            // sending  stream definition post from an invalid user
            int statusCode = sendRequest(getHTTPPost(streamsURL, streamdefn1, "john", "smith"));
            assertNotEquals(statusCode, unexpectedStatusCode, errorMsg);

            // sending  event post from an invalid user
            statusCode = sendRequest(getHTTPPost(stockQuoteVersion1URL, events1, "john", "smith"));
            assertNotEquals(statusCode, unexpectedStatusCode, errorMsg);

            // sending  stream definition post over http
            statusCode = sendRequest(getHTTPPost(streamsURL, streamdefn1), host, httpPort, "http");
            assertNotEquals(statusCode, unexpectedStatusCode, errorMsg);

            // sending  stream definition post over http
            statusCode = sendRequest(getHTTPPost(stockQuoteVersion2URL, events2), host, httpPort, "http");
            assertNotEquals(statusCode, unexpectedStatusCode, errorMsg);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

    }


    @Test (groups = {"wso2.bam"})
    public static void publishRESTStreamDefnsAndEvents() {
        try {
            String errorMsg = "Unexpected status code returned.";
            int expectedStatusCode = 202;

            // persist event streamd defn 1

            int statusCode = sendRequest(getHTTPPost(streamsURL, streamdefn1));
            assertEquals( statusCode, expectedStatusCode, errorMsg);

            // persist event streamd defn 2

            statusCode = sendRequest(getHTTPPost(streamsURL, streamdefn2));
            assertEquals( statusCode, expectedStatusCode, errorMsg);

            // persist event streamd defn 3

            statusCode = sendRequest(getHTTPPost(streamsURL, streamdefn3));
            assertEquals( statusCode, expectedStatusCode, errorMsg);

            // persist event 1

            statusCode = sendRequest(getHTTPPost(stockQuoteVersion1URL, events1));
            assertEquals( statusCode, expectedStatusCode, errorMsg);

            // persist event 2

            statusCode = sendRequest(getHTTPPost(stockQuoteVersion2URL, events2));
            assertEquals( statusCode, expectedStatusCode, errorMsg);

            // persist event 3

            statusCode = sendRequest(getHTTPPost(stockQuoteVersion3URL, events3));
            assertEquals( statusCode, expectedStatusCode, errorMsg);

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

    }

    @Test (groups = {"wso2.bam"})
    public static void dataTypeValueComparison() {
        publishRESTSEvents();
        runHiveDataTypeTest();

    }

    public static void publishRESTSEvents() {
        try {
            String errorMsg = "Unexpected status code returned.";
            int expectedStatusCode = 202;
            int statusCode = -1;

            // persist event streamd defn 4

            statusCode = sendRequest(getHTTPPost(streamsURL, streamdefn4));
            assertEquals( statusCode, expectedStatusCode, errorMsg);

            // persist event 4

            statusCode = sendRequest(getHTTPPost(stockQuoteVersion4URL, events4));
            assertEquals( statusCode, expectedStatusCode, errorMsg);

            // persist event streamd defn 5

            statusCode = sendRequest(getHTTPPost(streamsURL, streamdefn5));
            assertEquals( statusCode, expectedStatusCode, errorMsg);

            // persist event 5

            statusCode = sendRequest(getHTTPPost(stockQuoteVersion5URL, events5));
            assertEquals( statusCode, expectedStatusCode, errorMsg);

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

    }

    private static void runHiveDataTypeTest() {
        try {
            String[] queries = getHiveQueries("TestScriptForRestDataTypes");
            hiveStub.executeHiveScript(null, queries[0].trim());
            HiveExecutionServiceStub.QueryResult[] results = hiveStub.executeHiveScript(null, queries[1].trim());
            assertTrue(null != results && results.length != 0, "No results are returned from jdbc handler test");
            HiveExecutionServiceStub.QueryResultRow[] rows = results[0].getResultRows();
            assertTrue(null != rows && rows.length != 0, "No results are returned from jdbc handler test");
            String[] vals = rows[0].getColumnValues();
            assertTrue(null != vals && vals.length != 0, "No results are returned from jdbc handler test");

            String[] dataArray = getEventsArray();

            int eventPayloadPosition= 0;
            for (HiveExecutionServiceStub.QueryResultRow queryResultRow : rows) {
                String[] valuesActual = dataArray[eventPayloadPosition].split(",");
                String[] valuesFromDB = queryResultRow.getColumnValues();

                for(int valPos = 0; valPos < valuesActual.length; valPos++) {
                    switch (valPos) {
                        case 0:
                            assertEquals(Boolean.parseBoolean(valuesFromDB[valPos]),
                                    Boolean.parseBoolean(valuesActual[valPos]), "Boolean values are not equal");
                            break;
                        case 1:
                            assertEquals(Integer.parseInt(valuesFromDB[valPos]),
                                    Integer.parseInt(valuesActual[valPos]), "Integer values are not equal");
                            break;
                        case 2:
                            assertEquals(Double.parseDouble(valuesFromDB[valPos]),
                                    Double.parseDouble(valuesActual[valPos]), "Double values are not equal");
                            break;
                        case 3:
                            assertEquals(Float.parseFloat(valuesFromDB[valPos]),
                                    Float.parseFloat(valuesActual[valPos]), "Float values are not equal");
                            break;
                        case 4:
                            assertEquals(Long.parseLong(valuesFromDB[valPos]),
                                    Long.parseLong(valuesActual[valPos]), "Long values are not equal");
                            break;
                        case 5:
                            assertEquals(valuesFromDB[valPos], valuesActual[valPos], "String values are not equal");
                            break;
                    }
                }
                eventPayloadPosition++;
            }

            for (String val : vals) {
                assertTrue(null != val && !val.isEmpty(), "Value is null or empty");
            }
        } catch (HiveExecutionServiceHiveExecutionException e) {
            e.printStackTrace();
            fail("Failed while excecuting hive script " + e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            fail("Error when trying to run hive script: "+ e.getMessage());
        }
    }

    public static String[] getEventsArray() {
        List<String> payloadList =  null;
        String formattedString = events5.replaceAll(" ", "").replaceAll("\\n", "").replaceAll("\\[\\{", "\\{").replaceAll("\\}\\]", "\\}").
                replaceAll("\\},\\{", "}" + (char)24 + "{").replaceAll("],\"", "]" + (char)25 + "\"").replaceAll("\"", "");

        String[] events = formattedString.split("" + (char)24);
        payloadList = new ArrayList<String>(events.length);

        for (String event : events) {
            String [] eventData = event.split("" + (char)25);
            String payloadData = eventData[2].substring(eventData[2].indexOf("[") +1, eventData[2].lastIndexOf("]"));
            payloadList.add(payloadData);
        }

        return payloadList.toArray(new String[payloadList.size()]);
    }

    private static String[] getHiveQueries(String resourceName){
        String[] queries = null;
        try {
            initializeHiveStub();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Error while initializing hive stub: " + e.getMessage());
        }

        URL url = RESTAPITestCase.class.getClassLoader().getResource(resourceName);

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(
                    new File(url.toURI()).getAbsolutePath()));
            String script ="";
            String line = null;
            while ((line = bufferedReader.readLine())!=null){
                script += line;
            }
            queries =script.split(";");
        } catch (Exception e) {
            fail("Error while reading resource : " + resourceName);
        }
        return queries;
    }

    private static void initializeHiveStub() throws Exception {
        ConfigurationContext configContext = ConfigurationContextFactory.
                createConfigurationContextFromFileSystem(null);

        String loggedInSessionCookie = util.login();

        String EPR = "https://" + FrameworkSettings.HOST_NAME +
                ":" + FrameworkSettings.HTTPS_PORT + HIVE_SERVICE;
        hiveStub = new HiveExecutionServiceStub(configContext, EPR);
        ServiceClient client = hiveStub._getServiceClient();
        Options option = client.getOptions();
        option.setTimeOutInMilliSeconds(10*60000);
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                loggedInSessionCookie);
    }

    @AfterClass(groups = {"wso2.bam"})
    public void shutdown() {
        if (client != null) {
            client.getConnectionManager().shutdown();
        }
    }

     private static int sendRequest(HttpPost post) throws IOException {
        return sendRequest(post, host, httpsPort, "https");
    }

    private static int sendRequest(HttpPost post, String host, int port, String protocol) throws IOException {
        HttpResponse httpResponse = client.execute(new HttpHost(host, port, protocol), post);
         int statusCode = httpResponse.getStatusLine().getStatusCode();
        EntityUtils.consume(httpResponse.getEntity());
        return statusCode;
    }

    private static HttpPost getHTTPPost(String url, String entityBody, String username, String password) throws UnsupportedEncodingException, AuthenticationException {
        HttpPost post = new HttpPost(url);
//            post.addHeader("Authorization", "Basic " + new String(Base64.encodeBase64("admin:admin".getBytes())));
        HttpEntity httpEntity = new StringEntity(entityBody, "application/json", "UTF-8");
        post.setEntity(httpEntity);

        post.addHeader(new BasicScheme().authenticate(new UsernamePasswordCredentials(username, password), post));
        return post;
    }

    private static HttpPost getHTTPPost(String url, String entityBody) throws UnsupportedEncodingException, AuthenticationException {
        return getHTTPPost(url, entityBody, "admin", "admin");
    }

}