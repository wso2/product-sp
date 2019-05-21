package org.wso2.sp.selenium.util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.wso2.sp.selenium.components.Global;
import org.wso2.sp.selenium.components.HeaderContainer.HeaderDropDowns;
import org.wso2.sp.selenium.components.HeaderContainer.MenuBarContainer;
import org.wso2.sp.selenium.components.HeaderContainer.ToolBarContainer;
import org.wso2.sp.selenium.components.PageContent.LeftContainer.LeftContainer;
import org.wso2.sp.selenium.components.PageContent.LeftContainer.Simulation;
import org.wso2.sp.selenium.components.PageContent.SourceView.RightContainer;
import org.wso2.sp.selenium.components.PageContent.SourceView.WelcomePage;

import java.util.concurrent.TimeUnit;

public class SPUIIntegrationTest {

    static WebDriver driver;

    /**
     * This method initiate the Webdriver and add arguments to the chrome driver
     *
     * @return webdriver instance
     */
    public static WebDriver getDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("headless");
        options.addArguments("window-size=1920,1080");
        driver = new ChromeDriver(options);
        return driver;
    }

    /**
     * This method initiate the get method of the driver using editor URL and make the implicit wait to 10 seconds
     *
     * @param driver Webdriver instance from the test method
     */
    public static void setOptions(WebDriver driver) {
        driver.get("http://localhost:9390/editor");
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

    /**
     * This method initiates a MenuBarContainer object which will be used in test cases
     *
     * @param driver Webdriver instance from the test method
     * @return MenuBarContainer instance
     */
    public static MenuBarContainer getMenubar(WebDriver driver) {
        MenuBarContainer menuBarContainer = new MenuBarContainer(driver);
        return menuBarContainer;
    }

    /**
     * This method initiates a ToolBarContainer object which will be used in test cases
     *
     * @param driver Webdriver instance from the test method
     * @return ToolBarContainer instance
     */
    public static ToolBarContainer getToolBar(WebDriver driver) {
        ToolBarContainer toolBarContainer = new ToolBarContainer(driver);
        return toolBarContainer;
    }

    /**
     * This method initiates a HeaderDropDowns object which will be used in test cases
     *
     * @param driver Webdriver instance from the test method
     * @return HeaderDropDowns instance
     */
    public static HeaderDropDowns getHeaderDropdowns(WebDriver driver) {
        HeaderDropDowns headerDropDowns = new HeaderDropDowns(driver);
        return headerDropDowns;
    }

    /**
     * This method initiates a LeftContainer object which will be used in test cases
     *
     * @param driver Webdriver instance from the test method
     * @return LeftContainer instance
     */
    public static LeftContainer getLeftContainer(WebDriver driver) {
        LeftContainer leftContainer = new LeftContainer(driver);
        return leftContainer;
    }

    /**
     * This method initiates a Simulation object which will be used in test cases
     *
     * @param driver Webdriver instance from the test method
     * @return Simulation instance
     */
    public static Simulation getSimulation(WebDriver driver) {
        Simulation simulation = new Simulation(driver);
        return simulation;
    }

    /**
     * This method initiates a RightContainer object which will be used in test cases
     *
     * @param driver Element to be present in the page
     * @return RightContainer instance
     */
    public static RightContainer getRightContainer(WebDriver driver) {
        RightContainer rightContainer = new RightContainer(driver);
        return rightContainer;
    }

    /**
     * This method initiates a WelcomePage object which will be used in test cases
     *
     * @param driver Element to be present in the page
     * @return WelcomePage instance
     */
    public static WelcomePage getWelcomePage(WebDriver driver) {
        WelcomePage welcomePage = new WelcomePage(driver);
        return welcomePage;
    }

    /**
     * This method initiates a Global object which will be used in test cases
     *
     * @param driver Element to be present in the page
     * @return global instance
     */
    public static Global getGlobalMethods(WebDriver driver) {
        Global global = new Global(driver);
        return global;
    }
}
