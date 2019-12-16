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

public class HeaderDropDownDialogsObject {
    private WebDriver driver;

    public HeaderDropDownDialogsObject(WebDriver driver) {
        this.driver = driver;
    }

    public void clickSamples(String sampleID) {
        driver.findElement(By.id(sampleID)).click();
    }

    public void selectFromDropdown(String dropdownID, String valueToBeSelected) {
        driver.findElement(By.id(dropdownID)).click();
        driver.findElement(By.cssSelector("option[value=" + valueToBeSelected + "]")).click();
    }

    public void clickGuideClose() throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, 30);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"guideDialog\"]/div/div/div[1]/button")));
        driver.findElement(By.xpath("//*[@id=\"guideDialog\"]/div/div/div[1]/button")).click();
        Thread.sleep(500);
    }

}
