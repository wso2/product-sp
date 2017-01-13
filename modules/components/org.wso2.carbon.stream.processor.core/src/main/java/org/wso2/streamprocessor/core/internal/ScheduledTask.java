package org.wso2.streamprocessor.core.internal;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.stream.input.InputHandler;

import java.util.Map;
import java.util.TimerTask;


//TODO Temporary class
public class ScheduledTask extends TimerTask {

    public static final Logger log = LoggerFactory.getLogger(ScheduledTask.class);

    @Override
    public void run() {
        Map<String, ExecutionPlanRuntime> executionPlanRunTimeMap = StreamProcessorDataHolder.getStreamProcessorService().getExecutionPlanRunTimeMap();
        for (ExecutionPlanRuntime runtime : executionPlanRunTimeMap.values()) {

            //TODO temp
            InputHandler stockStream = runtime.getInputHandler("FooStream");

            try {
                stockStream.send(new Object[]{"WSO2", 55.6f, 100L});
                stockStream.send(new Object[]{"IBM", 75.6f, 100L});
                stockStream.send(new Object[]{"WSO2", 57.6f, 100L});

            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
