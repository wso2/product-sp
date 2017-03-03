/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.streamprocessor.core.internal;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.streamprocessor.core.*;
import org.wso2.siddhi.core.SiddhiManagerService;

import java.io.File;
import java.util.Map;

/**
 * Service component to consume CarbonRuntime instance which has been registered as an OSGi service
 * by Carbon Kernel.
 */
@Component(
        name = "stream-processor-core-service",
        immediate = true
)
public class ServiceComponent {

    public static final Logger log = LoggerFactory.getLogger(ServiceComponent.class);
    private ServiceRegistration serviceRegistration;

    /**
     * This is the activation method of ServiceComponent. This will be called when its references are
     * satisfied.
     *
     * @param bundleContext the bundle context instance of this bundle.
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start(BundleContext bundleContext) throws Exception {
        log.info("Service Component is activated");

        String runningFileName = System.getProperty(Constants.SYSTEM_PROP_RUN_FILE);
        String runtimeMode = System.getProperty(Constants.SYSTEM_PROP_RUN_MODE);

        // Create Stream Processor Service
        StreamProcessorDataHolder.setStreamProcessorService(new StreamProcessorService());

        File runningFile;

        if (runtimeMode != null && runtimeMode.equalsIgnoreCase(Constants.SYSTEM_PROP_RUN_MODE_RUN)) {
            StreamProcessorDataHolder.getInstance().setRuntimeMode(Constants.RuntimeMode.RUN_FILE);
            if (runningFileName == null || runningFileName.trim().equals("")) {
                // Can't Continue. We shouldn't be here. that means there is a bug in the startup script.
                log.error("Error: Can't get target file(s) to run. System property {} is not set.",
                        Constants.SYSTEM_PROP_RUN_FILE);
                StreamProcessorDataHolder.getInstance().setRuntimeMode(Constants.RuntimeMode.ERROR);
                return;
            }
            runningFile = new File(runningFileName);
            if (!runningFile.exists()) {
                log.error("Error: File " + runningFile.getName() + " not found in the given location.");
                StreamProcessorDataHolder.getInstance().setRuntimeMode(Constants.RuntimeMode.ERROR);
                return;
            }
            StreamProcessorDeployer.deploySiddhiQLFile(runningFile);
        } else {
            StreamProcessorDataHolder.getInstance().setRuntimeMode(Constants.RuntimeMode.SERVER);
        }

        if (log.isDebugEnabled()) {
            log.debug("Runtime mode is set to : " + StreamProcessorDataHolder.getInstance().getRuntimeMode());
        }

        if (log.isDebugEnabled()) {
            log.debug("WSO2 Stream Processor runtime started...!");
        }

       /* Timer time = new Timer(); // Instantiate Timer Object
        ScheduledTask st = new ScheduledTask(); // Instantiate SheduledTask class
        time.schedule(st, 0, 5000);*/

        serviceRegistration = bundleContext.registerService(StreamDefinitionService.class.getName(),new StreamDefinitionServiceImpl(),null);
        serviceRegistration = bundleContext.registerService(EventStreamService.class.getName(),new CarbonEventStreamService(),null);
    }

    /**
     * This is the deactivation method of ServiceComponent. This will be called when this component
     * is being stopped or references are satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
        log.info("Service Component is deactivated");

        Map<String, ExecutionPlanRuntime> executionPlanRunTimeMap = StreamProcessorDataHolder.getStreamProcessorService().getExecutionPlanRunTimeMap();
        for (ExecutionPlanRuntime runtime : executionPlanRunTimeMap.values()) {
            runtime.shutdown();
        }

        serviceRegistration.unregister();
    }

    /**
     * This bind method will be called when CarbonRuntime OSGi service is registered.
     *
     * @param carbonRuntime The CarbonRuntime instance registered by Carbon Kernel as an OSGi service
     */
    @Reference(
            name = "carbon.runtime.service",
            service = CarbonRuntime.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetCarbonRuntime"
    )
    protected void setCarbonRuntime(CarbonRuntime carbonRuntime) {
        StreamProcessorDataHolder.getInstance().setCarbonRuntime(carbonRuntime);
    }

    /**
     * This is the unbind method which gets called at the un-registration of CarbonRuntime OSGi service.
     *
     * @param carbonRuntime The CarbonRuntime instance registered by Carbon Kernel as an OSGi service
     */
    protected void unsetCarbonRuntime(CarbonRuntime carbonRuntime) {
        StreamProcessorDataHolder.getInstance().setCarbonRuntime(null);
    }

    /**
     * This bind method will be called when SiddhiManagerService OSGi service is registered.
     *
     * @param siddhiManager The SiddhiManager instance registered by Siddhi Core as an OSGi service
     */
    @Reference(
            name = "siddhi.manager.core",
            service = SiddhiManagerService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSiddhiManager"
    )
    protected void setSiddhiManager(SiddhiManagerService siddhiManager) {
        StreamProcessorDataHolder.getInstance().setSiddhiManager(siddhiManager);
    }

    /**
     * This is the unbind method which gets called at the un-registration of SiddhiManager OSGi service.
     *
     * @param siddhiManager The SiddhiManager instance registered by Siddhi Core as an OSGi service
     */
    protected void unsetSiddhiManager(SiddhiManagerService siddhiManager) {
        StreamProcessorDataHolder.getInstance().setSiddhiManager(null);
    }

}