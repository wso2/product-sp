/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.sp.open.tracer.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This is the utils class for the analytics tracer component.
 *
 */
public class Utils {
    private static Gson gson = new Gson();

    public static String getJSONString(Map properties) {
        Iterator it = properties.entrySet().iterator();
        JSONArray jsonArray = new JSONArray();
        while (it.hasNext()) {
            JSONObject jsonObject = new JSONObject();
            Map.Entry pair = (Map.Entry) it.next();
            jsonObject.put(pair.getKey().toString(), pair.getValue().toString());
            jsonArray.put(jsonObject);
            it.remove();
        }
        return jsonArray.toString();
    }

    public static String getJSONString(List<AnalyticsSpan.Reference> references) {
        Type collectionType = new TypeToken<List<AnalyticsSpan.Reference>>() {
        }.getType();
        return gson.toJson(references, collectionType);
    }
}
