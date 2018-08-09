/*
 * Copyright (c)  2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.extension.siddhi.execution.country;

import org.apache.log4j.Logger;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.ReturnAttribute;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.exception.SiddhiAppRuntimeException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Returns the country of the given user location.
 * country(string)
 * Accept Type(s): STRING
 * Return Type(s): STRING
 */

@Extension(
        name = "country",
        namespace = "find",
        description = "Returns the country of the given user location.",
        parameters = {
                @Parameter(name = "input.string",
                        description = "The input string to derive the country.",
                        type = {DataType.STRING})
        },
        returnAttributes = @ReturnAttribute(
                description = "Outputs the country of the location provided.",
                type = {DataType.STRING}),
        examples = @Example(description = "This outputs the country of the provided location. In this scenario, the " +
                "output is 'India' .", syntax = "country(\"New Delhi, India\")")
)

public class CountryFunction extends FunctionExecutor {

    private static final Logger log = Logger.getLogger(CountryFunction.class);

    private Attribute.Type returnType = Attribute.Type.STRING;
    private List<String> countryList = new ArrayList<>();


    /**
     * The initialization method for {@link FunctionExecutor}, which will be called before other methods and validate
     * the all configuration and getting the initial values.
     *
     * @param attributeExpressionExecutors are the executors of each attributes in the Function
     * @param configReader                 this hold the {@link FunctionExecutor} extensions configuration reader.
     * @param siddhiAppContext             Siddhi app runtime context
     */
    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ConfigReader configReader,
                        SiddhiAppContext siddhiAppContext) {
        String line;
        if (attributeExpressionExecutors.length != 1) {
            throw new SiddhiAppValidationException("Invalid no of arguments passed to find:country() function. " +
                    "Required 1. Found " + attributeExpressionExecutors.length);
        } else if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.STRING) {
            throw new SiddhiAppValidationException(
                    "Invalid parameter type found for find:country() function, required " + Attribute.Type.STRING +
                            ", " + "but found " + attributeExpressionExecutors[0].getReturnType());
        }
        InputStream inputStream = CountryFunction.class.getResourceAsStream("/Countries.csv");
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,
                StandardCharsets.UTF_8))) {
            while ((line = bufferedReader.readLine()) != null) {
                countryList.add(line);
            }
        } catch (FileNotFoundException e) {
            log.error("File is not found : " + e.getMessage());
        } catch (IOException e) {
            log.error("Error occurred while reading file : " + e.getMessage());
        }
    }

    /**
     * The main execution method which will be called upon event arrival
     * when there are more than one Function parameter
     *
     * @param data the runtime values of Function parameters
     * @return the Function result
     */
    @Override
    protected Object execute(Object[] data) {
        return null; //Since the length function takes in only 1 parameter, this method does not get called.
        // Hence, not implemented.
    }

    /**
     * The main execution method which will be called upon event arrival
     * when there are zero or one Function parameter
     *
     * @param data null if the Function parameter count is zero or
     *             runtime data value of the Function parameter
     * @return the Function result
     */
    @Override
    protected Object execute(Object data) {
        String[] location;
        String[] countryValues;
        int i;
        int j;
        if (data == null) {
            throw new SiddhiAppRuntimeException("Invalid input given to find:length() function. " +
                    "The argument cannot be null");
        }
        for (String country : countryList) {
            location = data.toString().split("[ ,-]");
            countryValues = country.split(",");
            for (i = 0; i < location.length; i++) {
                for (j = 0; j < countryValues.length; j++) {
                    if (location[i].trim().equalsIgnoreCase(countryValues[j])) {
                        return countryValues[0];
                    }
                }
            }
        }
        return "undefined";
    }

    /**
     * return a Class object that represents the formal return type of the method represented by this Method object.
     *
     * @return the return type for the method this object represents
     */
    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }

    /**
     * Used to collect the serializable state of the processing element, that need to be
     * persisted for reconstructing the element to the same state on a different point of time
     *
     * @return stateful objects of the processing element as an map
     */
    @Override
    public Map<String, Object> currentState() {
        return null;
    }

    /**
     * Used to restore serialized state of the processing element, for reconstructing
     * the element to the same state as if was on a previous point of time.
     *
     * @param state the stateful objects of the processing element as a map.
     *              This is the same map that is created upon calling currentState() method.
     */
    @Override
    public void restoreState(Map<String, Object> state) {
        // State is not maintained here.
    }
}
