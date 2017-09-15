/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.sp.tests.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 *
 * TestUtil class
 */
public abstract class TestResults {

    private static final Log log = LogFactory.getLog(TestResults.class);
    //method for msf4j service listener implementation
    public abstract void waitForResults(int retryCount, long interval);

    //This will verify the actual result with expected.
    public abstract boolean resultsFound(String eventMessage);

    //msf4j service listener - wait for and verify test results
    public synchronized void verifyResult(long interval, int maxRetry, String eventMessage) {
        boolean arrived = resultsFound(eventMessage);
        int i;
        while (!arrived) {
            for (i = 1; i <= maxRetry; i++) {
                arrived = resultsFound(eventMessage);
                try {
                    if (arrived) {
                        break;
                    }
                    wait(interval);
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                }
            }
            if (i == maxRetry + 1) {
                break;
            }
        }
    }

}
