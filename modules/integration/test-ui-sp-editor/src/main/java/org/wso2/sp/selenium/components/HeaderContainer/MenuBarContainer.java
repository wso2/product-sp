package org.wso2.sp.selenium.components.HeaderContainer;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class MenuBarContainer {

    private WebDriver driver;
    private By file = By.id("File");
    private By edit = By.id("Edit");
    private By run = By.id("Run");
    private By tools = By.id("Tools");
    private By deploy = By.id("Deploy");

    public MenuBarContainer(WebDriver driver) {
        this.driver = driver;
    }

    public void clickFile() {
        driver.findElement(file).click();
    }

    public void clickEdit() {
        driver.findElement(edit).click();
    }

    public void clickRun() {
        driver.findElement(run).click();
    }

    public void clickTools() {
        driver.findElement(tools).click();
    }

    public void clickDeploy() {
        driver.findElement(deploy).click();
    }

    public void clickClose() throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver,30);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"guideDialog\"]/div/div/div[1]/button")));
        driver.findElement(By.xpath("//*[@id=\"guideDialog\"]/div/div/div[1]/button")).click();
        Thread.sleep(500);
    }

}
