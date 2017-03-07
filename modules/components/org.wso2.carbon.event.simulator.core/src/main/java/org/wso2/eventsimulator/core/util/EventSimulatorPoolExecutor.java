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
import org.wso2.eventsimulator.core.internal.ServiceComponent;
import org.wso2.eventsimulator.core.simulator.EventSimulator;
import org.wso2.eventsimulator.core.simulator.bean.FeedSimulationDto;
import org.wso2.eventsimulator.core.simulator.bean.FeedSimulationStreamConfiguration;
import org.wso2.eventsimulator.core.simulator.csvFeedSimulation.CSVFileSimulationDto;
import org.wso2.eventsimulator.core.simulator.csvFeedSimulation.core.CSVFeedEventSimulator;
import org.wso2.eventsimulator.core.simulator.databaseFeedSimulation.DatabaseFeedSimulationDto;
import org.wso2.eventsimulator.core.simulator.databaseFeedSimulation.core.DatabaseFeedSimulator;
import org.wso2.eventsimulator.core.simulator.randomdatafeedsimulation.bean.RandomDataSimulationDto;
import org.wso2.eventsimulator.core.simulator.randomdatafeedsimulation.core.RandomDataEventSimulator;
import org.wso2.eventsimulator.core.simulator.singleventsimulator.SingleEventDto;
import org.wso2.eventsimulator.core.simulator.singleventsimulator.SingleEventSimulator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


/**
 * EventSimulatorPoolExecutor starts the simulation execution for single Event and
 * Feed Simulation
 */
public class EventSimulatorPoolExecutor extends ThreadPoolExecutor {
    private static final Logger log = Logger.getLogger(EventSimulatorPoolExecutor.class);
    private boolean isPaused;
    private ReentrantLock pauseLock = new ReentrantLock();
    private Condition unpaused = pauseLock.newCondition();
    private List<EventSimulator> simulators = new ArrayList<>();

    /**
     * EventSimulatorPoolExecutor
     *
     * @param configuration : FeedSimulationDto object which contains the configurations for simulations
     * @param  nThreads     : the size of the thread pool
     * */
    public EventSimulatorPoolExecutor(FeedSimulationDto configuration, int nThreads) {
        /*
          @param nThreads                              The size of the pool
          @param nThreads                              The maximum size of the pool
          @param keepAliveTime                         The amount of time you wish to keep a single task alive
          @param TimeUnit                              The unit of time that the keep alive time represents
          @param LinkedBlockingQueue<Runnable>()       The queue that holds the tasks
         */
        super(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

        /*
        Each simulation configuration may contain different types of simulations. For each simulation configuration found in
        'configuration', a respective simulator will be added to the list of simulators.
        */
        for (FeedSimulationStreamConfiguration streamConfiguration : configuration.getStreamConfigurationList()) {
            switch (streamConfiguration.getSimulationType()) {
                case RANDOM_DATA_SIMULATION:
                    simulators.add(new RandomDataEventSimulator((RandomDataSimulationDto) streamConfiguration));
                    break;
                case FILE_SIMULATION:
                    simulators.add(new CSVFeedEventSimulator((CSVFileSimulationDto) streamConfiguration));
                    break;
                case DATABASE_SIMULATION:
                    simulators.add(new DatabaseFeedSimulator((DatabaseFeedSimulationDto) streamConfiguration));
                    break;
                case SINGLE_EVENT:
                    simulators.add(new SingleEventSimulator((SingleEventDto) streamConfiguration));
                    break;
                default:
                    break;
            }
        }
        /*
        * once simulators are created for all the simulations specified in 'configuration', start execution of the simulators
        * available in 'simulators'(i.e. the list of simulators)
        * */
        simulators.forEach(this::execute);
    }

    public static EventSimulatorPoolExecutor newEventSimulatorPool(FeedSimulationDto configuration, int nThreads) {
        return new EventSimulatorPoolExecutor(configuration, nThreads);
    }

    /**
     * This method will be executed before starting the execution of each simulator in 'simulators'
     * It will call addSimulator() method of EventSender to increment the count of the data sources that
     * are currently generating events for the specified stream.
     *
     * @param t
     * @param r
     * */
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        pauseLock.lock();
        try {
            while (isPaused) unpaused.await();
        } catch (InterruptedException ie) {
            t.interrupt();
        } finally {
            pauseLock.unlock();
        }
        EventSender.getInstance().addSimulator(((EventSimulator) r).getStreamConfiguration().getStreamName());
    }

    /**
     * afterExecute() method will be called upon the completion of execution of each simulator in 'simulators'.
     * This method will call removeSimulator() method of EventSender to decrement the count of data sources currently
     * generating events for the specified stream
     *
     * @param t
     * @param r
     * */
    protected void afterExecute(Runnable r, Throwable t) {
        try {
            EventSender.getInstance().removeSimulator(((EventSimulator) r).getStreamConfiguration().getExecutionPlanName(),
                    ((EventSimulator) r).getStreamConfiguration().getStreamName());
        } finally {
            super.afterExecute(r, t);
        }
    }

    /**
     * pause the execution of simulators
     * @see org.wso2.eventsimulator.core.internal.ServiceComponent#pause(String)
     * */
    public void pause() {
        pauseLock.lock();
        try {
            isPaused = true;
            simulators.forEach(EventSimulator::pause);
        } finally {
            pauseLock.unlock();
        }
    }

    /**
     * resume the execution of simulators
     * @see org.wso2.eventsimulator.core.internal.ServiceComponent#resume(String)
     * */
    public void resume() {
        pauseLock.lock();
        try {
            isPaused = false;
            simulators.forEach(EventSimulator::resume);
            unpaused.signalAll();
        } finally {
            pauseLock.unlock();
        }
    }

    /**
     * stop the execution of simulators
     * @see ServiceComponent#stop()
     * */
    public void stop() {
        simulators.forEach(EventSimulator::stop);
        shutdownNow();
    }

    /**
     * currently isRunning() and isPaused() methods are unused. However, in future they may be useful to obtain status of simulations.
     * */
    public boolean isRunning() {
        return !isPaused;
    }

    public boolean isPaused() {
        return isPaused;
    }
}



