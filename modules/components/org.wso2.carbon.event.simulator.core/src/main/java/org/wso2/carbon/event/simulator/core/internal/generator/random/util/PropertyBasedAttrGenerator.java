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

package org.wso2.carbon.event.simulator.core.internal.generator.random.util;

import fabricator.Calendar;
import fabricator.Contact;
import fabricator.Fabricator;
import fabricator.Finance;
import fabricator.Internet;
import fabricator.Location;
import fabricator.Words;
import fabricator.enums.DateFormat;

import org.joda.time.DateTime;
import org.json.JSONObject;
import org.wso2.carbon.event.simulator.core.exception.EventGenerationException;
import org.wso2.carbon.event.simulator.core.exception.InvalidConfigException;
import org.wso2.carbon.event.simulator.core.internal.bean.PropertyBasedAttributeDTO;
import org.wso2.carbon.event.simulator.core.internal.generator.random.RandomAttributeGenerator;
import org.wso2.carbon.event.simulator.core.internal.util.EventSimulatorConstants;

import static org.wso2.carbon.event.simulator.core.internal.util.CommonOperations.checkAvailability;

import java.util.Random;

/**
 * PropertyBasedAttrGenerator class is responsible for generating attribute values for a given property type
 */
public class PropertyBasedAttrGenerator implements RandomAttributeGenerator {
    /**
     * Initialize contact to generate contact related data
     */
    private static final Contact contact = Fabricator.contact();
    /**
     * Initialize calendar to generate calendar related data
     */
    private static final Calendar calendar = Fabricator.calendar();
    /**
     * Initialize Finance to generate finance related data
     */
    private static final Finance finance = Fabricator.finance();
    /**
     * Initialize internet to generate internet related data
     */
    private static final Internet internet = Fabricator.internet();
    /**
     * Initialize location to generate location related data
     */
    private static final Location location = Fabricator.location();
    /**
     * Initialize words to generate words related data
     */
    private static final Words words = Fabricator.words();
    private PropertyBasedAttributeDTO propertyBasedAttrConfig = new PropertyBasedAttributeDTO();

    /**
     * PropertyBasedAttrGenerator() constructor validates the property based attribute configuration provided and
     * creates a PropertyBasedAttributeDTO object containing Property base attribute generation configuration
     *
     * @param attributeConfig JSON object of the property based attribute configuration
     * @throws InvalidConfigException if attribute configuration is invalid
     */
    public PropertyBasedAttrGenerator(JSONObject attributeConfig) throws InvalidConfigException {
        /**
         * check whether the property based attribute generation has a property name specified
         * if not throw an exception
         * else check whether the property value provided is valid
         * if yes set property value to PropertyBasedAttributeDTO
         * else throw an exception
         * */
        if (checkAvailability(attributeConfig, EventSimulatorConstants.PROPERTY_BASED_ATTRIBUTE_PROPERTY)) {
            try {
                propertyBasedAttrConfig.setProperty(PropertyType.valueOf(attributeConfig
                        .getString(EventSimulatorConstants.PROPERTY_BASED_ATTRIBUTE_PROPERTY)));
            } catch (IllegalArgumentException e) {
                throw new EventGenerationException("Invalid property type '" + propertyBasedAttrConfig.getProperty() +
                        "' provided for random event generation using " +
                        RandomAttributeGenerator.RandomDataGeneratorType.PROPERTY_BASED + " attribute generator. " +
                        "Invalid attribute configuration provided : " + attributeConfig.toString());
            }
        } else {
            throw new InvalidConfigException("Property value is required for "
                    + RandomAttributeGenerator.RandomDataGeneratorType.PROPERTY_BASED + " attribute generation. " +
                    "Invalid attribute configuration provided : " + attributeConfig.toString());
        }
    }

    /**
     * generateAttribute() generated an attribute value based on the property specified in the
     * propertyBasedAttributeDto configuration
     *
     * @return attribute value generated based on the property value
     */
    @Override
    public Object generateAttribute() {
        Object dataValue = null;
        switch (propertyBasedAttrConfig.getProperty()) {
            case TIME_12H:
                dataValue = calendar.time12h();
                break;
            case TIME_24H:
                dataValue = calendar.time24h();
                break;
            case SECOND:
                dataValue = calendar.second();
                break;
            case MINUTE:
                dataValue = calendar.minute();
                break;
            case MONTH:
                dataValue = calendar.month();
                break;
            case MONTH_NUM:
                dataValue = calendar.month(true);
                break;
            case YEAR:
                dataValue = calendar.year();
                break;
            case DAY:
                dataValue = calendar.day();
                break;
            case DAY_OF_WEEK:
                dataValue = calendar.dayOfWeek();
                break;
            case DATE:
                Random random = new Random();
                int incrementValue = random.nextInt(10);
                dataValue = calendar.relativeDate(DateTime.now().plusDays(incrementValue)).
                        asString(DateFormat.dd_MM_yyyy_H_m_s_a);
                break;
            case FULL_NAME:
                dataValue = contact.fullName(true, true);
                break;
            case FIRST_NAME:
                dataValue = contact.firstName();
                break;
            case LAST_NAME:
                dataValue = contact.lastName();
                break;
            case BSN:
                dataValue = contact.bsn();
                break;
            case ADDRESS:
                dataValue = contact.address();
                break;
            case EMAIL:
                dataValue = contact.eMail();
                break;
            case PHONE_NUM:
                dataValue = contact.phoneNumber();
                break;
            case POST_CODE:
                dataValue = contact.postcode();
                break;
            case STATE:
                dataValue = contact.state();
                break;
            case CITY:
                dataValue = contact.city();
                break;
            case COMPANY:
                dataValue = contact.company();
                break;
            case COUNTRY:
                dataValue = contact.country();
                break;
            case STREET_NAME:
                dataValue = contact.streetName();
                break;
            case HOUSE_NO:
                dataValue = contact.houseNumber();
                break;
            case HEIGHT_CM:
                dataValue = contact.height(true);
                break;
            case HEIGHT_M:
                dataValue = contact.height(false);
                break;
            case WEIGHT:
                dataValue = contact.weight(true);
                break;
            case OCCUPATION:
                dataValue = contact.occupation();
                break;
            case IBAN:
                dataValue = finance.iban();
                break;
            case BIC:
                dataValue = finance.bic();
                break;
            case VISA_CARD:
                dataValue = finance.visaCard();
                break;
            case PIN_CODE:
                dataValue = finance.pinCode();
                break;
            case URL:
                dataValue = internet.urlBuilder();
                break;
            case IP:
                dataValue = internet.ip();
                break;
            case IP_V6:
                dataValue = internet.ipv6();
                break;
            case MAC_ADDRESS:
                dataValue = internet.macAddress();
                break;
            case UUID:
                dataValue = internet.UUID();
                break;
            case COLOUR:
                dataValue = internet.color();
                break;
            case USERNAME:
                dataValue = internet.username();
                break;
            case ALTITUDE:
                dataValue = location.altitude();
                break;
            case DEPTH:
                dataValue = location.depth();
                break;
            case COORDINATES:
                dataValue = location.coordinates();
                break;
            case LATITUDE:
                dataValue = location.latitude();
                break;
            case LONGITUDE:
                dataValue = location.longitude();
                break;
            case GEO_HASH:
                dataValue = location.geohash();
                break;
            case WORDS:
                dataValue = words.word();
                break;
            case PARAGRAPH:
                dataValue = words.paragraph();
                break;
            case SENTENCE:
                dataValue = words.sentence();
                break;
        }
        return dataValue;
    }

    /**
     * Property enum specifies the attributes that can be randomly generates using the random data generator library
     */
    public enum PropertyType {
        TIME_12H, TIME_24H, SECOND, MINUTE, MONTH, MONTH_NUM, YEAR, DAY, DAY_OF_WEEK, DATE, FULL_NAME, FIRST_NAME,
        LAST_NAME, WORDS, BSN, ADDRESS, EMAIL, PHONE_NUM, POST_CODE, STATE, CITY, COMPANY, COUNTRY,
        STREET_NAME, HOUSE_NO, HEIGHT_CM, HEIGHT_M, WEIGHT, OCCUPATION, IBAN, BIC, VISA_CARD, PIN_CODE, URL, IP, IP_V6,
        MAC_ADDRESS, UUID, USERNAME, COLOUR, ALTITUDE, DEPTH, COORDINATES, LATITUDE, LONGITUDE, GEO_HASH, SENTENCE,
        PARAGRAPH
    }
}
