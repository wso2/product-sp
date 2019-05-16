package org.wso2.sp.selenium.components.PageContent.LeftContainer;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class Simulation {

    private WebDriver driver;

    public Simulation(WebDriver driver) {
        this.driver = driver;
    }

    public void selectFromDropdown(String dropdownID, String valueToBeSelected) {
        driver.findElement(By.id(dropdownID)).click();
        driver.findElement(By.cssSelector("option[value="+valueToBeSelected+"]")).click();
    }

    public void fillInput(String fieldName, String valueToBeInserted) {
        driver.findElement(By.name(fieldName)).sendKeys(valueToBeInserted);
    }

    public void clickStartAndSend() {
        driver.findElement(By.id("start-and-send")).click();
    }

}
