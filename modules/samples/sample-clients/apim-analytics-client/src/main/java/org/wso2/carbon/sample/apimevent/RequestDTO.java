package org.wso2.carbon.sample.apimevent;

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
