package com.nopcommerce.tests;

import com.nopcommerce.listeners.AllureListener;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;

import java.io.File;
import java.net.URL;
import java.time.Duration;

@Listeners(AllureListener.class)
public class BaseTest {
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    protected WebDriver driver;

    public static WebDriver getDriver() {
        return driverThreadLocal.get();
    }

    @BeforeClass
    public void setUp() throws Exception {
        driver = createWebDriver();
        driverThreadLocal.set(driver);

        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.get(getBaseUrl());
    }

    private WebDriver createWebDriver() throws Exception {
        ChromeOptions options = getChromeOptions();
        String gridUrl = System.getProperty("gridUrl", System.getenv().getOrDefault("SELENIUM_HUB_URL", ""));

        if (!gridUrl.isEmpty()) {
            options.addArguments("--headless=new");
            return new RemoteWebDriver(new URL(gridUrl), options);
        }

        if (Boolean.getBoolean("headless")) {
            options.addArguments("--headless=new");
        }
        return new ChromeDriver(options);
    }

    private ChromeOptions getChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        return options;
    }

    private String getBaseUrl() {
        return System.getProperty("baseUrl", System.getenv().getOrDefault("BASE_URL", "http://localhost:8080"));
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
            driverThreadLocal.remove();
        }
    }

    public static void autoOpenAllureReport() {
        if (System.getenv("CI") != null || System.getenv("GITHUB_ACTIONS") != null) {
            return;
        }
        try {
            File resultsDir = new File("target/allure-results");
            if (!resultsDir.exists()) {
                resultsDir = new File("allure-results");
            }
            String resultsPath = resultsDir.getAbsolutePath();

            File reportDir = new File("allure-report");
            String reportPath = reportDir.getAbsolutePath();

            // 1. Tạo thư mục báo cáo HTML tĩnh (allure-report)
            new ProcessBuilder("allure", "generate", resultsPath, "-o", reportPath, "--clean").start().waitFor();
        } catch (Exception ignored) {
        }
    }
}
