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

package org.wso2.sp.selenium.components.pageObjects.pageContentObjects.leftContainerObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LeftContainerObject {
    public WebDriver driver;

    public LeftContainerObject(WebDriver driver) {
        this.driver = driver;
    }

    //Left side bar attributes
    private By fileExplorer = By.className("workspace-explorer-activate-btn");
    private By eventSimulator = By.className("event-simulator-activate-btn");
    private By console = By.className("output-console-activate-btn");
    private By operatorFinder = By.className("operator-finder-activate-btn");

    //Left side methods
    public void clickFileExplorer() {
        driver.findElement(fileExplorer).click();
    }

    public void clickEventSimulator() {
        driver.findElement(eventSimulator);
    }

    public void clickConsole() {
        driver.findElement(console).click();
    }

    public void clickOperatorFinder() {
        driver.findElement(operatorFinder).click();
    }
}
