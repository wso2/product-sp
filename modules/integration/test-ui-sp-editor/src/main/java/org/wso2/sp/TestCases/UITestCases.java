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

package org.wso2.sp.TestCases;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.Test;
import org.wso2.sp.selenium.components.ActionObjects.ActionObject;
import org.wso2.sp.selenium.components.PageObjects.HeaderContainer.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.wso2.sp.selenium.util.SPUIIntegrationTest.*;

public class UITestCases {

    @Test
    public void openASample() throws IOException, InterruptedException {

        //initiate the Webdriver and add arguments to the chrome driver
        WebDriver driver = new ChromeDriver(setupChromeOptions());

        //initiate the get method of the driver using editor URL and make the implicit wait to 10 seconds
        driver.get(getEditorUrl());
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        //initiate page objects
        ActionObject action = new ActionObject(driver);
        MenuBarContainer menuBar = new MenuBarContainer(driver);
        HeaderDropDowns dropdown = new HeaderDropDowns(driver);
        HeaderDropDownDialogs dropdownDialogs = new HeaderDropDownDialogs(driver);

        //test scenario
        dropdownDialogs.clickGuideClose();
        menuBar.clickFile();
        dropdown.clickImportSample();
        dropdownDialogs.clickSamples("DataPreprocessing");
        action.hoverOnAnElementByClassName("workspace-explorer-activate-btn");

        driver.quit();
    }

}
