package com.nopcommerce.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.WebDriver;

import static com.nopcommerce.interfaceUI.RegisterPageUI.*;

public class RegisterPage extends BasePage {

    public RegisterPage(WebDriver driver) {
        super(driver);
    }

    @Step("Chọn giới tính: {gender}")
    public void selectGender(String gender) {
        if (gender.equalsIgnoreCase("male")) {
            click(GENDER_MALE_RADIO);
        } else if (gender.equalsIgnoreCase("female")) {
            click(GENDER_FEMALE_RADIO);
        }
    }

    @Step("Nhập First Name: {firstName}")
    public void enterFirstName(String firstName) {
        sendKeys(FIRST_NAME_INPUT, firstName);
    }

    @Step("Nhập Last Name: {lastName}")
    public void enterLastName(String lastName) {
        sendKeys(LAST_NAME_INPUT, lastName);
    }

    @Step("Nhập Email: {email}")
    public void enterEmail(String email) {
        sendKeys(EMAIL_INPUT, email);
    }

    @Step("Nhập Mật khẩu: {password}")
    public void enterPassword(String password) {
        sendKeys(PASSWORD_INPUT, password);
    }

    @Step("Nhập Xác nhận Mật khẩu: {confirmPassword}")
    public void enterConfirmPassword(String confirmPassword) {
        sendKeys(CONFIRM_PASSWORD_INPUT, confirmPassword);
    }

    @Step("Click nút Đăng ký (Register)")
    public void clickRegisterButton() {
        click(REGISTER_BUTTON);
    }

    @Step("Điền thông tin và thực hiện Đăng ký tài khoản: {email}")
    public void registerNewUser(String gender, String firstName, String lastName, String email, String password) {
        selectGender(gender);
        enterFirstName(firstName);
        enterLastName(lastName);
        enterEmail(email);
        enterPassword(password);
        enterConfirmPassword(password);
        clickRegisterButton();
    }

    @Step("Lấy thông báo đăng ký thành công")
    public String getRegisSuccessMess() {
        return getText(SUCCESS_MESSAGE);
    }
}
