package org.wso2.carbon.event.simulator.core.internal.generator.random;

import org.json.JSONObject;
import org.wso2.carbon.event.simulator.core.exception.InvalidConfigException;

/**
 * RandomAttributeGenerator interface defines common methods used by all random attribute generators
 * This interface is implemented by
 *
 * @see org.wso2.carbon.event.simulator.core.internal.generator.random.util.CustomBasedGenerator
 * @see org.wso2.carbon.event.simulator.core.internal.generator.random.util.PrimitiveBasedGenerator
 * @see org.wso2.carbon.event.simulator.core.internal.generator.random.util.PropertyBasedGenerator
 * @see org.wso2.carbon.event.simulator.core.internal.generator.random.util.RegexBasedGenerator
 */
public interface RandomAttributeGenerator {

    Object generateAttribute();

    void validateAttributeConfig(JSONObject attributeConfig) throws InvalidConfigException;
}
