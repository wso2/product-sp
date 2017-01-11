package org.wso2.streamprocessor.core.internal;

import org.osgi.framework.BundleContext;
import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.streamprocessor.core.StreamProcessorService;

/**
 * StreamProcessorDataHolder to hold org.wso2.carbon.kernel.CarbonRuntime instance referenced through
 * org.wso2.carbon.helloworld.internal.ServiceComponent.
 *
 * @since 1.0.0
 */
public class StreamProcessorDataHolder {

    private static StreamProcessorDataHolder instance = new StreamProcessorDataHolder();
    private CarbonRuntime carbonRuntime;
    private Constants.RuntimeMode runtimeMode = Constants.RuntimeMode.ERROR;
    private BundleContext bundleContext;

    private static SiddhiManager siddhiManager;
    private static StreamProcessorService streamProcessorService;

    private StreamProcessorDataHolder() {

    }

    /**
     * This returns the StreamProcessorDataHolder instance.
     *
     * @return The StreamProcessorDataHolder instance of this singleton class
     */
    public static StreamProcessorDataHolder getInstance() {
        return instance;
    }

    /**
     * Returns the CarbonRuntime service which gets set through a service component.
     *
     * @return CarbonRuntime Service
     */
    public CarbonRuntime getCarbonRuntime() {
        return carbonRuntime;
    }

    /**
     * This method is for setting the CarbonRuntime service. This method is used by
     * ServiceComponent.
     *
     * @param carbonRuntime The reference being passed through ServiceComponent
     */
    public void setCarbonRuntime(CarbonRuntime carbonRuntime) {
        this.carbonRuntime = carbonRuntime;
    }

    public Constants.RuntimeMode getRuntimeMode() {
        return runtimeMode;
    }

    public void setRuntimeMode(Constants.RuntimeMode runtimeMode) {
        this.runtimeMode = runtimeMode;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public static SiddhiManager getSiddhiManager() {
        return siddhiManager;
    }

    public static void setSiddhiManager(SiddhiManager siddhiManager) {
        StreamProcessorDataHolder.siddhiManager = siddhiManager;
    }

    public static StreamProcessorService getStreamProcessorService() {
        return streamProcessorService;
    }

    public static void setStreamProcessorService(StreamProcessorService streamProcessorService) {
        StreamProcessorDataHolder.streamProcessorService = streamProcessorService;
    }
}
