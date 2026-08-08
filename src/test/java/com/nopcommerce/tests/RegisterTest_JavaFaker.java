package com.nopcommerce.tests;

import com.nopcommerce.config.NopCommerceConfig;
import com.nopcommerce.database.DatabaseHelper;
import com.nopcommerce.pages.HomePage;
import com.nopcommerce.pages.RegisterPage;
import net.datafaker.Faker;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.sql.SQLException;

public class RegisterTest_JavaFaker extends BaseTest {

    @Test(invocationCount = 2)
    public void testUserRegisWithJavaFaker() throws SQLException {
        driver.manage().deleteAllCookies();
        driver.get(NopCommerceConfig.BASE_URL);

        Faker faker = new Faker();
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        String email = faker.internet().emailAddress();
        String password = "Test@123456!";
        String gender = faker.options().option("male", "female");

        HomePage homePage = new HomePage(driver);
        RegisterPage registerPage = homePage.clickRegister();

        registerPage.registerNewUser(gender, firstName, lastName, email, password);

        // UI verification
        Assert.assertEquals(registerPage.getRegisSuccessMess(), "Your registration completed");

        // Database verification
        Assert.assertTrue(DatabaseHelper.isCustomerExist(email));
    }
}
