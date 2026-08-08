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
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        // Đọc Selenium Grid URL từ System Property hoặc Env Var
        // CI/CD sẽ set SELENIUM_HUB_URL, local dev để trống
        String gridUrl = System.getProperty("gridUrl",
            System.getenv().getOrDefault("SELENIUM_HUB_URL", ""));

        if (!gridUrl.isEmpty()) {
            // ── Chạy trên CI/CD → dùng Selenium Grid (RemoteWebDriver) ──
            options.addArguments("--headless=new");  // CI luôn headless
            driver = new RemoteWebDriver(new URL(gridUrl), options);
            System.out.println("🤖 CI Mode: Kết nối Selenium Grid tại " + gridUrl);
        } else {
            // ── Chạy local → dùng ChromeDriver thường ──
            if (Boolean.getBoolean("headless")) {
                options.addArguments("--headless=new");
            }
            driver = new ChromeDriver(options);
            System.out.println("💻 Local Mode: Dùng ChromeDriver");
        }

        driverThreadLocal.set(driver);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        // Đọc base URL từ System Property hoặc Env Var
        // CI/CD set BASE_URL=http://nop_web:80, local dùng localhost:8080
        String baseUrl = System.getProperty("baseUrl",
            System.getenv().getOrDefault("BASE_URL", "http://localhost:8080"));
        driver.get(baseUrl);
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
            driverThreadLocal.remove();
        }
    }

    public static void autoOpenAllureReport() {
        try {
            File resultsDir = new File("target/allure-results");
            if (!resultsDir.exists()) {
                resultsDir = new File("allure-results");
            }
            String resultsPath = resultsDir.getAbsolutePath();

            File reportDir = new File("allure-report");
            String reportPath = reportDir.getAbsolutePath();
            String indexPath = reportPath + File.separator + "index.html";

            System.out.println("\n=======================================================");
            System.out.println("📊 ALLURE RESULTS PATH: " + resultsPath);
            System.out.println("📊 ALLURE REPORT PATH:  file://" + indexPath);
            System.out.println("🚀 Tự động tạo và mở Allure Report trên trình duyệt...");
            System.out.println("=======================================================\n");

            // 1. Tạo thư mục báo cáo HTML tĩnh (allure-report)
            new ProcessBuilder("allure", "generate", resultsPath, "-o", reportPath, "--clean").start().waitFor();
            System.out.println("✅ Đã tạo xong Allure Report tĩnh tại: file://" + indexPath);
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi tự động mở Allure Report: " + e.getMessage());
        }
    }
}
