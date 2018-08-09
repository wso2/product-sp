/*
 * Copyright (c)  2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.extension.siddhi.execution.tokenizer.streamprocessor;

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

public class TestCaseOfTweetTextTokenizer {
    private static final Logger LOGGER = Logger.getLogger(TestCaseOfTweetTextTokenizer.class);
    private AtomicInteger count = new AtomicInteger(0);
    private volatile boolean eventArrived;

    @BeforeMethod
    public void init() {
        count.set(0);
        eventArrived = false;
    }

    @Test
    public void testTokenizerExtension() throws InterruptedException {
        LOGGER.info("TweetTextTokenizerStreamProcessorExtension TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (data string);";
        String query = ("@info(name = 'query1') from inputStream#text:tokenize(data)" +
                "insert all events into outputStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        AssertJUnit.assertArrayEquals(new Object[]
                                        {"Journalists, this isn't how you use a comma. It drives me" +
                                                " mad \uD83D\uDC2D \uD83D\uDE00, the way it interrupts the flow" +
                                                " of the headline. What would be so wrong with using the word " +
                                                "\"and\" there? https://t.co/VOl7zMk9Ar", "Journalists"},
                                inEvents[0].getData());
                        eventArrived = true;
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("inputStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"Journalists, this isn't how you use a" +
                " comma. It drives me mad \uD83D\uDC2D \uD83D\uDE00, the way it interrupts the flow of the headline." +
                " What would be so wrong with using the word \"and\" there? https://t.co/VOl7zMk9Ar"});
        inputHandler.send(new Object[]{"Shopping @Amazon #Amazon"});
        SiddhiTestHelper.waitForEvents(100, 13, count, 60000);
        AssertJUnit.assertEquals(eventArrived, true);
        AssertJUnit.assertEquals(13, count.get());
        siddhiAppRuntime.shutdown();
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void testTokenizerExtensionWithZeroArguments() {
        LOGGER.info("TweetTextTokenizerExtension TestCase with Zero arguments");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (text2 string);";
        String query = ("@info(name = 'query1') from inputStream#text:tokenize()" +
                "insert into outputStream;");
        siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void testTokenizerExtensionWithInvalidDataTypeforText() {
        LOGGER.info("TweetTextTokenizerExtension TestCase with invalid data type for text");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (text2 double);";
        String query = ("@info(name = 'query1') from inputStream#text:tokenize(text2) " +
                "insert into outputStream;");
        siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
    }

    @Test
    public void testTokenizerExtensionWithNullValue() throws InterruptedException {
        LOGGER.info("TweetTextTokenizerExtension TestCase with null value");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (text2 string);";
        String query = ("@info(name = 'query1') from inputStream#text:tokenize(text2)  " +
                "insert into outputStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("inputStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{null});
        siddhiAppRuntime.shutdown();
    }
}
