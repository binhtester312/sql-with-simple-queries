package com.nopcommerce.tests;

import com.nopcommerce.config.NopCommerceConfig;
import com.nopcommerce.database.DatabaseHelper;
import com.nopcommerce.pages.HomePage;
import com.nopcommerce.pages.RegisterPage;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.sql.SQLException;

public class RegisterTest_CurrentTimeMillis extends BaseTest {
    private String email;

    @Test
    public void testUserRegistrationAndDatabaseVerification() throws SQLException {
        driver.get(NopCommerceConfig.BASE_URL);
        // Generate random email
        email = "testuser_" + System.currentTimeMillis() + "@example.com";
        String firstName = "John";
        String lastName = "Doe";
        String password = "Test@123456!";

        HomePage homePage = new HomePage(driver);
        RegisterPage registerPage = homePage.clickRegister();

        registerPage.registerNewUser("male", firstName, lastName, email, password);

        // UI Verification
        Assert.assertEquals(registerPage.getRegisSuccessMess(), "Your registration completed");

        // Database Verification
        Assert.assertTrue(DatabaseHelper.isCustomerExist(email));
    }

}
