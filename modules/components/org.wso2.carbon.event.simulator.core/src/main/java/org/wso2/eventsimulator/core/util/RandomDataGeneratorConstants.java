/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

    public static final String PRIMITIVE_BASED_ATTRIBUTE = "PRIMITIVEBASED";
    public static final String PROPERTY_BASED_ATTRIBUTE = "PROPERTYBASED";
    public static final String REGEX_BASED_ATTRIBUTE = "REGEXBASED";
    public static final String CUSTOM_DATA_BASED_ATTRIBUTE = "CUSTOMDATA";

    //constants for Property based data generator
    public static final String MODULE_ADDRESS = "address";
    public static final String MODULE_APP = "app";
    public static final String MODULE_BOOL = "bool";
    public static final String MODULE_BUSINESS = "business";
    public static final String MODULE_CODE = "code";
    public static final String MODULE_COMMERCE= "commerce";
    public static final String MODULE_COMPANY = "company";
    public static final String MODULE_CRYPTO = "crypto";
    public static final String MODULE_DATE = "date";
    public static final String MODULE_FINANCE = "finance";
    public static final String MODULE_ID_NUMBER = "idNumber";
    public static final String MODULE_INTERNET = "internet";
    public static final String MODULE_LOREM = "lorem";
    public static final String MODULE_NAME = "name";
    public static final String MODULE_PHONE_NUMBER= "phoneNumber";


//constants for each category/module

//    Address
    public static final String MODULE_ADDRESS_BUILDING_NUMBER = "buildingNumber";
    public static final String MODULE_ADDRESS_CITY = "city";
    public static final String MODULE_ADDRESS_COUNTRY = "country";
    public static final String MODULE_ADDRESS_COUNTRY_CODE = "countryCode";
    public static final String MODULE_ADDRESS_LATITUDE = "latitude";
    public static final String MODULE_ADDRESS_LONGITUDE = "longitude";
    public static final String MODULE_ADDRESS_STATE = "state";
    public static final String MODULE_ADDRESS_STATE_ABBREVIATION = "stateAbbreviation";
    public static final String MODULE_ADDRESS_STREET_ADDRESS = "streetAddress";
    public static final String MODULE_ADDRESS_STREET_ADDRESS_NUMBER = "streetAddressNumber";
    public static final String MODULE_ADDRESS_STREET_NAME = "streetName";
    public static final String MODULE_ADDRESS_TIME_ZONE = "timeZone";
    public static final String MODULE_ADDRESS_ZIP_CODE = "zipCode";

//    App
    public static final String MODULE_APP_NAME = "name";
    public static final String MODULE_APP_VERSION = "version";
    public static final String MODULE_APP_AUTHOR = "author";

//    Bool
    public static final String MODULE_BOOL_BOOL = "bool";

//    Business
    public static final String MODULE_BUSINESS_CREDIT_CARD_EXPIRY = "expiry";
    public static final String MODULE_BUSINESS_CREDIT_CARD_NUMBER = "number";
    public static final String MODULE_BUSINESS_CREDIT_CARD_TYPE = "type";

//    Code
    public static final String MODULE_CODE_ASIN = "asin";
    public static final String MODULE_CODE_EAN8 = "ean8";
    public static final String MODULE_CODE_EAN13 = "ean13";
    public static final String MODULE_CODE_GTIN8 = "gtin8";
    public static final String MODULE_CODE_GTIN13 = "gtin13";
    public static final String MODULE_CODE_IMEI = "imei";
//    public static final String MODULE_CODE_ISBN10 = "isbn10";
//    public static final String MODULE_CODE_ISBN13 = "isbn13";
//    public static final String MODULE_CODE_ISBN_GROUP = "isbnGroup";
//    public static final String MODULE_CODE_ISBNGS1 = "isbnGs1";
//    public static final String MODULE_CODE_ISBNREGISTRANT = "isbnRegistrant";

//    Commerce
    public static final String MODULE_COMMERCE_COLOUR = "colour";
    public static final String MODULE_COMMERCE_DEPARTMENT = "department";
    public static final String MODULE_COMMERCE_MATERIAL = "material";
    public static final String MODULE_COMMERCE_PRICE = "price";
    public static final String MODULE_COMMERCE_PRODUCT_NAME = "productName";
    public static final String MODULE_COMMERCE_PROMOTION_CODE = "promotionCode";

//    Company
//    public static final String MODULE_COMPANY_BS = "bs";
//    public static final String MODULE_COMPANY_BUZZWORD = "buzzWord";
//    public static final String MODULE_COMPANY_CATCH_PHRASE = "catchPhrase";
    public static final String MODULE_COMPANY_INDUSTRY = "industry";
    public static final String MODULE_COMPANY_NAME = "name";
    public static final String MODULE_COMPANY_PROFESSION = "profession";
    public static final String MODULE_COMPANY_URL = "url";

//    Crypto
    public static final String MODULE_CRYPTO_MD5 = "md5";
    public static final String MODULE_CRYPTO_SHA1 = "sha1";
    public static final String MODULE_CRYPTO_SHA256 = "sha256";
    public static final String MODULE_CRYPTO_SHA512 = "sha512";

    //    DateAndTime
    public static final String MODULE_DATE_PAST = "pastDate";
    public static final String MODULE_DATE_FUTURE = "futureDate";
    public static final String MODULE_DATE_BETWEEN = "dateBetween";

//       Finance
    public static final String MODULE_FINANCE_BIC = "bic";
    public static final String MODULE_FINANCE_CREDIT_CARD = "creditCard";
    public static final String MODULE_FINANCE_IBAN = "iban";

//       ID number
    public static final String MODULE_ID_NUMBER_SSN = "ssn";

//       Internet
    public static final String MODULE_INTERNET_DOMAIN_NAME = "domainName";
    public static final String MODULE_INTERNET_EMAIL = "email";
    public static final String MODULE_INTERNET_IPV4_ADDRESS = "ipv4Address";
    public static final String MODULE_INTERNET_IPV4_CIDR_ADDRESS = "ipv4CidrAddress";
    public static final String MODULE_INTERNET_IPV6_ADDRESS = "ipv6Address";
    public static final String MODULE_INTERNET_IPV6_CIDR_ADDRESS = "ipv6CidrAddress";
    public static final String MODULE_INTERNET_MAC_ADDRESS = "macAddress";
    public static final String MODULE_INTERNET_PASSWORD = "password";
    public static final String MODULE_INTERNET_PRIVATE_IPV4_ADDRESS = "privateIpv4Address";
    public static final String MODULE_INTERNET_PUBLIC_IPV4_ADDRESS = "publicIpv4Address";
    public static final String MODULE_INTERNET_SAFE_EMAIL = "safeEmail";
    public static final String MODULE_INTERNET_URL = "url";

//    Lorem
    public static final String MODULE_LOREM_CHARACTER = "character";
    public static final String MODULE_LOREM_PARAGRAPH = "paragraph";
    public static final String MODULE_LOREM_SENTENCE = "sentence";
    public static final String MODULE_LOREM_WORD = "word";

//    Name
    public static final String MODULE_NAME_FIRST_NAME = "firstName";
    public static final String MODULE_NAME_FULL_NAME = "fullName";
    public static final String MODULE_NAME_LAST_NAME = "lastName";
    public static final String MODULE_NAME_TITLE = "title";
    public static final String MODULE_NAME_USERNAME = "username";

//    Phone number
    public static final String MODULE_PHONE_NUMBER_CELL_PHONE = "cellPhone";

}
