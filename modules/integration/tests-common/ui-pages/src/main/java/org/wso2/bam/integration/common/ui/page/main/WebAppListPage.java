/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.appserver.integration.common.ui.page.main;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.wso2.appserver.integration.common.ui.page.util.UIElementMapper;

import java.io.IOException;
import java.util.List;

public class WebAppListPage {
    private static final Log log = LogFactory.getLog(WebAppListPage.class);
    private WebDriver driver;

    public WebAppListPage(WebDriver driver) throws IOException {
        this.driver = driver;
        UIElementMapper uiElementMapper = UIElementMapper.getInstance();
        // Check that we're on the right page.
        driver.findElement(By.xpath(uiElementMapper.getElement("webapp.list.xpath"))).click();

        if (!driver.findElement(By.id(uiElementMapper.getElement("webapp.list.page.middle"))).
                getText().contains("Running Applications")) {

            throw new IllegalStateException("This is not the Running Web Applications Page");
        }
    }

    public boolean findWebApp(String webAppContext) {
        WebElement table_element = driver.findElement(By.id("webappsTable"));
        List<WebElement> tr_collection = table_element.findElement(By.tagName("tbody"))
                .findElements(By.tagName("tr"));

        log.info("Number of rows in webapp table = " + tr_collection.size());
        if (tr_collection.size() == 0) {
            return false;
        }
        int row_num, col_num;
        row_num = 1;
        for (WebElement trElement : tr_collection) {
            List<WebElement> td_collection = trElement.findElements(By.tagName("td"));
            col_num = 1;
            for (WebElement tdElement : td_collection) {
                log.info("row # " + row_num + ", col # " + col_num + "text=" + tdElement.getText());
                if (tdElement.getText().equals(webAppContext)) {
                    log.info("Webapp context found");
                    return true;
                }
                col_num++;
            }
            row_num++;
        }
        return false;
    }

    public boolean deleteWebApp(String webAppContext) throws Exception {
        WebElement table_element = driver.findElement(By.id("webappsTable"));
        List<WebElement> tr_collection = table_element.findElement(By.tagName("tbody"))
                .findElements(By.tagName("tr"));
        if (tr_collection.size() == 0) {
            throw new Exception("Web App you are trying to delete not exists");
        }
        List<WebElement> td_collection;
        for (WebElement tr : tr_collection) {
            td_collection = tr.findElements(By.tagName("td"));
            if (webAppContext.equals(td_collection.get(1).getText())) {
                td_collection.get(0).findElement(By.tagName("input")).click();
                driver.findElement(By.id("delete2")).click();
                Assert.assertEquals(driver.findElement(By.id("messagebox-confirm")).getText()
                        , "Do you want to delete the selected applications?", "Delete Confirmation message mismatched");
                List<WebElement> buttons = driver.findElements(By.tagName("button"));
                for (WebElement button : buttons) {
                    if ("yes".equalsIgnoreCase(button.getText())) {
                        button.click();
                        break;
                    }
                }

                Assert.assertEquals(driver.findElement(By.id("messagebox-info")).getText()
                        , "Successfully deleted selected applications", "Web Application deletion failed" +
                        ". Message box content mis matched");
                driver.findElement(By.xpath("/html/body/div[3]/div[2]/button")).click();
                return true;
            }
        }
        throw new Exception("Web App you are trying to delete not exists");
    }

    public void loadWebAppURL(String URL) {
        driver.get(URL);
    }
}
