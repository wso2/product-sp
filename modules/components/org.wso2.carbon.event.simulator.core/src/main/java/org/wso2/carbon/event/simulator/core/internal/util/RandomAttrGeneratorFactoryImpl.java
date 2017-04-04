package org.wso2.carbon.event.simulator.core.internal.util;

import org.json.JSONObject;
import org.wso2.carbon.event.simulator.core.exception.InvalidConfigException;
import org.wso2.carbon.event.simulator.core.internal.generator.random.RandomAttributeGenerator;
import org.wso2.carbon.event.simulator.core.internal.generator.random.util.CustomBasedAttrGenerator;
import org.wso2.carbon.event.simulator.core.internal.generator.random.util.PrimitiveBasedAttrGenerator;
import org.wso2.carbon.event.simulator.core.internal.generator.random.util.PropertyBasedAttrGenerator;
import org.wso2.carbon.event.simulator.core.internal.generator.random.util.RegexBasedAttrGenerator;

import static org.wso2.carbon.event.simulator.core.internal.util.CommonOperations.checkAvailability;

/**
 * Factory class used to create random attribute generators
 */
public class RandomAttrGeneratorFactoryImpl implements RandomAttrGeneratorFactory {
    @Override
    public RandomAttributeGenerator getRandomAttrGenerator(JSONObject attributeConfig) throws InvalidConfigException {
        if (checkAvailability(attributeConfig, EventSimulatorConstants.RANDOM_DATA_GENERATOR_TYPE)) {
            RandomAttributeGenerator.RandomDataGeneratorType type;
            /*
             * for each attribute configuration, check whether a valid attribute generation type is provided.
             * if yes create respective attribute generators
             * else throw an exception
             * */
            try {
                type = RandomAttributeGenerator.RandomDataGeneratorType.valueOf(attributeConfig
                        .getString(EventSimulatorConstants.RANDOM_DATA_GENERATOR_TYPE));
            } catch (IllegalArgumentException e) {
                throw new InvalidConfigException("Invalid random attribute generation type. Generator type must " +
                        "be either '" + RandomAttributeGenerator.RandomDataGeneratorType.CUSTOM_DATA_BASED + "' or '"
                        + RandomAttributeGenerator.RandomDataGeneratorType.PRIMITIVE_BASED + "' or '" +
                        RandomAttributeGenerator.RandomDataGeneratorType.PROPERTY_BASED + "' or '" +
                        RandomAttributeGenerator.RandomDataGeneratorType.REGEX_BASED + "'. Invalid attribute " +
                        "configuration : " + attributeConfig.toString());
            }
            RandomAttributeGenerator randomAttributeGenerator = null;
            switch (type) {
                case CUSTOM_DATA_BASED:
                    randomAttributeGenerator = new CustomBasedAttrGenerator(attributeConfig);
                    break;
                case PRIMITIVE_BASED:
                    randomAttributeGenerator = new PrimitiveBasedAttrGenerator(attributeConfig);
                    break;
                case PROPERTY_BASED:
                    randomAttributeGenerator = new PropertyBasedAttrGenerator(attributeConfig);
                    break;
                case REGEX_BASED:
                    randomAttributeGenerator = new RegexBasedAttrGenerator(attributeConfig);
                    break;
            }
            return randomAttributeGenerator;
        } else {
            throw new InvalidConfigException("Random attribute generator type is required for random " +
                    "simulation. Generation type must be either '" +
                    RandomAttributeGenerator.RandomDataGeneratorType.CUSTOM_DATA_BASED + "' or '" +
                    RandomAttributeGenerator.RandomDataGeneratorType.PRIMITIVE_BASED + "' or '" +
                    RandomAttributeGenerator.RandomDataGeneratorType.PROPERTY_BASED + "' or '" +
                    RandomAttributeGenerator.RandomDataGeneratorType.REGEX_BASED + "'. Invalid attribute" +
                    " configuration : " + attributeConfig.toString());
        }
    }
}
