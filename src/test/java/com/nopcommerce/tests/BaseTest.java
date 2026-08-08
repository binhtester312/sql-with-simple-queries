package com.nopcommerce.tests;

import com.nopcommerce.config.GlobalConstants;
import com.nopcommerce.enums.BrowserList;
import com.nopcommerce.enums.EnvironmentList;
import com.nopcommerce.listeners.AllureListener;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

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
    @Parameters({"browser", "env"})
    public void setUp(@Optional("chrome") String browserName, @Optional("local") String envName) {
        driver = getBrowserDriver(browserName, envName);
        driverThreadLocal.set(driver);
    }

    protected WebDriver getBrowserDriver(String browserName, String environmentName) {
        String gridUrl = System.getProperty("gridUrl", System.getenv().getOrDefault("SELENIUM_HUB_URL", ""));

        if (!gridUrl.isEmpty()) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
            try {
                driver = new RemoteWebDriver(new URL(gridUrl), options);
            } catch (Exception e) {
                throw new RuntimeException("Lỗi kết nối Selenium Grid: " + e.getMessage());
            }
        } else {
            BrowserList browserList = BrowserList.valueOf(browserName.toUpperCase());
            switch (browserList) {
                case CHROME:
                    ChromeOptions chromeOptions = new ChromeOptions();
                    chromeOptions.addArguments("--no-sandbox");
                    chromeOptions.addArguments("--disable-dev-shm-usage");
                    chromeOptions.addArguments("--window-size=1920,1080");
                    if (Boolean.getBoolean("headless")) {
                        chromeOptions.addArguments("--headless=new");
                    }
                    driver = new ChromeDriver(chromeOptions);
                    break;
                case CHROME_HEADLESS:
                    ChromeOptions headlessOptions = new ChromeOptions();
                    headlessOptions.addArguments("--no-sandbox");
                    headlessOptions.addArguments("--disable-dev-shm-usage");
                    headlessOptions.addArguments("--headless=new");
                    headlessOptions.addArguments("--window-size=1920,1080");
                    driver = new ChromeDriver(headlessOptions);
                    break;
                case FIREFOX:
                    driver = new FirefoxDriver();
                    break;
                case EDGE:
                    driver = new EdgeDriver();
                    break;
                default:
                    throw new RuntimeException("Browser name is not valid: " + browserName);
            }
        }

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(GlobalConstants.LONG_TIMEOUT));
        driver.manage().window().maximize();

        String url = getUrlByEnvironmentName(environmentName);
        driver.get(url);

        return driver;
    }

    private String getUrlByEnvironmentName(String environmentName) {
        String customUrl = System.getProperty("baseUrl", System.getenv("BASE_URL"));
        if (customUrl != null && !customUrl.isEmpty()) {
            return customUrl;
        }

        EnvironmentList envList = EnvironmentList.valueOf(environmentName.toUpperCase());
        switch (envList) {
            case LOCAL:
            case DEV:
                return "http://localhost:8080";
            case TESTING:
                return "https://testing.nopcommerce.com/";
            case STAGING:
                return "https://staging.nopcommerce.com/";
            default:
                throw new RuntimeException("Environment name is not valid: " + environmentName);
        }
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

            new ProcessBuilder("allure", "generate", resultsPath, "-o", reportPath, "--clean").start().waitFor();
        } catch (Exception ignored) {
        }
    }
}
