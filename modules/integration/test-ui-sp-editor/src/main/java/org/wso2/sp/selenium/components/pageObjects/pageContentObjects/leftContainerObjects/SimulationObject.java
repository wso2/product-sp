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

public class SimulationObject {
    private WebDriver driver;

    public SimulationObject(WebDriver driver) {
        this.driver = driver;
    }

    public void selectFromDropdown(String dropdownID, String valueToBeSelected) {
        driver.findElement(By.id(dropdownID)).click();
        driver.findElement(By.cssSelector("option[value=" + valueToBeSelected + "]")).click();
    }

    public void fillInput(String fieldName, String valueToBeInserted) {
        driver.findElement(By.name(fieldName)).sendKeys(valueToBeInserted);
    }

    public void clickStartAndSend() {
        driver.findElement(By.id("start-and-send")).click();
    }
}
