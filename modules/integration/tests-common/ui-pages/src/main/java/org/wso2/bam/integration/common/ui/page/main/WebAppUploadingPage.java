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
import org.wso2.appserver.integration.common.ui.page.util.UIElementMapper;

import java.io.IOException;

public class WebAppUploadingPage {
    private static final Log log = LogFactory.getLog(WebAppUploadingPage.class);
    private WebDriver driver;

    public WebAppUploadingPage(WebDriver driver) throws IOException {
        this.driver = driver;
        UIElementMapper uiElementMapper = UIElementMapper.getInstance();
        // Check that we're on the right page.
        driver.findElement(By.xpath(uiElementMapper.getElement("webapp.add.xpath"))).click();

        if (!driver.findElement(By.id(uiElementMapper.getElement("webapp.list.page.middle"))).
                getText().contains("Upload Web Applications")) {

            throw new IllegalStateException("This is not the upload web application page Page");
        }
    }

    public boolean uploadWebApp(String filePath) {
        String responseMessage;
        driver.findElement(By.xpath("//*[@id=\"webappTbl\"]/tbody/tr/td[2]/input[1]")).sendKeys(filePath);
        driver.findElement(By.xpath("//*[@id=\"workArea\"]/form/table[2]/tbody/tr/td/input[1]")).click();
        responseMessage = driver.findElement(By.id("dialog")).getText();
        log.info(responseMessage);
        driver.findElement(By.xpath("/html/body/div[3]/div[2]/button")).click();
        return ("Web application has been uploaded successfully. " +
                "Please refresh this page in a while to see the status of the running webapps.").equalsIgnoreCase(responseMessage);

    }
}
