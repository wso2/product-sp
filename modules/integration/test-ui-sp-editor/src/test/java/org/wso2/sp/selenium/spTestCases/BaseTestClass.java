/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.sp.selenium.spTestCases;

import org.openqa.selenium.chrome.ChromeOptions;
import org.yaml.snakeyaml.Yaml;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class BaseTestClass {

    /**
     * This method returns the editor url from properties.yml
     *
     * @return editor_url
     */
    public String getEditorUrl() throws IOException {
        Yaml yaml = new Yaml();
        try (InputStream in = baseTestClass.class.getResourceAsStream("/properties.yml")) {
            Object obj = yaml.load(in);
            Map map = (Map) obj;
            return String.valueOf((map.get("editor_url")));
        }
    }

    /**
     * This method passes chrome options to web driver
     *
     * @return ChromeOptions
     */
    public ChromeOptions buildChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("headless");
        options.addArguments("window-size=1920,1080");

        return options;
    }
}
