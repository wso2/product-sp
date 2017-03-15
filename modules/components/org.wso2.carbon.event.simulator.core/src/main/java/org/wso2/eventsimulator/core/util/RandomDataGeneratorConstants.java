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

/**
 * Constants related to random data generator
 */
public class RandomDataGeneratorConstants {

    //constants for Property based data generator
    public static final String MODULE_CALENDAR = "calendar";
    public static final String MODULE_CONTACT = "contact";
    public static final String MODULE_FINANCE = "finance";
    public static final String MODULE_INTERNET = "internet";
    public static final String MODULE_LOCATION = "location";
    public static final String MODULE_WORDS = "words";


    //constants for each category/module

    //CALENDAR
    public static final String MODULE_CALENDAR_TIME_12_H = "time12h";
    public static final String MODULE_CALENDAR_TIME_24_H = "time24h";
    public static final String MODULE_CALENDAR_SECOND = "second";
    public static final String MODULE_CALENDAR_MINUTE = "minute";
    public static final String MODULE_CALENDAR_MONTH = "month";
    public static final String MODULE_CALENDAR_YEAR = "year";
    public static final String MODULE_CALENDAR_DAY = "day";
    public static final String MODULE_CALENDAR_DAY_OF_WEEK = "dayOfWeek";
    public static final String MODULE_CALENDAR_MONTH_NUMBER = "month(Number)";
    public static final String MODULE_CALENDAR_DATE = "date";

    //CONTACT
    public static final String MODULE_CONTACT_FULL_NAME = "fullName";
    public static final String MODULE_CONTACT_FIRST_NAME = "firstName";
    public static final String MODULE_CONTACT_LAST_NAME = "lastName";
    public static final String MODULE_CONTACT_ADDRESS = "address";
    public static final String MODULE_CONTACT_BSN = "BSN";
    public static final String MODULE_CONTACT_EMAIL = "email";
    public static final String MODULE_CONTACT_PHONE_NO = "phoneNo";
    public static final String MODULE_CONTACT_POSTCODE = "postCode";
    public static final String MODULE_CONTACT_STATE = "state";
    public static final String MODULE_CONTACT_CITY = "city";
    public static final String MODULE_CONTACT_COMPANY = "company";
    public static final String MODULE_CONTACT_COUNTRY = "country";
    public static final String MODULE_CONTACT_STREET_NAME = "streetName";
    public static final String MODULE_CONTACT_HOUSE_NO = "houseNo";
    public static final String MODULE_CONTACT_HEIGHT_CM = "height(cm)";
    public static final String MODULE_CONTACT_HEIGHT_M = "height(m)";
    public static final String MODULE_CONTACT_WEIGHT = "weight";
    public static final String MODULE_CONTACT_OCCUPATION = "occupation";

    //FINANCE
    public static final String MODULE_FINANCE_IBAN = "iban";
    public static final String MODULE_FINANCE_BIC = "bic";
    public static final String MODULE_FINANCE_VISA_CREDIT_CARD = "visaCreditCard";
    public static final String MODULE_FINANCE_PIN_CODE = "pinCode";

    //Internet
    public static final String MODULE_INTERNET_URL_BUILDER = "url";
    public static final String MODULE_INTERNET_IP = "ip";
    public static final String MODULE_INTERNET_IPV_6 = "ipv6";
    public static final String MODULE_INTERNET_MAC_ADDRESS = "macAddress";
    public static final String MODULE_INTERNET_UUID = "UUID";
    public static final String MODULE_INTERNET_COLOR = "color";
    public static final String MODULE_INTERNET_USER_NAME = "userName";

    //LOCATION
    public static final String MODULE_LOCATION_ALTITUDE = "altitude";
    public static final String MODULE_LOCATION_DEPTH = "depth";
    public static final String MODULE_LOCATION_COORDINATES = "coordinates";
    public static final String MODULE_LOCATION_LATITUDE = "latitude";
    public static final String MODULE_LOCATION_LONGITUDE = "longitude";
    public static final String MODULE_LOCATION_GEO_HASH = "geoHash";

    //WORDS
    public static final String MODULE_WORDS_WORDS = "words";
    public static final String MODULE_WORDS_PARAGRAPH = "paragraph";
    public static final String MODULE_WORDS_SENTENCE = "sentence";
}
