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

public class MenuBarContainerObject {
    private WebDriver driver;
    private By file = By.id("File");
    private By edit = By.id("Edit");
    private By run = By.id("Run");
    private By tools = By.id("Tools");
    private By deploy = By.id("Deploy");

    public MenuBarContainerObject(WebDriver driver) {
        this.driver = driver;
    }

    public void clickFile() {
        driver.findElement(file).click();
    }

    public void clickEdit() {
        driver.findElement(edit).click();
    }

    public void clickRun() {
        driver.findElement(run).click();
    }

    public void clickTools() {
        driver.findElement(tools).click();
    }

    public void clickDeploy() {
        driver.findElement(deploy).click();
    }

}
