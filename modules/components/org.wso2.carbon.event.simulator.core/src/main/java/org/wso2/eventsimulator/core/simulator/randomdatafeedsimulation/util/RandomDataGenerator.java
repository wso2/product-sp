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

import com.github.javafaker.Faker;
import com.mifmif.common.regex.Generex;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.wso2.eventsimulator.core.simulator.exception.EventSimulationException;
import org.wso2.eventsimulator.core.util.RandomDataGeneratorConstants;
import org.wso2.streamprocessor.core.StreamDefinitionRetriever;

/**
 * Generates random value for given case
 * It is an utility class
 * Data can be generated in three ways
 * 1. Generate data according to given data type
 * For this it uses Fabricator library
 * 2. Generate meaning full data Eg : Full Name
 * For reference {<databaseFeedSimulation href="https://www.mockaroo.com/">www.mockaroo.com</databaseFeedSimulation>}
 * 3. Generate data according to given regular expression
 * For this it uses generex library
 * 4. Generate data with in given data list
 * <p>
 * <databaseFeedSimulation href="http://biercoff.com/fabricator/">fabricator</databaseFeedSimulation>
 * <databaseFeedSimulation href="https://github.com/azakordonets/fabricator">fabricator - github source </databaseFeedSimulation>
 * <databaseFeedSimulation href="https://github.com/mifmif/Generex">Generex</databaseFeedSimulation>
 */
public class RandomDataGenerator {

    private static Faker faker = new Faker();

    /**
     * Initialize RandomDataGenerator and make it private
     */
    private RandomDataGenerator() {

    }

    /**
     * Generate data according to given data type. And cast it into relevant data type
     * For this it uses Alphanumeric from fabricator library
     *
     * @param type   attribute data type (String,Integer,Float,Double,Long,Boolean)
     * @param min    Minimum value for numeric values to be generate
     * @param max    Maximum value for numeric values to be generated
     * @param length If attribute type is string length indicates length of the string to be generated
     *               If attribute type is Float or Double length indicates no of Numbers after the decimal point
     * @return Generated value as object
     * <databaseFeedSimulation href="http://biercoff.com/fabricator/">fabricator</databaseFeedSimulation>
     */
    public static Object generatePrimitiveBasedRandomData(StreamDefinitionRetriever.Type type, Object min, Object max, int length) {
        Object result = null;
        DecimalFormat format = new DecimalFormat();
        switch (type) {
            case INTEGER:
                //number().numberBetween (int,int)
                result = faker.number().numberBetween(Integer.parseInt((String) min),Integer.parseInt((String) max));
                break;
            case LONG:
//                number().numberBetween(long,long)
                result = faker.number().numberBetween(Long.parseLong((String) min), Long.parseLong((String) max));
                break;
            case FLOAT:
//                todo 24/02/2017 find for float
                result = (long) faker.number().randomDouble(length,Integer.parseInt((String) min),Integer.parseInt((String) max));
                break;
            case DOUBLE:
//                number().randomDouble(decimals,min,max) - min & max can be int/long
//                todo R 24/02/2017 why doesnt randomDouble() doesnt work for long min and max
                result = faker.number().randomDouble(length,Integer.parseInt((String) min),Integer.parseInt((String) max));
                break;
            case STRING:
//                lorem().fixedString(length)
                result = faker.lorem().fixedString(length);
                break;
            case BOOLEAN:
//                bool().bool()
                result = faker.bool().bool();
                break;
        }

        return result;
    }

    /**
     * Generate data according to given regular expression.
     * It uses  A Java library called Generex for generating String that match
     * databaseFeedSimulation given regular expression
     *
     * @param pattern Regular expression used to generate data
     * @return Generated value as object
     * @see <databaseFeedSimulation href="https://github.com/mifmif/Generex">Generex</databaseFeedSimulation>
     */
    public static Object generateRegexBasedRandomData(String pattern) {
        Generex generex = new Generex(pattern);
        Object result;
        result = generex.random();
        return result;
    }

//    todo R 01/03/2017 instead of using faker revert back to using fabricator.

    /**
     * Generate meaning full data.
     * For this it uses the Java-Faker library
     *
     * @param categoryType CategoryType
     * @param propertyType PropertyType
     * @return Generated value as object
     */

    /** The category breakdown of Java-Faker is as follows
     *
     * 1. address - building no
     *              city
     *              country
     *              country code
     *              latitude
     *              longtitude
     *              state
     *              state abbreviation
     *              street address no.
     *              street adress
     *              street name
     *              timezone
     *              zipcode
     *
     * 2. app - author
     *          name
     *          version
     *
     * 3. bool - bool
     *
     * 4. business - credit card no
     *              credit card type
     *              credit card expiry
     *
     * 5. code- asin - Amazon Standard Identification Number (ASIN) is a 10-character alphanumeric unique identifier assigned by Amazon.com and its partners for product identification within the Amazon organization
     *          ean8 - EAN-8 is a barcode symbology
     *          ean13 - EAN-13 is a barcode symbology
     *          gtin8 - GTIN describes a family of GS1 (EAN.UCC) global data structures that employ 14 digits and can be encoded into various types of data carriers.8-digit number used predominately outside of North America
     *          gtin13 - 13-digit number used predominately outside of North America
     *          imei - International Mobile Equipment Identity (IMEI) number is a unique 15 digit identification or serial number for mobile phones
     *          isbn10 - International Standard Book Number. This 10 or 13-digit number identifies a specific book
     *          isbn13
     *          isbnGroup - ISBN group code
     *          isbnGs1
     *          isbnRegistrant
     *
     * 6. commerce - department
     *               material
     *               product name
     *               promotion code
     *
     * 7. company - bs
     *              buzzword
     *              catchphrase
     *              domain name
     *              industry
     *              name
     *              proffession
     *              url
     *
     * 8. crypto - md5 - 128-bit hash value
     *             sha1 - Secure Hash Algorithm 1 produces a 160-bit (20-byte) hash value. It's rendered as a hexadecimal number, 40 digits long
     *             sha256 - almost-unique, fixed size 256-bit (32-byte) hash
     *             sha512 - use 1024 bits (64-byte) blocks
     *
     * 9. date -future
     *          inbetween
     *          past
     *
     * 10. finance - bic
     *               credit card
     *               iban
     *
     * 11.id no - ssnvalid
     *
     * 12. internet - domain name
     *                email
     *                ipV4 Address
     *                ipV4 Cidr
     *                ipV6 Address
     *                ipV6 Cidr
     *                macaddress
     *                password
     *                private IpV4 Address
     *                public IpV4 Address
     *                safe email
     *                url
     *
     * 13. lorem - character
     *             paragraph
     *             sentence
     *             words
     *
     * 14. name - first Name
     *            full Name
     *            last Name
     *            title
     *            username
     *
     * 15. phone number - cell phone number
     *
     **/

    public static Object generatePropertyBasedRandomData(String categoryType, String propertyType) {
        Object result = null;
        switch (categoryType) {
            case RandomDataGeneratorConstants.MODULE_ADDRESS:
                switch (propertyType) {
                    case RandomDataGeneratorConstants.MODULE_ADDRESS_BUILDING_NUMBER:
                        result = faker.address().buildingNumber();
                        break;
                    case RandomDataGeneratorConstants.MODULE_ADDRESS_CITY:
                        result = faker.address().cityName();
                        break;
                    case RandomDataGeneratorConstants.MODULE_ADDRESS_COUNTRY:
                        result = faker.address().country();
                        break;
                    case RandomDataGeneratorConstants.MODULE_ADDRESS_COUNTRY_CODE:
                        result = faker.address().countryCode();
                        break;
                    case RandomDataGeneratorConstants.MODULE_ADDRESS_LATITUDE:
                        result = faker.address().latitude();
                        break;
                    case RandomDataGeneratorConstants.MODULE_ADDRESS_LONGITUDE:
                        result = faker.address().longitude();
                        break;
                    case RandomDataGeneratorConstants.MODULE_ADDRESS_STATE:
                        result = faker.address().state();
                        break;
                    case RandomDataGeneratorConstants.MODULE_ADDRESS_STATE_ABBREVIATION:
                        result = faker.address().stateAbbr();
                        break;
                    case RandomDataGeneratorConstants.MODULE_ADDRESS_STREET_ADDRESS:
                        result = faker.address().streetAddress();
                        break;
                    case RandomDataGeneratorConstants.MODULE_ADDRESS_STREET_ADDRESS_NUMBER:
                        result = faker.address().streetAddressNumber();
                        break;
                    case RandomDataGeneratorConstants.MODULE_ADDRESS_STREET_NAME:
                        result = faker.address().streetName();
                        break;
                    case RandomDataGeneratorConstants.MODULE_ADDRESS_TIME_ZONE:
                        result = faker.address().timeZone();
                        break;
                    case RandomDataGeneratorConstants.MODULE_ADDRESS_ZIP_CODE:
                        result = faker.address().zipCode();
                        break;
                    default:
                        throw new EventSimulationException(". Category 'address' does not have property : " + propertyType);
                }
                break;

            case RandomDataGeneratorConstants.MODULE_APP:

                switch (propertyType) {

                    case RandomDataGeneratorConstants.MODULE_APP_AUTHOR:
                        result = faker.app().author();
                        break;
                    case RandomDataGeneratorConstants.MODULE_APP_NAME:
                        result = faker.app().name();
                        break;
                    case RandomDataGeneratorConstants.MODULE_APP_VERSION:
                        result = faker.app().version();
                        break;
                    default:
                        throw new EventSimulationException(". Category 'app' does not have property : " + propertyType);
                }
                break;

            case RandomDataGeneratorConstants.MODULE_BOOL:

                switch (propertyType) {

                    case RandomDataGeneratorConstants.MODULE_BOOL_BOOL:
                        result = faker.bool().bool();
                        break;
                    default:
                        throw new EventSimulationException(". Category 'bool' does not have property : " + propertyType);
                }
                break;

            case RandomDataGeneratorConstants.MODULE_BUSINESS:

                switch (propertyType) {

                    case RandomDataGeneratorConstants.MODULE_BUSINESS_CREDIT_CARD_EXPIRY:
                        result = faker.business().creditCardExpiry();
                        break;
                    case RandomDataGeneratorConstants.MODULE_BUSINESS_CREDIT_CARD_NUMBER:
                        result = faker.business().creditCardNumber();
                        break;
                    case RandomDataGeneratorConstants.MODULE_BUSINESS_CREDIT_CARD_TYPE:
                        result = faker.business().creditCardType();
                        break;
                    default:
                        throw new EventSimulationException(". Category 'business' does not have property : " + propertyType);
                }
                break;

            case RandomDataGeneratorConstants.MODULE_CODE:
                switch (propertyType) {
                    case RandomDataGeneratorConstants.MODULE_CODE_ASIN:
                        result = faker.code().asin();
                        break;
                    case RandomDataGeneratorConstants.MODULE_CODE_EAN8:
                        result = faker.code().ean8();
                        break;
                    case RandomDataGeneratorConstants.MODULE_CODE_EAN13:
                        result = faker.code().ean13();
                        break;
                    case RandomDataGeneratorConstants.MODULE_CODE_GTIN8:
                        result = faker.code().gtin8();
                        break;
                    case RandomDataGeneratorConstants.MODULE_CODE_GTIN13:
                        result = faker.code().gtin13();
                        break;
                    case RandomDataGeneratorConstants.MODULE_CODE_IMEI:
                        result = faker.code().imei();
                        break;
//                    case RandomDataGeneratorConstants.MODULE_CODE_ISBN10:
//                        result = faker.code().isbn10();
//                        break;
//                    case RandomDataGeneratorConstants.MODULE_CODE_ISBN13:
//                        result = faker.code().isbn13();
//                        break;
//                    case RandomDataGeneratorConstants.MODULE_CODE_ISBN_GROUP:
//                        result = faker.code().isbnGroup();
//                        break;
//                    case RandomDataGeneratorConstants.MODULE_CODE_ISBNGS1:
//                        result = faker.code().isbnGs1();
//                        break;
//                    case RandomDataGeneratorConstants.MODULE_CODE_ISBNREGISTRANT:
//                        result = faker.code().isbnRegistrant();
//                        break;
                    default:
                        throw new EventSimulationException(". Category 'code' does not have property : " + propertyType);
                }
                break;
            case RandomDataGeneratorConstants.MODULE_COMMERCE:
                switch (propertyType) {
                    case RandomDataGeneratorConstants.MODULE_COMMERCE_COLOUR:
                        result = faker.commerce().color();
                        break;
                    case RandomDataGeneratorConstants.MODULE_COMMERCE_DEPARTMENT:
                        result = faker.commerce().department();
                        break;
                    case RandomDataGeneratorConstants.MODULE_COMMERCE_MATERIAL:
                        result = faker.commerce().material();
                        break;
                    case RandomDataGeneratorConstants.MODULE_COMMERCE_PRICE:
                        result = faker.commerce().price(0,1000000);
                        break;
                    case RandomDataGeneratorConstants.MODULE_COMMERCE_PRODUCT_NAME:
                        result = faker.commerce().productName();
                        break;
                    case RandomDataGeneratorConstants.MODULE_COMMERCE_PROMOTION_CODE:
                        result = faker.commerce().promotionCode();
                        break;
                    default:
                        throw new EventSimulationException(". Category 'commerce' does not have property : " + propertyType);
                }
                break;

            case RandomDataGeneratorConstants.MODULE_COMPANY:
                switch (propertyType) {
//                    case RandomDataGeneratorConstants.MODULE_COMPANY_BS:
//                        result = faker.company().bs();
//                        break;
//                    case RandomDataGeneratorConstants.MODULE_COMPANY_BUZZWORD:
//                        result = faker.company().buzzword();
//                        break;
//                    case RandomDataGeneratorConstants.MODULE_COMPANY_CATCH_PHRASE:
//                        result = faker.company().catchPhrase();
//                        break;
                    case RandomDataGeneratorConstants.MODULE_COMPANY_INDUSTRY:
                        result = faker.company().industry();
                        break;
                    case RandomDataGeneratorConstants.MODULE_COMPANY_NAME:
                        result = faker.company().name();
                        break;
                    case RandomDataGeneratorConstants.MODULE_COMPANY_PROFESSION:
                        result = faker.company().profession();
                        break;
                    case RandomDataGeneratorConstants.MODULE_COMPANY_URL:
                        result = faker.company().url();
                        break;
                    default:
                        throw new EventSimulationException(". Category 'company' does not have property : " + propertyType);
                }
                break;

            case RandomDataGeneratorConstants.MODULE_CRYPTO:
                switch (propertyType) {
                    case RandomDataGeneratorConstants.MODULE_CRYPTO_MD5:
                        result = faker.crypto().md5();
                        break;
                    case RandomDataGeneratorConstants.MODULE_CRYPTO_SHA1:
                        result = faker.crypto().sha1();
                        break;
                    case RandomDataGeneratorConstants.MODULE_CRYPTO_SHA256:
                        result = faker.crypto().sha256();
                        break;
                    case RandomDataGeneratorConstants.MODULE_CRYPTO_SHA512:
                        result = faker.crypto().sha512();
                        break;
                    default:
                        throw new EventSimulationException(". Category 'crypto' does not have property : " + propertyType);
                }
                break;

            case RandomDataGeneratorConstants.MODULE_DATE:

                Date date = new Date();

//                todo R 24/02/2017 get time,day and month related data
                switch (propertyType) {
                    case RandomDataGeneratorConstants.MODULE_DATE_BETWEEN:
                        Calendar calendar =Calendar.getInstance();
                        Date today = calendar.getTime();
                        calendar.add(Calendar.YEAR,-5);
                        Date datePast = calendar.getTime();
                        calendar.add(Calendar.YEAR,10);
                        Date dateFuture = calendar.getTime();
                        result = faker.date().between(datePast,dateFuture);
                        break;
                    case RandomDataGeneratorConstants.MODULE_DATE_FUTURE:
                        result = faker.date().future(1825,TimeUnit.DAYS,date);
                        break;
                    case RandomDataGeneratorConstants.MODULE_DATE_PAST:
                        result = faker.date().past(1825,TimeUnit.DAYS,date);
                        break;
                    default:
                        throw new EventSimulationException(". Category 'date' does not have property : " + propertyType);
                }
                break;


            case RandomDataGeneratorConstants.MODULE_FINANCE:

                switch (propertyType) {
                    case RandomDataGeneratorConstants.MODULE_FINANCE_BIC:
                        result = faker.finance().bic();
                        break;
                    case RandomDataGeneratorConstants.MODULE_FINANCE_CREDIT_CARD:
                        result = faker.finance().creditCard();
                        break;
                    case RandomDataGeneratorConstants.MODULE_FINANCE_IBAN:
                        result = faker.finance().iban();
                        break;
//                    case RandomDataGeneratorConstants.MODULE_FINANCE_PIN_CODE:
////                        todo R 24/02/2017 faker doesnt support pin code. use numerify("####") or a regexify() instead?
//                        result = finance.pinCode();
//                        break;
                    default:
                        throw new EventSimulationException(". Category 'finance' does not have property : " + propertyType);
                }
                break;
            case RandomDataGeneratorConstants.MODULE_ID_NUMBER:

                switch (propertyType) {
                    case RandomDataGeneratorConstants.MODULE_ID_NUMBER_SSN:
                        result = faker.idNumber().ssnValid();
                        break;
                }
                break;

            case RandomDataGeneratorConstants.MODULE_INTERNET:

                switch (propertyType) {
                    case RandomDataGeneratorConstants.MODULE_INTERNET_DOMAIN_NAME:
                        result = faker.internet().domainName();
                        break;
                    case RandomDataGeneratorConstants.MODULE_INTERNET_EMAIL:
                        result = faker.internet().emailAddress();
                        break;
                    case RandomDataGeneratorConstants.MODULE_INTERNET_IPV4_ADDRESS:
                        result = faker.internet().ipV4Address();
                        break;
                    case RandomDataGeneratorConstants.MODULE_INTERNET_IPV4_CIDR_ADDRESS:
                        result = faker.internet().ipV4Cidr();
                        break;
                    case RandomDataGeneratorConstants.MODULE_INTERNET_IPV6_ADDRESS:
                        result = faker.internet().ipV6Address();
                        break;
                    case RandomDataGeneratorConstants.MODULE_INTERNET_IPV6_CIDR_ADDRESS:
                        result = faker.internet().ipV6Cidr();
                        break;
                    case RandomDataGeneratorConstants.MODULE_INTERNET_MAC_ADDRESS:
                        result = faker.internet().macAddress();
                        break;
                    case RandomDataGeneratorConstants.MODULE_INTERNET_PASSWORD:
                        result = faker.internet().password(8,15,true,true);
                        break;
                    case RandomDataGeneratorConstants.MODULE_INTERNET_PRIVATE_IPV4_ADDRESS:
                        result = faker.internet().privateIpV4Address();
                        break;
                    case RandomDataGeneratorConstants.MODULE_INTERNET_PUBLIC_IPV4_ADDRESS:
                        result = faker.internet().publicIpV4Address();
                        break;
                    case RandomDataGeneratorConstants.MODULE_INTERNET_SAFE_EMAIL:
                        result = faker.internet().safeEmailAddress();
                        break;
                    case RandomDataGeneratorConstants.MODULE_INTERNET_URL:
                        result = faker.internet().url();
                        break;
                    default:
                        throw new EventSimulationException(". Category 'internet' does not have property : " + propertyType);
                }
                break;

//                     todo R 24/02/2017 altitude, depth., coordinates, geohash

            case RandomDataGeneratorConstants.MODULE_LOREM:

                switch (propertyType) {
                    case RandomDataGeneratorConstants.MODULE_LOREM_CHARACTER:
                        result = faker.lorem().character();
                        break;
                    case RandomDataGeneratorConstants.MODULE_LOREM_PARAGRAPH:
                        result = faker.lorem().paragraph();
                        break;
                    case RandomDataGeneratorConstants.MODULE_LOREM_SENTENCE:
                        result = faker.lorem().sentence();
                        break;
                    case RandomDataGeneratorConstants.MODULE_LOREM_WORD:
                        result = faker.lorem().word();
                        break;
                    default:
                        throw new EventSimulationException(". Category 'lorem' does not have property : " + propertyType);
                }
                break;

            case RandomDataGeneratorConstants.MODULE_NAME:
                switch (propertyType) {
                    case RandomDataGeneratorConstants.MODULE_NAME_FIRST_NAME:
                        result = faker.name().firstName();
                        break;
                    case RandomDataGeneratorConstants.MODULE_NAME_FULL_NAME:
                        result = faker.name().name();
                        break;
                    case RandomDataGeneratorConstants.MODULE_NAME_LAST_NAME:
                        result = faker.name().lastName();
                        break;
                    case RandomDataGeneratorConstants.MODULE_NAME_TITLE:
                        result = faker.name().title();
                        break;
                    case RandomDataGeneratorConstants.MODULE_NAME_USERNAME:
                        result = faker.name().username();
                        break;
                    default:
                        throw new EventSimulationException(". Category 'name' does not have property : " + propertyType);
                }
                break;

            case RandomDataGeneratorConstants.MODULE_PHONE_NUMBER:
                switch (propertyType) {
                    case RandomDataGeneratorConstants.MODULE_PHONE_NUMBER_CELL_PHONE:
                        result = faker.phoneNumber().cellPhone();
                        break;
                    default:
                        throw new EventSimulationException(". Category 'phone number' does not have property : " + propertyType);
                }
                break;

            default:
                throw new EventSimulationException(". Category type '" + categoryType + "' is not available in the library.");
        }
        return result;
    }

    /**
     * Generate data with in given data list
     * <p>
     * Initialize Random to select random element from array
     *
     * @param customDataList Array of data
     * @return generated data from array
     */
    public static Object generateCustomRandomData(String[] customDataList) {
        Random random = new Random();
        int randomElementSelector = random.nextInt(customDataList.length);
        Object result;
        result = customDataList[randomElementSelector];
        return result;
    }

    /**
     * Validate Regular Expression
     *
     * @param regularExpression regularExpression
     */
    public static void validateRegularExpression(String regularExpression) {
        try {
            Pattern.compile(regularExpression);
        } catch (PatternSyntaxException e) {
            throw new EventSimulationException("Invalid regular expression : '" + regularExpression + "'. Error: " + e.getMessage());
        }

    }


}
