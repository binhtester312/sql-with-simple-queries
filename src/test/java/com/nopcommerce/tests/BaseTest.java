package com.nopcommerce.tests;

import com.nopcommerce.config.NopCommerceConfig;
import com.nopcommerce.database.DatabaseHelper;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.net.URL;
import java.time.Duration;

public class BaseTest {
    protected WebDriver driver;

    @BeforeClass
    public void setUp() {
        try {

            ChromeOptions options = new ChromeOptions();
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");

            driver = new RemoteWebDriver(new URL(NopCommerceConfig.SELENIUM_GRID_URL), options);
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            driver.get(NopCommerceConfig.BASE_URL);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
        try {
            DatabaseHelper.closeConnection();
        } catch (Exception e) {
            System.err.println("⚠️ Error closing Database connection: " + e.getMessage());
        }
    }
}
