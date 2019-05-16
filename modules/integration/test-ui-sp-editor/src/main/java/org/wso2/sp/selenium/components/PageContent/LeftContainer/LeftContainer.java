package org.wso2.sp.selenium.components.PageContent.LeftContainer;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LeftContainer {

    public WebDriver driver;

    public LeftContainer(WebDriver driver) {
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
