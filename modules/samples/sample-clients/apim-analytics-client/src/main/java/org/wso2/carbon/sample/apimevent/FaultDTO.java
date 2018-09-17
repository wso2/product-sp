package org.wso2.carbon.sample.apimevent;

import java.sql.Timestamp;

public class FaultDTO {

    String applicationConsumerKey;
    String apiName;
    String apiVersion;
    String apiContext;
    String apiResourcePath;
    String apiMethod;
    String apiCreator;
    String username;
    String userTenantDomain;
    String apiCreatorTenantDomain;
    String hostname;
    String applicationId;
    String applicationName;
    String protocol;
    String errorCode;
    String errorMessage;
    Long requestTimestamp;
}
