package com.nopcommerce.tests;

import com.nopcommerce.data.DataHelper;
import com.nopcommerce.data.LoginData;
import com.nopcommerce.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {

    private LoginPage loginPage;

    /**
     * Chạy trước MỖI lần @Test (mỗi case trong DataProvider).
     * Lý do dùng driver.get() thay vì refresh():
     * - refresh() chỉ an toàn nếu chắc chắn đang ở /login.
     * - driver.get() luôn đảm bảo về đúng trang /login dù test trước có redirect
     * hay không.
     */
    @BeforeMethod
    public void navigateToLoginPage() {
        driver.get("http://localhost:8080/login");
        loginPage = new LoginPage(driver);
    }

    @Test(dataProvider = "invalidLoginData", dataProviderClass = DataHelper.class)
    public void testLoginInvalidCases(LoginData loginData) {
        System.out.println("🧪 Đang kiểm thử kịch bản: " + loginData.getDescription());

        // 1. Nhập email & password từ file JSON vào giao diện
        loginPage.enterEmail(loginData.getEmail());
        loginPage.enterPassword(loginData.getPassword());
        loginPage.clickLoginButton();

        // 2. Kiểm tra thông báo lỗi có đúng như mong đợi không
        String actualError = loginPage.getErrorMessage();
        Assert.assertEquals(actualError, loginData.getExpectedErrorMessage());
    }
}