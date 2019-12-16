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

package org.wso2.sp.selenium.components.pageObjects.pageContentObjects.sourceViewObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class WelcomePageObject {
    public WebDriver driver;

    public WelcomePageObject(WebDriver driver) {
        this.driver = driver;
    }

    //Welcome page attributes
    private By newButton = By.className("new-welcome-button");
    private By openButton = By.className("open-welcome-button");
    private By moreSamples = By.className("more-samples");

    //Welcome page methods
    public void clickNewButton() {
        driver.findElement(newButton).click();
    }

    public void clickOpenButton() {
        driver.findElement(openButton).click();
    }

    public void clickMoreSamples() {
        driver.findElement(moreSamples).click();
    }
}
