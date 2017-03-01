package org.wso2.eventsimulator.core.util;

/**
 * Created by ruwini on 2/2/17.
 */

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.event.Event;
import org.wso2.eventsimulator.core.internal.EventSimulatorDataHolder;
import scala.util.parsing.combinator.testing.Str;

import java.util.Collections;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

// TODO: 2/4/17 Are we sorting per configuration or per stream? For now its per stream
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

//    todo R 01/03/2017 is it alright to simply add the execution plan name when flushing. or does the implementation need to be changed.
//     todo R 16/02/2017 send the execution plan name to flush from here
    public void removeSimulator(String executionPlanName,String streamName) {
        synchronized (taskCounter) {
            if (taskCounter.get(streamName).decrementAndGet() == 0) {
                flush(executionPlanName,streamName);
                taskCounter.remove(streamName);
            }
        }
    }

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

    public void sendEvent(String executionPlanName, String streamName,Event event) {

        /*send events by calling the EventReceiverService
        EventSimulatorDataHolder.getInstance().getEventReceiverService().eventReceiverService(event.getStreamName());*/
        EventSimulatorDataHolder.getInstance().getEventReceiverService().eventReceiverService(executionPlanName,streamName,event);

    }

//    todo R 16/02/2017 send the execution plan name into flush()
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

