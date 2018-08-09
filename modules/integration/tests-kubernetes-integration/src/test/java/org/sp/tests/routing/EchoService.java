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
package org.sp.tests.routing;

import org.sp.tests.base.SPBaseTest;
import org.testng.annotations.Test;
/**
* EchoService class
 */
public class EchoService extends SPBaseTest {

    @Test public void getPostToEcho() throws Exception {

       String serviceURL = spURL + "/echo";
//
//        StringRequestEntity requestEntity = new StringRequestEntity("{ \"Hello\":\"DAS\" };", "application/json",
//                "UTF-8");
//
//        HttpClient client = new HttpClient();
//        PostMethod post = new PostMethod(serviceURL);
//
//        post.setRequestEntity(requestEntity);
//
//        int statuscode = client.executeMethod(post);
//        assertEquals(statuscode, 201);
    }
}
