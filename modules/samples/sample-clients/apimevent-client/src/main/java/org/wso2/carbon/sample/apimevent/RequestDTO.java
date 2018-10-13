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

/**
 * DTO For Request Stream
 */
public class RequestDTO {

    String applicationConsumerKey;
    String applicationName;
    String applicationId;
    String applicationOwner;
    String apiContext;
    String apiName;
    String apiVersion;
    String apiResourcePath;
    String apiResourceTemplate;
    String apiMethod;
    String apiCreator;
    String apiCreatorTenantDomain;
    String apiTier;
    String apiHostname;
    String username;
    String userTenantDomain;
    String userIp;
    String userAgent;
    Long requestTimestamp;
    Boolean throttledOut;
    Long responseTime;
    Long serviceTime;
    Long backendTime;
    Boolean responseCacheHit;
    Long responseSize;
    String protocol;
    int responseCode;
    String destination;
    Long securityLatency;
    Long throttlingLatency;
    Long requestMedLat;
    Long responseMedLat;
    Long backendLatency;
    Long otherLatency;
    String gatewayType;
    String label;
}
