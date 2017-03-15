package org.wso2.carbon.das.jobmanager.core.factories;

import org.wso2.carbon.das.jobmanager.core.WorkersApiService;
import org.wso2.carbon.das.jobmanager.core.impl.WorkersApiServiceImpl;

public class WorkersApiServiceFactory {
    private static final WorkersApiService service = new WorkersApiServiceImpl();

    public static WorkersApiService getWorkersApi() {
        return service;
    }
}
