package com.nopcommerce.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.WebDriver;

import static com.nopcommerce.interfaceUI.LoginPageUI.*;

public class LoginPage extends BasePage {

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    @Step("Nhập Email đăng nhập: {email}")
    public void enterEmail(String email) {
        sendKeys(EMAIL_INPUT, email);
    }

    @Step("Nhập Mật khẩu: {password}")
    public void enterPassword(String password) {
        sendKeys(PASSWORD_INPUT, password);
    }

    @Step("Click nút Đăng nhập (Login)")
    public void clickLoginButton() {
        click(LOGIN_BUTTON);
    }

    @Step("Lấy thông báo lỗi hiển thị trên giao diện")
    public String getErrorMessage() {
        if (isDisplayed(EMAIL_INLINE_ERROR)) {
            return getText(EMAIL_INLINE_ERROR);
        }
        return getText(SUMMARY_ERROR_DETAIL);
    }
}
