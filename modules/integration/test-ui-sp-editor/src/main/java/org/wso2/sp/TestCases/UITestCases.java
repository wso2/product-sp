package org.wso2.sp.TestCases;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import static org.wso2.sp.selenium.util.SPUIIntegrationTest.*;

public class UITestCases {

    private WebDriver driver;

    @Test
    public void openASample() throws InterruptedException {
        driver = getDriver();
        setOptions(driver);

        getMenubar(driver).clickClose();
        getMenubar(driver).clickFile();
        getHeaderDropdowns(driver).clickImportSample();
        getRightContainer(driver).clickSamples("DataPreprocessing");
        driver.quit();
    }

    @Test
    public void createNewFile() throws InterruptedException {
        driver = getDriver();
        setOptions(driver);

        getMenubar(driver).clickClose();
        getMenubar(driver).clickFile();
        getHeaderDropdowns(driver).clickNew();
        driver.quit();
    }

    @Test
    public void simulateAnApp() throws InterruptedException {
        driver = getDriver();
        setOptions(driver);

        getMenubar(driver).clickClose();
        getMenubar(driver).clickTools();
        getHeaderDropdowns(driver).clickEventSimulator();
        getSimulation(driver).selectFromDropdown("siddhi-app-name", "SweetFactory__3");
        getSimulation(driver).selectFromDropdown("stream-name", "SweetProductionStream");
        getSimulation(driver).fillInput("name-attr", "Cake");
        getSimulation(driver).fillInput("amount-attr", "124");
        getSimulation(driver).clickStartAndSend();
        driver.quit();
    }

}
