package org.wso2.sp.selenium.components.PageContent.SourceView;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class RightContainer {

    private WebDriver driver;

    public RightContainer(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * To click the sample
     *
     * @param sampleID ID of the sample you need to click
     */
    public void clickSamples(String sampleID){
        driver.findElement(By.id(sampleID)).click();
    }

    /**
     * To shift among tabs
     *
     * @param tabID ID of the tab you want to navigate
     */
    public void changeActiveTab(String tabID){
        driver.findElement(By.id(tabID)).click();
    }

}
