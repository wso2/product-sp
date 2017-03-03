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

package org.wso2.eventsimulator.core.util;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.event.Event;
import org.wso2.eventsimulator.core.internal.EventSimulatorDataHolder;

import java.util.Collections;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class EventSender {
    private static final Logger log = Logger.getLogger(EventSender.class);
    private static final EventSender instance = new EventSender();
    private final Map<String, AtomicInteger> taskCounter = new ConcurrentHashMap<>();
    private int minQueueSize = 4;
    private int queueLength = 20;
    private Map<String, Queue<QueuedEvent>> eventQueue = new ConcurrentHashMap<>();

    private EventSender() {
    }

    public static EventSender getInstance() {
        return instance;
    }

    public void addSimulator(String streamName) {
        synchronized (taskCounter) {
            AtomicInteger counter;
            if (!taskCounter.containsKey(streamName)) {
                counter = new AtomicInteger(0);
                taskCounter.put(streamName, counter);
            } else {
                counter = taskCounter.get(streamName);
            }
            counter.incrementAndGet();
        }
    }

//    todo 01/03/2017 is it alright to simply add the execution plan name when flushing. or does the implementation need to be changed.
    public void removeSimulator(String executionPlanName,String streamName) {
        synchronized (taskCounter) {
            if (taskCounter.get(streamName).decrementAndGet() == 0) {
                flush(executionPlanName,streamName);
                taskCounter.remove(streamName);
            }
        }
    }

    /**
     * This sendEvent method is used if orderByTimestamp flag is set to true.
     * If a queue (a priorityBlockingQueue) has not been already created for the specified stream name,
     * it will create a new queue and add it to the eventQueue map.
     * The event will be sorted upon insertion to the queue.
     * When the size of the queue exceed the minQueueSize (i.e. 2 * number of parallel simulations), events
     * will be sent to the stream processor via sendEvent(executionPlanName, StreamName, event) method.
     *
     * @param executionPlanName : name of the execution plan being simulated
     * @param streamName        : the name of the stream to which the event must be sent
     * @param event             : the event produced
     * */
    public void sendEvent(String executionPlanName, String streamName,QueuedEvent event) {
        synchronized (this) {
            Queue<QueuedEvent> queue = eventQueue.get(streamName);
            if (queue == null) {
                queue = new PriorityBlockingQueue<>(queueLength, Collections.reverseOrder());
                eventQueue.putIfAbsent(streamName, queue);
            }
            queue.add(event);
            if (queue.size() > minQueueSize) {
                sendEvent(executionPlanName, streamName, queue.poll().getEvent());
            }
        }
    }

    /**
     * This sendEvent method is used if orderByTimestamp flag is set to false.
     * This method sends the execution plan name, stream name and the produced event to the stream processor.
     *
     * @param executionPlanName : name of the execution plan being simulated
     * @param streamName        : the name of the stream to which the event must be sent
     * @param event             : the event produced
     * */

    public void sendEvent(String executionPlanName, String streamName,Event event) {

        EventSimulatorDataHolder.getInstance().getEventReceiverService().eventReceiverService(executionPlanName,streamName,event);

    }

    /**
     * flush() is used to clear the queue once all data sources have finished generating events.
     * It sends all events remaining in the queue to sendEvent(executionPlanName, StreamName, event) method.
     *
     * @param executionPlanName : name of the execution plan being simulated
     * @param streamName        : the name of the stream to which the event must be sent
     * */
    public void flush(String executionPlanName,String streamName) {
        synchronized (this) {
            Queue<QueuedEvent> queue = eventQueue.get(streamName);
            if (queue != null) {
                for (QueuedEvent event : queue) {
                    sendEvent(executionPlanName, streamName,queue.poll().getEvent());
                }
            }
        }
    }

    public void setMinQueueSize(int minQueueSize) {
        this.minQueueSize = minQueueSize;
    }

    public void setQueueLength(int queueLength) {
        this.queueLength = queueLength;
    }
}

