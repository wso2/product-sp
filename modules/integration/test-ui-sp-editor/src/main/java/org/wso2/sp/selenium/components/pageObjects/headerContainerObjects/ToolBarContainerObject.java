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

package org.wso2.sp.selenium.components.pageObjects.headerContainerObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ToolBarContainerObject {
    private WebDriver driver;
    private By run = By.className("run_btn");
    private By debug = By.className("debug_btn");
    private By stop = By.className("stop_btn");
    private By revert = By.className("revert_btn");

    public ToolBarContainerObject(WebDriver driver) {
        this.driver = driver;
    }

    public void clickRun() {
        driver.findElement(run).click();
    }

    public void clickDebug() {
        driver.findElement(debug).click();
    }

    public void clickStop() {
        driver.findElement(stop).click();
    }

    public void clickRevert() {
        driver.findElement(revert).click();
    }

    public void waitForElement(int seconds, String waitConditionLocator) {
        WebDriverWait wait = new WebDriverWait(driver, seconds);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(waitConditionLocator)));
    }

}
