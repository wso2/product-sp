/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.extension.siddhi.execution.throughput;

import org.HdrHistogram.Histogram;
import org.apache.log4j.Logger;
import org.wso2.extension.siddhi.execution.throughput.util.filewriting.BothFileWriting;
import org.wso2.extension.siddhi.execution.throughput.util.filewriting.LatencyFileWriting;
import org.wso2.extension.siddhi.execution.throughput.util.filewriting.ThroughputFileWriting;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.populater.ComplexEventPopulater;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.stream.StreamProcessor;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Input attributes to log is (iijTimeStamp (Long), value (Float)).
 */
@Extension(
        name = "throughput",
        namespace = "throughput",
        description = "Measuring performance of stream processor with simple passthrough",
        parameters = {
                @Parameter(name = "iijtimestamp",
                           description = "This value used to find the sending timestamp from client",
                           type = {DataType.LONG}),
        },
        examples = {
                @Example(
                        syntax = "@App:name(\"TCP_Benchmark\")\n" +
                                "@source(type = 'tcp', context='inputStream',@map(type='binary'))\n" +
                                "define stream outputStream (iijtimestamp long,value float);\n" +
                                "from inputStream\n" +
                                "select iijtimestamp,value\n" +
                                "insert into tempStream;" +
                                "from tempStream#throughput:throughput(iijtimestamp,value)\n" +
                                "select \"aa\" as tempTrrb\n" +
                                "insert into tempStream1;",
                        description = "This is a simple passthrough query that inserts iijtimestamp (long) and random "
                                + "number(float) into the temp stream  "
                ),
                @Example(
                        syntax = "@App:name(\"TCP_Benchmark\")\n"
                                + "@source(type = 'tcp', context='inputStream',@map(type='binary'))\n"
                                + "define stream inputStream (iijtimestamp long,value float);\n"
                                + "define stream outputStream (iijtimestamp long,value float,mode String);\n"
                                + "from inputStream[value<=0.25]\n"
                                + "select iijtimestamp,value\n"
                                + "insert into tempStream;\n"
                                + "from tempStream#throughput:throughput(iijtimestamp,value,\"both\")\n"
                                + "select \"aa\" as tempTrrb\n"
                                + "insert into tempStream1;",
                        description = "This is a filter query"
                )
        }
)
public class CalculatePerformanceStreamProcessorExtension extends StreamProcessor {
    private static final Logger log = Logger.getLogger(CalculatePerformanceStreamProcessorExtension.class);
    private static final int RECORDWINDOW = 5000000;
    private static final Histogram histogram = new Histogram(2);
    private static final Histogram histogram2 = new Histogram(2);
    private static long firstTupleTime = -1;
    private static String logDir = "./PatternMatching-Results";
    private static long eventCountTotal = 0;
    private static long eventCount = 0;
    private static long timeSpent = 0;
    private static long totalTimeSpent = 0;
    private static long outputFileTimeStamp;
    private static long startTime = -1;
    private static Writer fstream = null;
    private static boolean exitFlag = false;
    private static int sequenceNumber = 0;
    private String executionType;
    private ExecutorService executorService;
    private boolean flag;

    private static int setCompletedFlag(int sequenceNumber) {
        try {
            String content = "" + sequenceNumber;
            File file = new File(logDir + "/completed-number.txt");
            //if file doesn't exists, then create new file
            if (!file.exists()) {
                boolean fileCreateResults = file.createNewFile();
                if (!fileCreateResults) {
                    log.error("Error when creating completed-number.txt file.");
                }
            }
            Writer fstream =
                    new OutputStreamWriter(new FileOutputStream(file.getAbsoluteFile()), StandardCharsets.UTF_8);

            fstream.write(content);
            fstream.flush();
            fstream.close();
        } catch (IOException e) {
            log.error("Error when writing performance information" + e.getMessage(), e);
        }
        return 0;
    }

    /**
     * This method returns a unique integer that can be used as a sequence number for log files.
     */

    private static int getLogFileSequenceNumber() {
        int results = -1;
        BufferedReader br = null;
        try {
            String sCurrentLine;
            File directory = new File(logDir);

            if (!directory.exists()) {
                if (!directory.mkdir()) {
                    log.error("Error while creating the output directory");
                }
            }

            File sequenceFile = new File(logDir + "/sequence-number.txt");

            if (sequenceFile.exists()) {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(logDir + "/sequence-number.txt"),
                                                              Charset.forName("UTF-8")));

                while ((sCurrentLine = br.readLine()) != null) {
                    results = Integer.parseInt(sCurrentLine);
                    break;
                }
            }
        } catch (IOException e) {
            log.error("Error when reading the sequence number from sequence-number.txt" + e.getMessage(), e);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        try {
            if (results == -1) {
                results = 0;
            }

            String content = "" + (results + 1); //need to increment for next iteration
            File file = new File(logDir + "/sequence-number.txt");

            //if file doesn't exists, then create it
            if (!file.exists()) {
                boolean fileCreateResults = file.createNewFile();
                if (!fileCreateResults) {
                    log.error("Error when creating sequence-number.txt file.");
                }
            }
            Writer fstream =
                    new OutputStreamWriter(new FileOutputStream(file.getAbsoluteFile()), StandardCharsets.UTF_8);
            fstream.write(content);
            fstream.flush();
            fstream.close();
        } catch (IOException ex) {
            log.error("Error when writing performance information" + ex.getMessage(), ex);
        }
        return results;
    }

    /**
     * The init method of the StreamFunction.
     *
     * @param inputDefinition              the incoming stream definition
     * @param attributeExpressionExecutors the executors for the function parameters
     * @param siddhiAppContext             siddhi app context
     * @param configReader                 this hold the {@link} configuration reader.
     * @return the additional output attributes introduced by the function
     */
    @Override
    protected List<Attribute> init(AbstractDefinition inputDefinition, ExpressionExecutor[]
            attributeExpressionExecutors, ConfigReader configReader, SiddhiAppContext siddhiAppContext) {
        executorService = siddhiAppContext.getExecutorService();

        if (attributeExpressionLength == 2) {
            if (!(attributeExpressionExecutors[0] instanceof VariableExpressionExecutor)) {
                throw new SiddhiAppValidationException("iijTimeStamp has to be a variable but found " +
                                                               this.attributeExpressionExecutors[0].getClass()
                                                                       .getCanonicalName());
            }

            if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.LONG) {

            } else {
                throw new SiddhiAppValidationException("iijTimestamp is expected to be long but "
                                                               + "found" + attributeExpressionExecutors[0]
                        .getReturnType());

            }

            if (!(attributeExpressionExecutors[1] instanceof ConstantExpressionExecutor)) {
                throw new SiddhiAppValidationException("second parameter has to be constant but found" + this
                        .attributeExpressionExecutors[1].getClass().getCanonicalName());
            }

            if (attributeExpressionExecutors[1].getReturnType() == Attribute.Type.STRING) {
                executionType = (String) ((ConstantExpressionExecutor) attributeExpressionExecutors[1]).getValue();

            } else {
                throw new SiddhiAppValidationException("Second parameter expected to be String but "
                                                               + "found" + attributeExpressionExecutors[1]
                        .getReturnType());
            }

        } else {
            throw new SiddhiAppValidationException("Input parameters for Log can be iijTimeStamp (Long), " +
                                                           "type (String), but there are " +
                                                           attributeExpressionExecutors
                                                                   .length + " in the input!");
        }
        createFile();

        return new ArrayList<Attribute>();
    }

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor,
                           StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater) {

        switch (executionType.toLowerCase()) {

            case "throughput":
                calculateThroughput(streamEventChunk);
                break;

            case "latency":
                calculateLatency(streamEventChunk);
                break;

            case "both":
                calculateThroughputAndLatency(streamEventChunk);
                break;

            default:
                log.error("executionType should be either throughput or latency or both "
                                  + "but found" + " " + executionType);
        }

        nextProcessor.process(streamEventChunk);
    }

    /**
     * This method is to calculate latency.
     *
     * @param streamEventChunk
     */

    private void calculateLatency(ComplexEventChunk<StreamEvent> streamEventChunk) {
        synchronized (this) {
            while (streamEventChunk.hasNext()) {
                StreamEvent streamEvent = streamEventChunk.next();
                try {
                    long currentTime = System.currentTimeMillis();
                    long iijTimestamp = (Long) (attributeExpressionExecutors[0].execute(streamEvent));
                    timeSpent += (currentTime - iijTimestamp);
                    eventCount++;
                    eventCountTotal++;

                    if (eventCount >= RECORDWINDOW) {
                        totalTimeSpent += timeSpent;
                        histogram2.recordValue((timeSpent));
                        histogram.recordValue(timeSpent);
                        if (!flag) {
                            flag = true;
                            fstream.write("id,Average "
                                                  + "latency "
                                                  +
                                                  "per event in this window(ms), Entire Average latency per "
                                                  + "event for the run(ms), Total "
                                                  + "number"
                                                  + " of "
                                                  +
                                                  "events received (non-atomic), timespent(in one window),"
                                                  + "totaltimespent),"
                                                  + "AVG latency from start (90),"
                                                  + "" + "AVG latency from start(95), "
                                                  + "AVG latency from start "
                                                  + "(99)," + "AVG latency in this "
                                                  + "window(90)," + "AVG latency in this window(95),"
                                                  + "AVG latency "
                                                  + "in this window(99)");
                            fstream.write("\r\n");
                        }
                        long time = timeSpent;
                        long totalTime = totalTimeSpent;
                        long event = eventCount;
                        long totalEvent = eventCountTotal;

                        LatencyFileWriting file =
                                new LatencyFileWriting(RECORDWINDOW, totalEvent, event,
                                                       time, totalTime, histogram,
                                                       histogram2, fstream);

                        executorService.submit(file);
                        histogram2.reset();
                        eventCount = 0;
                        timeSpent = 0;
                        if (!exitFlag && eventCountTotal == 100000000000L) {
                            log.info("Exit flag set");
                            setCompletedFlag(sequenceNumber);
                            exitFlag = true;
                        }
                    }

                } catch (Exception ex) {
                    log.error("Error while consuming event" + ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * This method is to calculate throughput.
     *
     * @param streamEventChunk
     */

    private void calculateThroughput(ComplexEventChunk<StreamEvent> streamEventChunk) {
        synchronized (this) {
            if (firstTupleTime == -1) {
                firstTupleTime = System.currentTimeMillis();
            }
            while (streamEventChunk.hasNext()) {
                streamEventChunk.next();
                try {
                    eventCount++;
                    eventCountTotal++;
                    if (eventCount >= RECORDWINDOW) {
                        long currentTime = System.currentTimeMillis();
                        long value = currentTime - startTime;

                        if (!flag) {
                            flag = true;
                            fstream.write("Id, Throughput in this window (thousands events/second), Entire "
                                                  + "throughput for the run (thousands events/second), Total "
                                                  + "elapsed time(s),Total Events,CurrentTime");
                            fstream.write("\r\n");
                        }

                        long event = eventCount;
                        long totalEvent = eventCountTotal;
                        ThroughputFileWriting
                                file = new ThroughputFileWriting(firstTupleTime, RECORDWINDOW, totalEvent,
                                                                 event, currentTime, value, fstream
                        );

                        executorService.submit(file);
                        startTime = -1;
                        eventCount = 0;

                        if (!exitFlag && eventCountTotal == 100000000000L) {
                            log.info("Exit flag set");
                            setCompletedFlag(sequenceNumber);
                            exitFlag = true;
                        }
                    }
                } catch (Exception ex) {
                    log.error("Error while consuming event" + ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * This method is to calculate throughput and latency at once.
     *
     * @param streamEventChunk
     */

    private void calculateThroughputAndLatency(ComplexEventChunk<StreamEvent> streamEventChunk) {
        synchronized (this) {

            if (firstTupleTime == -1) {
                firstTupleTime = System.currentTimeMillis();
            }
            while (streamEventChunk.hasNext()) {
                StreamEvent streamEvent = streamEventChunk.next();
                try {
                    if (startTime == -1) {
                        startTime = System.currentTimeMillis();
                    }

                    long currentTime = System.currentTimeMillis();
                    long iijTimestamp = (Long) (attributeExpressionExecutors[0].execute(streamEvent));
                    timeSpent += (currentTime - iijTimestamp);
                    eventCount++;
                    eventCountTotal++;

                    if (eventCount == RECORDWINDOW) {
                        currentTime = System.currentTimeMillis();
                        long value = currentTime - startTime;
                        totalTimeSpent += timeSpent;
                        histogram2.recordValue(timeSpent);
                        histogram.recordValue(timeSpent);

                        if (!flag) {
                            flag = true;
                            fstream.write("Id, Throughput in this window (thousands events/second), Entire "
                                                  + "throughput for the run (thousands events/second), Total "
                                                  + "elapsed time(s),Total Events,CurrentTime,Average "
                                                  + "latency "
                                                  +
                                                  "per event in this window(ms), Entire Average latency per "
                                                  + "event for the run(ms), "
                                                  + "AVG latency from start (90),"
                                                  + "" + "AVG latency from start(95), "
                                                  + "AVG latency from start "
                                                  + "(99)," + "AVG latency in this "
                                                  + "window(90)," + "AVG latency in this window(95),"
                                                  + "AVG latency "
                                                  + "in this window(99)");
                            fstream.write("\r\n");
                        }

                        long event = eventCount;
                        long totalEvent = eventCountTotal;
                        long time = timeSpent;
                        long totalTime = totalTimeSpent;

                        BothFileWriting
                                file = new BothFileWriting(firstTupleTime, RECORDWINDOW, totalEvent,
                                                           event, currentTime, value, fstream, time, totalTime,
                                                           histogram, histogram2
                        );

                        executorService.submit(file);
                        histogram2.reset();
                        timeSpent = 0;
                        startTime = -1;
                        eventCount = 0;

                        if (!exitFlag && eventCountTotal == 10000000000000L) {
                            log.info("Exit flag set");
                            setCompletedFlag(sequenceNumber);
                            exitFlag = true;
                        }
                    }
                } catch (Exception ex) {
                    log.error("Error while consuming event" + ex.getMessage(), ex);
                }
            }
        }
    }

    @Override
    public void start() {
        //Do nothing
    }

    @Override
    public void stop() {
        //Do nothing
    }

    @Override
    public Map<String, Object> currentState() {
        //No state
        return null;
    }

    @Override
    public void restoreState(Map<String, Object> state) {
        //Nothing to be done
    }

    /**
     * This method helps to create a file.
     */

    private void createFile() {

        try {
            File directory = new File(logDir);
            if (!directory.exists()) {
                //check whether that directory is created or not
                if (!directory.mkdir()) {
                    log.error("Error while creating the output directory.");
                }
            }
            sequenceNumber = getLogFileSequenceNumber();
            outputFileTimeStamp = System.currentTimeMillis();
            fstream = new OutputStreamWriter(new FileOutputStream(new File(logDir + "/output-" +
                                                                                   sequenceNumber + "-" +

                                                                                   (outputFileTimeStamp)
                                                                                   + ".csv")
                                                                          .getAbsoluteFile()), StandardCharsets
                                                     .UTF_8);
        } catch (IOException e) {
            log.error("Error while creating statistics output file, " + e.getMessage(), e);
        } finally {

        }
    }
}
