package org.wso2.extension.siddhi.execution.filterwords.function;

import org.apache.log4j.Logger;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.core.util.SiddhiTestHelper;

import java.util.concurrent.atomic.AtomicInteger;

public class TestCaseOfFilterWordsFunction {
    private static final Logger LOGGER = Logger.getLogger(TestCaseOfFilterWordsFunction.class);
    private AtomicInteger count = new AtomicInteger(0);
    private volatile boolean eventArrived;

    @BeforeMethod
    public void init() {
        count.set(0);
        eventArrived = false;
    }

    @Test
    public void testFilterWordsFunctionExtension() throws InterruptedException {
        LOGGER.info("FilterwordsFunctionExtension TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (text string);";
        String query = ("@info(name = 'query1') from inputStream select text:filterwords(text) as words " +
                "insert into outputStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        AssertJUnit.assertEquals("Shopping,super", event.getData(0));
                        eventArrived = true;
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("inputStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"Shopping is super #Amazon @Amazon"});
        SiddhiTestHelper.waitForEvents(100, 1, count, 60000);
        AssertJUnit.assertEquals(1, count.get());
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void testFilterWordsFunctionExtensionWithZeroArguments() {
        LOGGER.info("FilterwordsFunctionExtension TestCase with zero arguments");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (text string);";
        String query = ("@info(name = 'query1') from inputStream select text:filterwords() as words " +
                "insert into outputStream;");
        siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void testFilterWordsFunctionExtensionWithInvalidDataType() {
        LOGGER.info("FilterwordsFunctionExtension TestCase with invalid data type");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (text double);";
        String query = ("@info(name = 'query1') from inputStream select text:filterwords(text) as words " +
                "insert into outputStream;");
        siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
    }

    @Test
    public void testFilterWordsFunctionExtensionWithNullValue() throws InterruptedException {
        LOGGER.info("FilterwordsFunctionExtension TestCase with null value");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (text string);";
        String query = ("@info(name = 'query1') from inputStream select text:filterwords(text) as words " +
                "insert into outputStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("inputStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{null});
        siddhiAppRuntime.shutdown();
    }

}
