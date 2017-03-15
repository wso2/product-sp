package org.wso2.carbon.analytics.jobmanager.restapi.factories;

import org.wso2.carbon.analytics.jobmanager.restapi.WorkersApiService;
import org.wso2.carbon.analytics.jobmanager.restapi.impl.WorkersApiServiceImpl;

public class WorkersApiServiceFactory {
    private static final WorkersApiService service = new WorkersApiServiceImpl();

    public static WorkersApiService getWorkersApi() {
        return service;
    }
}
