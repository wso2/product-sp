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

package org.wso2.extension.siddhi.execution.throughput.util.filewriting;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Writer;

/**
 * Writes Throughput calculations in the csv file.
 */

public class ThroughputFileWriting implements Runnable {
    private final Logger log = Logger.getLogger(ThroughputFileWriting.class);
    private long firstTupleTime;
    private int recordWindow;
    private long currentTime;
    private long value;
    private long eventCountTotal;
    private long eventCount;
    private Writer fstream;

    public ThroughputFileWriting(long firstTupleTime, int recordWindow, long eventCountTotal, long
            eventCount, long currentTime, long value, Writer fstream) {
        this.firstTupleTime = firstTupleTime;
        this.recordWindow = recordWindow;
        this.eventCountTotal = eventCountTotal;
        this.eventCount = eventCount;
        this.currentTime = currentTime;
        this.value = value;
        this.fstream = fstream;
    }

    @Override public void run() {
        try {
            fstream.write(
                    (eventCountTotal / recordWindow) + "," + ((eventCount * 1000) / value) + ","
                            +
                            (eventCountTotal * 1000 / (currentTime - firstTupleTime)) + "," +
                            ((currentTime - firstTupleTime) / 1000f) + ","
                            + "" + eventCountTotal + "," + currentTime);
            fstream.write("\r\n");
            fstream.flush();
        } catch (IOException ex) {
            log.error("Error while writing into the file" + ex.getMessage(), ex);
        }
    }
}
