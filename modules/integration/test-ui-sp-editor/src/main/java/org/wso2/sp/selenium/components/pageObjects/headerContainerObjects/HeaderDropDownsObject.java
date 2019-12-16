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

public class HeaderDropDownsObject {
    private WebDriver driver;

    public HeaderDropDownsObject(WebDriver driver) {
        this.driver = driver;
    }

    //File Dropdown attributes
    private By newFile = By.id("new");
    private By openFile = By.id("open");
    private By importSample = By.id("openSample");
    private By save = By.id("save");
    private By saveAs = By.id("saveAs");
    private By importFile = By.id("import");
    private By exportFile = By.id("export");
    private By exportAsDocker = By.id("exportAsDocker");
    private By closeFile = By.id("close");
    private By closeAllFiles = By.id("closeAll");
    private By deleteFile = By.id("delete");
    private By settings = By.id("settings");

    //File Dropdown methods
    public void clickNew() {
        driver.findElement(newFile).click();
    }

    public void clickOpenFile() {
        driver.findElement(openFile).click();
    }

    public void clickImportSample() {
        driver.findElement(importSample).click();
    }

    public void clickSave() {
        driver.findElement(save).click();
    }

    public void clickSaveAs() {
        driver.findElement(saveAs).click();
    }

    public void clickImportFile() {
        driver.findElement(importFile).click();
    }

    public void clickExportFile() {
        driver.findElement(exportFile).click();
    }

    public void clickExportAsDocker() {
        driver.findElement(exportAsDocker).click();
    }

    public void clickCloseFile() {
        driver.findElement(closeFile).click();
    }

    public void clickCloseAllFiles() {
        driver.findElement(closeAllFiles).click();
    }

    public void clickDeleteFile() {
        driver.findElement(deleteFile).click();
    }

    public void clickSettings() {
        driver.findElement(settings).click();
    }

    //Edit Dropdown attributes
    private By undo = By.id("undo");
    private By redo = By.id("redo");
    private By find = By.id("find");
    private By findAndReplace = By.id("findAndReplace");
    private By reformatCode = By.id("format");

    //Edit Dropdown Methods
    public void clickUndo() {
        driver.findElement(undo).click();
    }

    public void clickRedo() {
        driver.findElement(redo).click();
    }

    public void clickFind() {
        driver.findElement(find).click();
    }

    public void clickFindAndReplace() {
        driver.findElement(findAndReplace).click();
    }

    public void clickReformatCode() {
        driver.findElement(reformatCode).click();
    }

    //Run Dropdown attributes
    private By run = By.id("run");
    private By debug = By.id("debug");
    private By stop = By.id("stop");

    //Run Dropdown methods
    public void clickRun() {
        driver.findElement(run).click();
    }

    public void clickDebug() {
        driver.findElement(debug).click();
    }

    public void clickStop() {
        driver.findElement(stop).click();
    }

    //Tools Dropdown attributes
    private By fileExplorer = By.id("toggleFileExplorer");
    private By eventSimulator = By.id("toggleEventSimulator");
    private By console = By.id("toggleConsole");
    private By sampleEventGenerator = By.id("sampleEvent");
    private By siddhiStoreQuery = By.id("queryStore");
    private By tourGuide = By.id("tour-guide");

    //Tools Dropdown methods
    public void clickFileExplorer() {
        driver.findElement(fileExplorer).click();
    }

    public void clickEventSimulator() {
        driver.findElement(eventSimulator).click();
    }

    public void clickConsole() {
        driver.findElement(console).click();
    }

    public void clickSampleEventGenerator() {
        driver.findElement(sampleEventGenerator).click();
    }

    public void clickSiddhiStoreQuery() {
        driver.findElement(siddhiStoreQuery).click();
    }

    public void clickTourGuide() {
        driver.findElement(tourGuide).click();
    }

    //Deploy Dropdown attributes
    private By deployToServer = By.id("deploy-to-server");

    //Deploy Dropdown methods
    public void clickDeployToServer() {
        driver.findElement(deployToServer).click();
    }

}
