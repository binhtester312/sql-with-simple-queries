package com.nopcommerce.pages;

import org.openqa.selenium.WebDriver;

import static com.nopcommerce.interfaceUI.RegisterPageUI.*;

public class RegisterPage extends BasePage {

    public RegisterPage(WebDriver driver) {
        super(driver);
    }

    public void selectGender(String gender) {
        if (gender.equalsIgnoreCase("male")) {
            click(GENDER_MALE_RADIO);
        } else if (gender.equalsIgnoreCase("female")) {
            click(GENDER_FEMALE_RADIO);
        }
    }

    public void enterFirstName(String firstName) {
        sendKeys(FIRST_NAME_INPUT, firstName);
    }

    public void enterLastName(String lastName) {
        sendKeys(LAST_NAME_INPUT, lastName);
    }

    public void enterEmail(String email) {
        sendKeys(EMAIL_INPUT, email);
    }

    public void enterPassword(String password) {
        sendKeys(PASSWORD_INPUT, password);
    }

    public void enterConfirmPassword(String confirmPassword) {
        sendKeys(CONFIRM_PASSWORD_INPUT, confirmPassword);
    }

    public void clickRegisterButton() {
        click(REGISTER_BUTTON);
    }

    public void registerNewUser(String gender, String firstName, String lastName, String email, String password) {
        selectGender(gender);
        enterFirstName(firstName);
        enterLastName(lastName);
        enterEmail(email);
        enterPassword(password);
        enterConfirmPassword(password);
        clickRegisterButton();
    }

    public String getRegistrationSuccessMessage() {
        return getText(SUCCESS_MESSAGE);
    }
}
