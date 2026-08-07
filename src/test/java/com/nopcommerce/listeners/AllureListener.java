package com.nopcommerce.listeners;

import com.nopcommerce.tests.BaseTest;
import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;

public class AllureListener implements ITestListener {

    private static boolean isCleaned = false;

    @Override
    public void onStart(ITestContext context) {
        deletePreviousAllureResults();
    }

    private synchronized void deletePreviousAllureResults() {
        if (!isCleaned) {
            cleanDirectory(new File("target/allure-results"));
            cleanDirectory(new File("allure-results"));
            isCleaned = true;
        }
    }

    private void cleanDirectory(File dir) {
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        file.delete();
                    }
                }
            }
        }
    }

    @Attachment(value = "Screenshot - {0}", type = "image/png")
    public byte[] saveScreenshot(String name, WebDriver driver) {
        if (driver != null) {
            try {
                return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            } catch (Exception e) {
                System.err.println("❌ Lỗi chụp ảnh màn hình: " + e.getMessage());
            }
        }
        return new byte[0];
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        WebDriver driver = BaseTest.getDriver();
        if (driver != null) {
            saveScreenshot("SUCCESS: " + result.getName(), driver);
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        WebDriver driver = BaseTest.getDriver();
        if (driver != null) {
            saveScreenshot("FAILED: " + result.getName(), driver);
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        BaseTest.autoOpenAllureReport();
    }
}
