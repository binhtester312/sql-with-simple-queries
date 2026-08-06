package com.nopcommerce.pages;

import org.openqa.selenium.WebDriver;

import static com.nopcommerce.interfaceUI.HomePageUI.*;

public class HomePage extends BasePage {

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public RegisterPage clickRegister() {
        click(REGISTER_LINK);
        return new RegisterPage(driver);
    }
}
