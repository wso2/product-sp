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
import org.wso2.siddhi.query.api.definition.Attribute;

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
//    todo 02/03/2017 instead of using faker revert back to fabricator

    public static Object generatePrimitiveBasedRandomData(Attribute.Type type, Object min, Object max, int length) {
        Object result = null;
        DecimalFormat format = new DecimalFormat();
        switch (type) {
            case INT:
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
            case BOOL:
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
