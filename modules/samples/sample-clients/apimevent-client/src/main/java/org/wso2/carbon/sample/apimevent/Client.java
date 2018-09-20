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

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * WSO2Event Client Publisher.
 */
public class Client {
    private static Log log = LogFactory.getLog(Client.class);
    private static final String STREAM_NAME = "org.wso2.apimgt.statistics.throttle";
    private static final String FAULT_STREAM = "org.wso2.apimgt.statistics.fault";
    private static final String REQUEST_STREAM = "org.wso2.apimgt.statistics.request";
    private static final String VERSION = "3.0.0";
    private static String agentConfigFileName = "sync.data.agent.config.yaml";

    private static String[] username = {"admin", "smith", "finch", "starc", "maxwell", "haddin", "warner", "faulkner", "marsh"};
    private static String[] tenantDomain = {"carbon.super", "loc.super","range.com"};
    private static String[] apiName = {"ceylan", "NDB", "Commercial", "BOC", "Peoples", "LG", "Abans", "Singer", "Damro"};
    private static String[] apiCreator = {"admin", "rohan", "Jeevan"};
    private static String[] apiMethod = {"GET","POST","PUT"};
    private static String[] applicationId = {"1", "2", "3"};
    private static String[] applicationName = {"default", "app1", "app2"};
    private static String[] applicationCK = {"default", "app1", "app2"};
    private static String[] applicationOwner = {"Michael", "clarke", "cook"};
    private static String[] userIp = {"10.100.8.14","10.100.8.26","10.100.3.64"};

    public static void main(String[] args) {

        DataPublisherUtil.setKeyStoreParams();
        DataPublisherUtil.setTrustStoreParams();

        log.info("These are the provided configurations: " + Arrays.deepToString(args));

        String protocol = args[0];
        String host = args[1];
        String port = args[2];
        int sslPort = Integer.parseInt(port) + 100;
        String username = args[3];
        String password = args[4];
        String numberOfEventsStr = args[5];
        int numberOfEvents = Integer.parseInt(numberOfEventsStr);

        try {
            log.info("Starting WSO2 Event Client");
            AgentHolder.setConfigPath(DataPublisherUtil.getDataAgentConfigPath(agentConfigFileName));
            DataPublisher dataPublisher = new DataPublisher(protocol, "tcp://" + host + ":" + port,
                    "ssl://" + host + ":" + sslPort, username, password);
            Event event = new Event();
            event.setStreamId(DataBridgeCommonsUtils.generateStreamId(STREAM_NAME, VERSION));
            event.setCorrelationData(null);
            Event faultEvent = new Event();
            faultEvent.setStreamId(DataBridgeCommonsUtils.generateStreamId(FAULT_STREAM, VERSION));
            faultEvent.setCorrelationData(null);
            Event requestEvent = new Event();
            requestEvent.setStreamId(DataBridgeCommonsUtils.generateStreamId(REQUEST_STREAM, VERSION));
            event.setCorrelationData(null);

            String metaClientType="mozilla";

            for (int i = 0; i < numberOfEvents; i++) {
                event.setMetaData(new Object[]{metaClientType});
                faultEvent.setMetaData(new Object[]{metaClientType});
                requestEvent.setMetaData(new Object[]{metaClientType});
                Object[] data = getObject();
                Object[] faultData = getFaultStream();
                Object[] requestData = getRequestStream();
                event.setPayloadData(data);
                faultEvent.setPayloadData(faultData);
                requestEvent.setPayloadData(requestData);
                dataPublisher.publish(event);
                dataPublisher.publish(faultEvent);
                dataPublisher.publish(requestEvent);
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                log.error(e);
            }
            dataPublisher.shutdown();
            log.info("Events published successfully");

        } catch (Throwable e) {
            log.error(e);
        }
    }

    private static Object[] getObject() {

        ThrottledOutDTO throttledObj = new ThrottledOutDTO();
        String[] username = {"admin", "smith", "finch", "starc", "maxwell", "haddin", "warner", "faulkner", "marsh"};
        String[] userTenantDomain = {"carbon.super", "loc.super"};
        String[] apiCreator = {"admin", "rohan", "Jeevan"};
        String[] applicationId = {"1", "2", "3"};
        String[] applicationName = {"default", "app1", "app2"};
        String[] subscriber = {"Michael", "clarke", "cook"};
        String[] throttledOutReason = {"SUBSCRIPTION_LIMIT_EXCEEDED", "APPLICATION_LIMIT_EXCEEDED"};

        int index = ThreadLocalRandom.current().nextInt(0, 9);

        throttledObj.username = username[index % 9];
        throttledObj.userTenantDomain = "carbon.super";
        throttledObj.apiName = apiName[index % 9];
        throttledObj.apiVersion = "1.0";
        throttledObj.apiContext = throttledObj.apiName + "/" + throttledObj.apiVersion;
        throttledObj.apiCreator = apiCreator[index % 3];
        throttledObj.apiCreatorTenantDomain = "carbon.super";
        throttledObj.applicationId = applicationId[index % 3];
        throttledObj.applicationName = applicationName[index % 3];
        throttledObj.subscriber = subscriber[index % 2];
        throttledObj.throttledOutReason = throttledOutReason[index % 2];
        throttledObj.gatewayType = "g1";
        throttledObj.throttledOutTimestamp = new Timestamp(System.currentTimeMillis()).getTime();
        throttledObj.hostname = "localhost";

        return (new Object[]{
                throttledObj.username,
                throttledObj.userTenantDomain,
                throttledObj.apiName,
                throttledObj.apiVersion,
                throttledObj.apiContext,
                throttledObj.apiCreator,
                throttledObj.apiCreatorTenantDomain,
                throttledObj.applicationId,
                throttledObj.applicationName,
                throttledObj.subscriber,
                throttledObj.throttledOutReason,
                throttledObj.gatewayType,
                throttledObj.throttledOutTimestamp,
                throttledObj.hostname
        });
    }

    private static Object[] getFaultStream()
    {
        int index = ThreadLocalRandom.current().nextInt(0, 9);
        FaultDTO faultObj = new FaultDTO();
        faultObj.applicationConsumerKey = applicationCK[index % applicationCK.length];
        faultObj.apiName = apiName[index % 3];
        faultObj.apiVersion = "1.0";
        faultObj.apiContext = "get";
        faultObj.apiResourcePath = "/temp";
        faultObj.apiMethod = apiMethod[index % apiMethod.length];
        faultObj.apiCreator = apiCreator[index % apiCreator.length];
        faultObj.username = username[index % username.length];
        faultObj.userTenantDomain = tenantDomain[index % tenantDomain.length];
        faultObj.apiCreatorTenantDomain = tenantDomain[index % tenantDomain.length];
        faultObj.hostname="loca";
        faultObj.applicationId = applicationId[index % applicationName.length];
        faultObj.applicationName = applicationName[index % applicationName.length];
        faultObj.protocol="https";
        faultObj.errorCode="403";
        faultObj.errorMessage="notfound";
        faultObj.requestTimestamp = new Timestamp(System.currentTimeMillis()).getTime();

        return( new Object[]{
              faultObj.applicationConsumerKey,
              faultObj.apiName,
              faultObj.apiVersion,
              faultObj.apiContext,
              faultObj.apiResourcePath,
              faultObj.apiMethod,
              faultObj.apiCreator,
              faultObj.username,
              faultObj.userTenantDomain,
              faultObj.apiCreatorTenantDomain,
              faultObj.hostname,
              faultObj.applicationId,
              faultObj.applicationName,
              faultObj.protocol,
              faultObj.errorCode,
              faultObj.errorMessage,
              faultObj.requestTimestamp
        });

    }

    public static Object[] getRequestStream()
    {
        RequestDTO requestObj = new RequestDTO();
        Boolean[] throttledOut = {true, false, true, true, true, true, true, true, true};
        long[] responseTime = {1,6,2};
        int[] responseCode = {200,403,200,200,200,200,200,200,550};

        int index = ThreadLocalRandom.current().nextInt(0, 9);

        requestObj.applicationConsumerKey = applicationCK[index % 3];
        requestObj.applicationName = applicationName[index % 3];
        requestObj.applicationId = applicationId[index % 3];
        requestObj.applicationOwner = applicationOwner[index % 3];
        requestObj.apiContext = apiName[index % 9]+"/"+"1.0";
        requestObj.apiName = apiName[index % 9];
        requestObj.apiVersion = "1.0";
        requestObj.apiResourcePath = requestObj.apiName+"/"+apiMethod[index % 3];
        requestObj.apiResourceTemplate = requestObj.apiName+"/"+apiMethod[index % 3];
        requestObj.apiMethod = apiMethod[index % 3];
        requestObj.apiCreator = apiCreator[index % 3];
        requestObj.apiCreatorTenantDomain = tenantDomain[index % 3];
        requestObj.apiTier = "unlimited";
        requestObj.apiHostname = "localhost";
        requestObj.username = "default";
        requestObj.userTenantDomain = requestObj.apiCreatorTenantDomain;
        requestObj.userIp = userIp[index % userIp.length];
        requestObj.userAgent = "Mozilla";
        requestObj.requestTimestamp = new Timestamp(System.currentTimeMillis()).getTime();
        requestObj.throttledOut = throttledOut[index % 9];
        requestObj.responseTime = responseTime[index % 3];
        requestObj.serviceTime = (long)2;
        requestObj.backendTime= (long)2;
        requestObj.responseCacheHit = false;
        requestObj.responseSize = (long)2;
        requestObj.protocol = "Https";
        requestObj.responseCode = responseCode[index % 3];
        requestObj.destination = "www.loc.com";
        requestObj.securityLatency = (long)2;
        requestObj.throttlingLatency = (long)2;
        requestObj.requestMedLat = (long)2;
        requestObj.responseMedLat = (long)2;
        requestObj.backendLatency = (long)2;
        requestObj.otherLatency = (long)2;
        requestObj.gatewayType = "SYNAPSE";
        requestObj.label = "SYNAPSE";

        return (new Object[]{
           requestObj.applicationConsumerKey,
           requestObj.applicationName,
           requestObj.applicationId,
           requestObj.applicationOwner,
           requestObj.apiContext,
           requestObj.apiName,
           requestObj.apiVersion,
           requestObj.apiResourcePath,
           requestObj.apiResourceTemplate,
           requestObj.apiMethod,
           requestObj.apiCreator,
           requestObj.apiCreatorTenantDomain,
           requestObj.apiTier,
           requestObj.apiHostname,
           requestObj.username,
           requestObj.userTenantDomain,
           requestObj.userIp,
           requestObj.userAgent,
           requestObj.requestTimestamp,
           requestObj.throttledOut,
           requestObj.responseTime,
           requestObj.serviceTime,
           requestObj.backendTime,
           requestObj.responseCacheHit ,
           requestObj.responseSize ,
           requestObj.protocol ,
           requestObj.responseCode ,
           requestObj.destination ,
           requestObj.securityLatency ,
           requestObj.throttlingLatency ,
           requestObj.requestMedLat ,
           requestObj.responseMedLat ,
           requestObj.backendLatency ,
           requestObj.otherLatency ,
           requestObj.gatewayType ,
           requestObj.label
        });
    }
}
