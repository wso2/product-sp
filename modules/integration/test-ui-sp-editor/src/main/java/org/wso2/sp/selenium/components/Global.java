package org.wso2.sp.selenium.components;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class Global {

    private WebDriver driver;

    /**
     * Constructor
     *
     * @param driver Webdriver instance
     */
    public Global(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * This method clicks particular element by a given ID
     *
     * @param buttonID Element ID to identify the element
     */
    public void clickElementByID(String buttonID) {
        driver.findElement(By.id(buttonID)).click();
    }

    /**
     * This method clicks particular element by a given CSS selector
     *
     * @param cssSelector Element CSS selector to identify the element
     */
    public void clickElementByCssSelector(String cssSelector) {
        driver.findElement(By.cssSelector(cssSelector)).click();
    }

    /**
     * This method clicks particular element by a given link text
     *
     * @param linkText Element link text to identify the element
     */
    public void clickelementByLinkText(String linkText) {
        driver.findElement(By.id(linkText)).click();
    }

    /**
     * This method clicks particular element by a given Tag Name
     *
     * @param tagName Element Tag Name to identify the element
     */
    public void clickElementByTagName(String tagName) {
        driver.findElement(By.id(tagName)).click();
    }

    /**
     * This method clicks particular element by a given x path
     *
     * @param xPath Element x path to identify the element
     */
    public void clickElementByXPath(String xPath) {
        driver.findElement(By.id(xPath)).click();
    }

    /**
     * This method clicks particular element by a given name
     *
     * @param name Element name to identify the element
     */
    public void clickElementByName(String name) {
        driver.findElement(By.id(name)).click();
    }

    /**
     * This method fills input field with the given css selector and the value to be inserted
     *
     * @param cssSelector Css selector of the input field
     * @param valueToBeInserted value to be inserted
     */
    public void fillInputFieldByCssSelector(String cssSelector, String valueToBeInserted) {
        driver.findElement(By.name(cssSelector)).sendKeys(valueToBeInserted);
    }

    /**
     * This method fills input field with the given id and the value to be inserted
     *
     * @param id ID of the input field
     * @param valueToBeInserted value to be inserted
     */
    public void fillInputFieldByID(String id, String valueToBeInserted) {
        driver.findElement(By.name(id)).sendKeys(valueToBeInserted);
    }

    /**
     * This method fills input field with the given input field name and the value to be inserted
     *
     * @param fieldName Name of the input field
     * @param valueToBeInserted value to be inserted
     */
    public void fillInputFieldByName(String fieldName, String valueToBeInserted) {
        driver.findElement(By.name(fieldName)).sendKeys(valueToBeInserted);
    }

    /**
     * This will pause the test process for a given amount of time
     *
     * @param timeInMillis Amount of time to be waited in milliseconds
     * @throws  InterruptedException
     */
    public void pauseTest(int timeInMillis) throws InterruptedException {
        Thread.sleep(timeInMillis);
    }

}
