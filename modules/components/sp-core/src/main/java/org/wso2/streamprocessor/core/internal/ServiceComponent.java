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
import org.wso2.carbon.kernel.utils.Utils;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.transport.PassThroughOutputMapper;
import org.wso2.siddhi.extension.output.mapper.text.TextOutputMapper;
import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.execution.io.Transport;
import org.wso2.siddhi.query.api.execution.io.map.Mapping;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.output.stream.OutputStream;
import org.wso2.siddhi.query.api.execution.query.selection.Selector;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.streamprocessor.core.Greeter;
import org.wso2.streamprocessor.core.GreeterImpl;
import org.wso2.streamprocessor.core.StreamProcessorDeployer;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Service component to consume CarbonRuntime instance which has been registered as an OSGi service
 * by Carbon Kernel.
 */
@Component(
        name = "org.wso2.streamprocessor.core.internal.ServiceComponent",
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
        log.info("*************" +runningFileName);

        // Register GreeterImpl instance as an OSGi service.
        serviceRegistration = bundleContext.registerService(Greeter.class.getName(), new GreeterImpl("WSO2"), null);
        testPublisherWithSelector();

        Path deploymentDir = Paths.get(Utils.getCarbonHome().toString(), "deployment", StreamProcessorDeployer.SIDDHIQL_FILES_DIRECTORY);

        runningFileName = deploymentDir.toString();
        if (log.isDebugEnabled()) {
            log.debug("SiddhiQL is running is carbon server mode. You SHOULDN'T run in this mode...!");
        }

        File runningFile = new File(runningFileName);

        StreamProcessorDeployer.deploySiddhiQLFile(runningFile);


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

        // Unregister Greeter OSGi service
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
        DataHolder.getInstance().setCarbonRuntime(carbonRuntime);
    }

    /**
     * This is the unbind method which gets called at the un-registration of CarbonRuntime OSGi service.
     *
     * @param carbonRuntime The CarbonRuntime instance registered by Carbon Kernel as an OSGi service
     */
    protected void unsetCarbonRuntime(CarbonRuntime carbonRuntime) {
        DataHolder.getInstance().setCarbonRuntime(null);
    }

    /**
     * This is the unbind method which gets called at the un-registration of CarbonRuntime OSGi service.
     */
    public void testPublisherWithSelector() throws InterruptedException {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("outputmapper:text", PassThroughOutputMapper.class);
        String streams = "" +
                         "@Plan:name('TestExecutionPlan')" +
                         "define stream FooStream (symbol string, price float, volume long); ";

        String query = "" +
                       "from FooStream " +
                       "select symbol " +
                       "publish test options (topic '{{symbol}}') " +
                       "map text for all events; ";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
        InputHandler stockStream = executionPlanRuntime.getInputHandler("FooStream");

        executionPlanRuntime.start();
        stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
        stockStream.send(new Object[]{"IBM", 75.6f, 100L});
        stockStream.send(new Object[]{"WSO2", 57.6f, 100L});
        Thread.sleep(100);

        executionPlanRuntime.shutdown();
    }
}
