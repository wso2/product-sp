package org.wso2.sp.selenium.components.HeaderContainer;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ToolBarContainer {

    private WebDriver driver;
    private By run = By.className("run_btn");
    private By debug = By.className("debug_btn");
    private By stop = By.className("stop_btn");
    private By revert = By.className("revert_btn");

    public ToolBarContainer(WebDriver driver) {
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

    public void waitForElement(int seconds, String waitConditionLocator){
        WebDriverWait wait = new WebDriverWait(driver, seconds);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(waitConditionLocator)));
    }

}
