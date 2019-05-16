package org.wso2.sp.selenium.components.PageContent.SourceView;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class WelcomePage {

    public WebDriver driver;

    public WelcomePage(WebDriver driver) {
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
