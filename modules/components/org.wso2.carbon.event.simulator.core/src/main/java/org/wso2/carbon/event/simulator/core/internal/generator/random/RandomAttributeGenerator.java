package org.wso2.carbon.event.simulator.core.internal.generator.random;

/**
 * RandomAttributeGenerator interface defines common methods used by all random attribute generators
 * This interface is implemented by
 *
 * @see org.wso2.carbon.event.simulator.core.internal.generator.random.util.CustomBasedAttrGenerator
 * @see org.wso2.carbon.event.simulator.core.internal.generator.random.util.PrimitiveBasedAttrGenerator
 * @see org.wso2.carbon.event.simulator.core.internal.generator.random.util.PropertyBasedAttrGenerator
 * @see org.wso2.carbon.event.simulator.core.internal.generator.random.util.RegexBasedAttrGenerator
 */
public interface RandomAttributeGenerator {

    Object generateAttribute();

}
