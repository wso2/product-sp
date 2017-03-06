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

package org.wso2.eventsimulator.core.simulator.randomdatafeedsimulation.util;


import org.wso2.eventsimulator.core.simulator.exception.EventSimulationException;
import org.wso2.eventsimulator.core.simulator.randomdatafeedsimulation.bean.CustomBasedAttribute;
import org.wso2.eventsimulator.core.simulator.randomdatafeedsimulation.bean.FeedSimulationStreamAttributeDto;
import org.wso2.eventsimulator.core.simulator.randomdatafeedsimulation.bean.PrimitiveBasedAttribute;
import org.wso2.eventsimulator.core.simulator.randomdatafeedsimulation.bean.PropertyBasedAttributeDto;
import org.wso2.eventsimulator.core.simulator.randomdatafeedsimulation.bean.RegexBasedAttributeDto;
import org.wso2.siddhi.query.api.definition.Attribute;

/**
 * Generates attribute values for an each attribute in an input stream
 * It is an utility class
 * It uses {@link RandomDataGenerator} to generate random values
 */
public class AttributeGenerator {

    /**
     * Initialize AttributeGenerator and make it private
     */
    private AttributeGenerator() {
    }

    /**
     * Generates attribute values for an each attribute in an input stream
     *
     * @param streamAttributeDto FeedSimulationStreamAttributeDto
     * @param attributeType      attribute data type (String,Integer,Float,Double,Long,Boolean)
     * @return generated random value as string
     */
    public static String generateAttributeValue(FeedSimulationStreamAttributeDto streamAttributeDto, Attribute.Type attributeType) {
        String value = null;
        if (streamAttributeDto.getType() != null) {
            if (streamAttributeDto instanceof PrimitiveBasedAttribute) {
                value = String.valueOf(RandomDataGenerator.generatePrimitiveBasedRandomData(attributeType, ((PrimitiveBasedAttribute) streamAttributeDto).getMin(), ((PrimitiveBasedAttribute) streamAttributeDto).getMax(), ((PrimitiveBasedAttribute) streamAttributeDto).getLength()));
            } else if (streamAttributeDto instanceof RegexBasedAttributeDto) {
                value = String.valueOf(RandomDataGenerator.generateRegexBasedRandomData(((RegexBasedAttributeDto) streamAttributeDto).getPattern()));
            } else if (streamAttributeDto instanceof PropertyBasedAttributeDto) {
                value = String.valueOf(RandomDataGenerator.generatePropertyBasedRandomData(((PropertyBasedAttributeDto) streamAttributeDto).getCategory(), ((PropertyBasedAttributeDto) streamAttributeDto).getProperty()));
            } else if (streamAttributeDto instanceof CustomBasedAttribute) {
                value = String.valueOf(RandomDataGenerator.generateCustomRandomData(((CustomBasedAttribute) streamAttributeDto).getCustomDataList()));
            }
        } else {
            throw new EventSimulationException(". Error occurred when generating attribute data. : " + new NullPointerException().getMessage());
        }
        return value;
//        }
    }

}
