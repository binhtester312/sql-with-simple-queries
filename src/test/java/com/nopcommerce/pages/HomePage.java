package com.nopcommerce.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.WebDriver;

import static com.nopcommerce.interfaceUI.HomePageUI.*;

public class HomePage extends BasePage {

    public HomePage(WebDriver driver) {
        super(driver);
    }

    @Step("Click liên kết Đăng ký (Register) trên Header")
    public RegisterPage clickRegister() {
        click(REGISTER_LINK);
        return new RegisterPage(driver);
    }
}
