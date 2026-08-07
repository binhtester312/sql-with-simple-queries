package com.nopcommerce.tests;

import com.nopcommerce.listeners.AllureListener;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;

import java.io.File;
import java.time.Duration;

@Listeners(AllureListener.class)
public class BaseTest {
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    protected WebDriver driver;

    public static WebDriver getDriver() {
        return driverThreadLocal.get();
    }

    @BeforeClass
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        if (Boolean.getBoolean("headless")) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver = new ChromeDriver(options);
        driverThreadLocal.set(driver);

        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.get("http://localhost:8080");
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
