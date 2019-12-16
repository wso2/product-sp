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

package org.wso2.sp.selenium.components.actionObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;

public class RootActionObject {
    private WebDriver driver;
    private Actions action;

    /**
     * Constructor
     *
     * @param driver Webdriver instance
     */
    public RootActionObject(WebDriver driver) {
        this.driver = driver;
        this.action = new Actions(driver);
    }

    /**
     * This method clicks particular element by a given ID
     *
     * @param buttonID Element ID to identify the element
     */
    public void clickElementByIDAction(String buttonID) {
        driver.findElement(By.id(buttonID)).click();
    }

    /**
     * This method clicks particular element by a given CSS selector
     *
     * @param cssSelector Element CSS selector to identify the element
     */
    public void clickElementByCssSelectorAction(String cssSelector) {
        driver.findElement(By.cssSelector(cssSelector)).click();
    }

    /**
     * This method clicks particular element by a given link text
     *
     * @param linkText Element link text to identify the element
     */
    public void clickElementByLinkTextAction(String linkText) {
        driver.findElement(By.linkText(linkText)).click();
    }

    /**
     * This method clicks particular element by a given Tag Name
     *
     * @param tagName Element Tag Name to identify the element
     */
    public void clickElementByTagNameAction(String tagName) {
        driver.findElement(By.tagName(tagName)).click();
    }

    /**
     * This method clicks particular element by a given x path
     *
     * @param xPath Element x path to identify the element
     */
    public void clickElementByXPathAction(String xPath) {
        driver.findElement(By.xpath(xPath)).click();
    }

    /**
     * This method clicks particular element by a given name
     *
     * @param name Element name to identify the element
     */
    public void clickElementByClassNameAction(String name) {
        driver.findElement(By.className(name)).click();
    }

    /**
     * This method fills input field with the given css selector and the value to be inserted
     *
     * @param cssSelector       Css selector of the input field
     * @param valueToBeInserted value to be inserted
     */
    public void fillInputFieldByCssSelectorAction(String cssSelector, String valueToBeInserted) {
        driver.findElement(By.cssSelector(cssSelector)).sendKeys(valueToBeInserted);
    }

    /**
     * This method fills input field with the given id and the value to be inserted
     *
     * @param id                ID of the input field
     * @param valueToBeInserted value to be inserted
     */
    public void fillInputFieldByIDAction(String id, String valueToBeInserted) {
        driver.findElement(By.id(id)).sendKeys(valueToBeInserted);
    }

    /**
     * This method fills input field with the given input field name and the value to be inserted
     *
     * @param className         Name of the input field
     * @param valueToBeInserted value to be inserted
     */
    public void fillInputFieldByClassNameAction(String className, String valueToBeInserted) {
        driver.findElement(By.className(className)).sendKeys(valueToBeInserted);
    }

    /**
     * This method hovers the mouse point on an given element
     *
     * @param id ID of the element field
     */
    public void hoverOnAnElementByIdAction(String id) {
        action.moveToElement(driver.findElement(By.id(id))).perform();
    }

    /**
     * This method hovers the mouse point on a given element
     *
     * @param cssSelector css selector of the element field
     */
    public void hoverOnAnElementByCssSelectorAction(String cssSelector) {
        action.moveToElement(driver.findElement(By.cssSelector(cssSelector))).perform();
    }

    /**
     * This method hovers the mouse point on a given element
     *
     * @param name name of the element field
     */
    public void hoverOnAnElementByClassNameAction(String name) {
        action.moveToElement(driver.findElement(By.className(name))).perform();
    }

    /**
     * This method hovers the mouse point on a given element
     *
     * @param tagName tagname of the element field
     */
    public void hoverOnAnElementByTagnameAction(String tagName) {
        action.moveToElement(driver.findElement(By.tagName(tagName))).perform();
    }

    /**
     * This method hovers the mouse point on a given element
     *
     * @param xPath xpath of the element field
     */
    public void hoverOnAnElementByXPathAction(String xPath) {
        action.moveToElement(driver.findElement(By.xpath(xPath))).perform();
    }

    /**
     * This method drags and drops a given element
     *
     * @param from ID of the from element
     * @param to   ID of the to element
     */
    public void dragAndDropByIdAction(String from, String to) {
        action.dragAndDrop(driver.findElement(By.id(from)), driver.findElement(By.id(to))).perform();
    }

    /**
     * This method drags and drops a given element
     *
     * @param from css Selector of the from element
     * @param to   css selector of the to element
     */
    public void dragAndDropByCssSelectorAction(String from, String to) {
        action.dragAndDrop(driver.findElement(By.cssSelector(from)), driver.findElement(By.cssSelector(to))).perform();
    }

    /**
     * This method drags and drops a given element
     *
     * @param from xpath of the from element
     * @param to   xpath of the to element
     */
    public void dragAndDropByXPathAction(String from, String to) {
        action.dragAndDrop(driver.findElement(By.xpath(from)), driver.findElement(By.xpath(to))).perform();
    }

    /**
     * This method drags and drops a given element to a specific location
     *
     * @param source  ID of the from element
     * @param xOffset x offset of the target
     * @param yOffset y offset of the target
     */
    public void dragAndDropToOffsetByIdAction(String source, int xOffset, int yOffset) {
        action.dragAndDropBy(driver.findElement(By.id(source)), xOffset, yOffset).perform();
    }

    /**
     * This method drags and drops a given element to a specific location
     *
     * @param source  css selector of the source element
     * @param xOffset x offset of the target
     * @param yOffset y offset of the target
     */
    public void dragAndDropToOffsetByCssCelectorAction(String source, int xOffset, int yOffset) {
        action.dragAndDropBy(driver.findElement(By.cssSelector(source)), xOffset, yOffset).perform();
    }

    /**
     * This method drags and drops a given element to a specific location
     *
     * @param source  xpath of the source element
     * @param xOffset x offset of the target
     * @param yOffset y offset of the target
     */
    public void dragAndDropToOffsetByXPathAction(String source, int xOffset, int yOffset) {
        action.dragAndDropBy(driver.findElement(By.xpath(source)), xOffset, yOffset).perform();
    }

    /**
     * This method right clicks on a given element
     *
     * @param id id of the source element
     */
    public void rightClickOnElementByIdAction(String id) {
        action.contextClick(driver.findElement(By.id(id))).perform();
    }

    /**
     * This method right clicks on a given element
     *
     * @param name name of the source element
     */
    public void rightClickOnElementByClassNameAction(String name) {
        action.contextClick(driver.findElement(By.className(name))).perform();
    }

    /**
     * This method right clicks on a given element
     *
     * @param cssSelector css selector of the source element
     */
    public void rightClickOnElementByCssSelectorAction(String cssSelector) {
        action.contextClick(driver.findElement(By.id(cssSelector))).perform();
    }

    /**
     * This will pause the test process for a given amount of time
     *
     * @param timeInMillis Amount of time to be waited in milliseconds
     * @throws InterruptedException interrupted exception
     */
    public void pauseTestAction(int timeInMillis) throws InterruptedException {
        Thread.sleep(timeInMillis);
    }
}
