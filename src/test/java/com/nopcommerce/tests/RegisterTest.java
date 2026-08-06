package com.nopcommerce.tests;

import com.nopcommerce.database.DatabaseHelper;
import com.nopcommerce.pages.HomePage;
import com.nopcommerce.pages.RegisterPage;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.sql.SQLException;

public class RegisterTest extends BaseTest {
    private String email;

    @Test
    public void testUserRegistrationAndDatabaseVerification() throws SQLException {
        // Generate random email to avoid duplication
        email = "testuser_" + System.currentTimeMillis() + "@example.com";
        String firstName = "John";
        String lastName = "Doe";
        String password = "Test@123456!";

        System.out.println("📝 Running register test with email: " + email);

        HomePage homePage = new HomePage(driver);
        RegisterPage registerPage = homePage.clickRegister();

        registerPage.registerNewUser("male", firstName, lastName, email, password);

        // UI Verification
        String successMsg = registerPage.getRegistrationSuccessMessage();
        System.out.println("💬 UI Success Message: " + successMsg);
        Assert.assertEquals(successMsg, "Your registration completed", "UI Success message does not match!");

        // Database Verification
        System.out.println("🔍 Verifying email in SQL Server database: " + email);
        boolean isExist = DatabaseHelper.isCustomerExist(email);
        Assert.assertTrue(isExist, "Customer was NOT found in the database!");
        System.out.println("✅ Customer verified successfully in the SQL Server database!");
    }

    @AfterMethod(alwaysRun = true)
    public void cleanUpData() {
        if (email != null) {
            try {
                DatabaseHelper.cleanupTestCustomer(email);
            } catch (SQLException e) {
                System.err.println("⚠️ Failed to clean up database record: " + e.getMessage());
            }
        }
    }
}
