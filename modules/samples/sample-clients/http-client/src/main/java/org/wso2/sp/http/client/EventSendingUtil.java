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

package org.wso2.sp.http.client;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.stream.input.InputHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Event generating util class for http source
 */
public class EventSendingUtil {
    private static final Logger log = Logger.getLogger(EventSendingUtil.class);
    public static void publishEvents(List<String[]> fileEntriesList, boolean sendEventsContinuously,
                                     int noOfEventsToSend, String eventDefinition, String[] sweetName,
                                     InputHandler inputHandler, int delay, boolean isBinaryMessage,
                                     boolean continuouslyReadFile)
            throws InterruptedException {
        String message = null;
        int sentEvents = 0;
        Iterator iterator = null;
        ArrayList<Object> objectList;
        if (fileEntriesList != null) {
            iterator = fileEntriesList.iterator();
        }

        while (sendEventsContinuously || sentEvents != noOfEventsToSend) {
            objectList = new ArrayList<>();
            if (null != iterator && iterator.hasNext()) {
                String[] stringArray = (String[]) iterator.next();
                message = eventDefinition;
                for (int i = 0; i < stringArray.length; i++) {
                    if (isBinaryMessage) {
                        objectList.add(stringArray[i]);
                    } else {
                        message = message.replace("{" + i + "}", stringArray[i]);
                    }
                }
            } else if (fileEntriesList == null) {
                double amount = ThreadLocalRandom.current().nextDouble(1, 10000);
                String name = sweetName[ThreadLocalRandom.current().nextInt(0, sweetName.length)];
                if (isBinaryMessage) {
                    objectList.add(name);
                    objectList.add(Math.round(amount * 100.0) / 100.0);
                } else {
                    message = eventDefinition.replace("{0}", name).replace("{1}", Double.toString(amount));
                }
            } else {
                break;
            }
            if (isBinaryMessage) {
                inputHandler.send(objectList.toArray());
                log.info("Sent event: " + Arrays.toString(objectList.toArray()));
            } else {
                inputHandler.send(new Object[]{message});
                log.info("Sent event: " + message);
            }
            sentEvents++;
            if (sentEvents != noOfEventsToSend && null != iterator && !iterator.hasNext() && continuouslyReadFile) {
                iterator = fileEntriesList.iterator();
            }
            Thread.sleep(delay);
        }
    }
}
