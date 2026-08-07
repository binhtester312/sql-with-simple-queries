package com.nopcommerce.listeners;

import com.nopcommerce.tests.BaseTest;
import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class AllureListener implements ITestListener {

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
