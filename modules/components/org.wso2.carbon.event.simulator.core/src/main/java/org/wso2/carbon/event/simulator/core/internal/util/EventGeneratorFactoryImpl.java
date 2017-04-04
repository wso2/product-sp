package org.wso2.carbon.event.simulator.core.internal.util;

import org.json.JSONObject;
import org.wso2.carbon.event.simulator.core.exception.InsufficientAttributesException;
import org.wso2.carbon.event.simulator.core.exception.InvalidConfigException;
import org.wso2.carbon.event.simulator.core.internal.generator.EventGenerator;
import org.wso2.carbon.event.simulator.core.internal.generator.csv.core.CSVEventGenerator;
import org.wso2.carbon.event.simulator.core.internal.generator.database.core.DatabaseEventGenerator;
import org.wso2.carbon.event.simulator.core.internal.generator.random.core.RandomEventGenerator;

import static org.wso2.carbon.event.simulator.core.internal.util.CommonOperations.checkAvailability;

/**
 * Factory class used to create event generators
 */
public class EventGeneratorFactoryImpl implements EventGeneratorFactory {

    /**
     * getEventGenerator() creates and initializes event generators according to the source configuration provided
     *
     * @param sourceConfig       json object containing source configuration used for simulation
     * @param timestampStartTime least possible timestamp an event produced could have
     * @param timestampEndTime   maximum possible timestamp an even produced could have
     * @throws InvalidConfigException          if the simulation type is not specified or if an invalid generator type
     *                                         is specified
     * @throws InsufficientAttributesException if the number of attributes produced by generator is not equal to the
     *                                         number of attributes in the stream being simulated
     */
    @Override
    public EventGenerator getEventGenerator(JSONObject sourceConfig, long timestampStartTime, long
            timestampEndTime) throws InvalidConfigException, InsufficientAttributesException {
        /*
         * check whether the source configuration has a simulation type specified
         * if the generator type is either DB, CSV, or Random retrieve type
         * else throw an exception
         * */
        if (checkAvailability(sourceConfig, EventSimulatorConstants.EVENT_SIMULATION_TYPE)) {
            EventGenerator.GeneratorType generatorType;
            try {
                generatorType = EventGenerator.GeneratorType.valueOf(sourceConfig.
                        getString(EventSimulatorConstants.EVENT_SIMULATION_TYPE));
            } catch (IllegalArgumentException e) {
                throw new InvalidConfigException("Simulation type must be " +
                        "either '" + EventGenerator.GeneratorType.FILE_SIMULATION + "' or '" +
                        EventGenerator.GeneratorType.DATABASE_SIMULATION + "' or '" +
                        EventGenerator.GeneratorType.RANDOM_DATA_SIMULATION + "'. Invalid source configuration " +
                        "provided : " + sourceConfig.toString());
            }
            /*
             * initialize generators for sources
             * */
            EventGenerator eventGenerator = null;
            switch (generatorType) {
                case FILE_SIMULATION:
                    eventGenerator = new CSVEventGenerator(sourceConfig, timestampStartTime,
                            timestampEndTime);
                    break;
                case DATABASE_SIMULATION:
                    eventGenerator = new DatabaseEventGenerator(
                            sourceConfig, timestampStartTime, timestampEndTime);
                    break;
                case RANDOM_DATA_SIMULATION:
                    eventGenerator = new RandomEventGenerator(sourceConfig, timestampStartTime,
                            timestampEndTime);
                    break;
            }
            return eventGenerator;
        } else {
            throw new InvalidConfigException("Simulation type must" +
                    " be either '" + EventGenerator.GeneratorType.FILE_SIMULATION + "' or '" +
                    EventGenerator.GeneratorType.DATABASE_SIMULATION + "' or '" +
                    EventGenerator.GeneratorType.RANDOM_DATA_SIMULATION + "'. Invalid source configuration " +
                    "provided : " + sourceConfig.toString());
        }
    }
}
