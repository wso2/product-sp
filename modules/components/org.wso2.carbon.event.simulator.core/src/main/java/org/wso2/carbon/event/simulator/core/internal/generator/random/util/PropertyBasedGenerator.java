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
import org.wso2.carbon.event.simulator.core.internal.bean.RandomAttributeDTO;
import org.wso2.carbon.event.simulator.core.internal.generator.random.RandomAttributeGenerator;
import org.wso2.carbon.event.simulator.core.internal.util.EventSimulatorConstants;
import org.wso2.carbon.event.simulator.core.internal.util.RandomDataGeneratorConstants;

import static org.wso2.carbon.event.simulator.core.internal.util.CommonOperations.checkAvailability;

import java.util.Random;


/**
 * PropertyBasedGenerator class is responsible for generating attribute values for a given category and property pair
 */
public class PropertyBasedGenerator implements RandomAttributeGenerator {
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

    private PropertyBasedAttributeDTO propertyBasedAttrConfig  = new PropertyBasedAttributeDTO();


    public PropertyBasedGenerator() {
    }

    /**
     * generateAttribute() generated an attribute value based on the property specified in the
     * propertyBasedAttributeDto configuration
     *
     * @return attribute value generated based on the property value
     */
    @Override
    public Object generateAttribute() {
        Object dataValue;

        switch (propertyBasedAttrConfig.getProperty()) {
            case RandomDataGeneratorConstants.MODULE_CALENDAR_TIME_12_H:
                dataValue = calendar.time12h();
                break;

            case RandomDataGeneratorConstants.MODULE_CALENDAR_TIME_24_H:
                dataValue = calendar.time24h();
                break;

            case RandomDataGeneratorConstants.MODULE_CALENDAR_SECOND:
                dataValue = calendar.second();
                break;

            case RandomDataGeneratorConstants.MODULE_CALENDAR_MINUTE:
                dataValue = calendar.minute();
                break;

            case RandomDataGeneratorConstants.MODULE_CALENDAR_MONTH:
                dataValue = calendar.month();
                break;

            case RandomDataGeneratorConstants.MODULE_CALENDAR_MONTH_NUMBER:
                dataValue = calendar.month(true);
                break;

            case RandomDataGeneratorConstants.MODULE_CALENDAR_YEAR:
                dataValue = calendar.year();
                break;

            case RandomDataGeneratorConstants.MODULE_CALENDAR_DAY:
                dataValue = calendar.day();
                break;

            case RandomDataGeneratorConstants.MODULE_CALENDAR_DAY_OF_WEEK:
                dataValue = calendar.dayOfWeek();
                break;

            case RandomDataGeneratorConstants.MODULE_CALENDAR_DATE:
                Random random = new Random();
                int incrementValue = random.nextInt(10);
                dataValue = calendar.relativeDate(DateTime.now().plusDays(incrementValue)).
                        asString(DateFormat.dd_MM_yyyy_H_m_s_a);
                break;

            case RandomDataGeneratorConstants.MODULE_CONTACT_FULL_NAME:
                dataValue = contact.fullName(true, true);
                break;

            case RandomDataGeneratorConstants.MODULE_CONTACT_FIRST_NAME:
                dataValue = contact.firstName();
                break;

            case RandomDataGeneratorConstants.MODULE_CONTACT_LAST_NAME:
                dataValue = contact.lastName();
                break;

            case RandomDataGeneratorConstants.MODULE_CONTACT_BSN:
                dataValue = contact.bsn();
                break;

            case RandomDataGeneratorConstants.MODULE_CONTACT_ADDRESS:
                dataValue = contact.address();
                break;

            case RandomDataGeneratorConstants.MODULE_CONTACT_EMAIL:
                dataValue = contact.eMail();
                break;

            case RandomDataGeneratorConstants.MODULE_CONTACT_PHONE_NO:
                dataValue = contact.phoneNumber();
                break;

            case RandomDataGeneratorConstants.MODULE_CONTACT_POSTCODE:
                dataValue = contact.postcode();
                break;

            case RandomDataGeneratorConstants.MODULE_CONTACT_STATE:
                dataValue = contact.state();
                break;

            case RandomDataGeneratorConstants.MODULE_CONTACT_CITY:
                dataValue = contact.city();
                break;

            case RandomDataGeneratorConstants.MODULE_CONTACT_COMPANY:
                dataValue = contact.company();
                break;

            case RandomDataGeneratorConstants.MODULE_CONTACT_COUNTRY:
                dataValue = contact.country();
                break;

            case RandomDataGeneratorConstants.MODULE_CONTACT_STREET_NAME:
                dataValue = contact.streetName();
                break;

            case RandomDataGeneratorConstants.MODULE_CONTACT_HOUSE_NO:
                dataValue = contact.houseNumber();
                break;

            case RandomDataGeneratorConstants.MODULE_CONTACT_HEIGHT_CM:
                dataValue = contact.height(true);
                break;

            case RandomDataGeneratorConstants.MODULE_CONTACT_HEIGHT_M:
                dataValue = contact.height(false);
                break;

            case RandomDataGeneratorConstants.MODULE_CONTACT_WEIGHT:
                dataValue = contact.weight(true);
                break;

            case RandomDataGeneratorConstants.MODULE_CONTACT_OCCUPATION:
                dataValue = contact.occupation();
                break;

            case RandomDataGeneratorConstants.MODULE_FINANCE_IBAN:
                dataValue = finance.iban();
                break;
            case RandomDataGeneratorConstants.MODULE_FINANCE_BIC:
                dataValue = finance.bic();
                break;
            case RandomDataGeneratorConstants.MODULE_FINANCE_VISA_CREDIT_CARD:
                dataValue = finance.visaCard();
                break;
            case RandomDataGeneratorConstants.MODULE_FINANCE_PIN_CODE:
                dataValue = finance.pinCode();
                break;

            case RandomDataGeneratorConstants.MODULE_INTERNET_URL_BUILDER:
                dataValue = internet.urlBuilder();
                break;

            case RandomDataGeneratorConstants.MODULE_INTERNET_IP:
                dataValue = internet.ip();
                break;

            case RandomDataGeneratorConstants.MODULE_INTERNET_IPV_6:
                dataValue = internet.ipv6();
                break;

            case RandomDataGeneratorConstants.MODULE_INTERNET_MAC_ADDRESS:
                dataValue = internet.macAddress();
                break;

            case RandomDataGeneratorConstants.MODULE_INTERNET_UUID:
                dataValue = internet.UUID();
                break;

            case RandomDataGeneratorConstants.MODULE_INTERNET_COLOR:
                dataValue = internet.color();
                break;

            case RandomDataGeneratorConstants.MODULE_INTERNET_USER_NAME:
                dataValue = internet.username();
                break;

            case RandomDataGeneratorConstants.MODULE_LOCATION_ALTITUDE:
                dataValue = location.altitude();
                break;
            case RandomDataGeneratorConstants.MODULE_LOCATION_DEPTH:
                dataValue = location.depth();
                break;
            case RandomDataGeneratorConstants.MODULE_LOCATION_COORDINATES:
                dataValue = location.coordinates();
                break;
            case RandomDataGeneratorConstants.MODULE_LOCATION_LATITUDE:
                dataValue = location.latitude();
                break;
            case RandomDataGeneratorConstants.MODULE_LOCATION_LONGITUDE:
                dataValue = location.longitude();
                break;
            case RandomDataGeneratorConstants.MODULE_LOCATION_GEO_HASH:
                dataValue = location.geohash();
                break;

            case RandomDataGeneratorConstants.MODULE_WORDS_WORDS:
                dataValue = words.word();
                break;

            case RandomDataGeneratorConstants.MODULE_WORDS_PARAGRAPH:
                dataValue = words.paragraph();
                break;

            case RandomDataGeneratorConstants.MODULE_WORDS_SENTENCE:
                dataValue = words.sentence();
                break;

            default:
                throw new EventGenerationException("Random data generator cannot generate attributes of property type" +
                        " '" + propertyBasedAttrConfig.getProperty() + "'. Please provide a valid property type.");
        }
        return dataValue;
    }

    /**
     * validateAttributeConfig() validates the property based attribute configuration provided
     *
     * @param attributeConfig JSON object of the property based attribute configuration
     */
    @Override
    public void validateAttributeConfig(JSONObject attributeConfig) throws InvalidConfigException {

        if (checkAvailability(attributeConfig, EventSimulatorConstants.PROPERTY_BASED_ATTRIBUTE_PROPERTY)) {
            propertyBasedAttrConfig.setProperty(attributeConfig
                    .getString(EventSimulatorConstants.PROPERTY_BASED_ATTRIBUTE_PROPERTY));
        } else {
            throw new InvalidConfigException("Property value is required for "
                    + RandomAttributeDTO.RandomDataGeneratorType.PROPERTY_BASED + " simulation.");
        }
    }

}
