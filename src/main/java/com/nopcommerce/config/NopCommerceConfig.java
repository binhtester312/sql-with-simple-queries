package com.nopcommerce.config;

/**
 * Cấu hình kết nối cho automation testing với nopCommerce local
 */

public class NopCommerceConfig {

        // ── Web App──────────────────────────────────
        /** URL của nopCommerce chạy local qua Docker */
        public static final String BASE_URL = "http://nop_web";

        /** URL Selenium Grid (remote browser) */
        public static final String SELENIUM_GRID_URL = "http://localhost:4444/wd/hub";

        // ── Database (SQL Server) ─────────────────────────────
        /** JDBC connection string cho SQL Server local */
        public static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=NopCommerceDB;" +
                        "encrypt=false;trustServerCertificate=true";

        public static final String DB_USERNAME = "sa";
        public static final String DB_PASSWORD = "Test@123456!";

        // ── Admin Credentials ─────────────────────────────────
        public static final String ADMIN_EMAIL = "admin@yourstore.com";
        public static final String ADMIN_PASSWORD = "admin";

        // ── Test User ─────────────────────────────────────────
        public static final String TEST_USER_EMAIL = "testuser@example.com";
        public static final String TEST_USER_PASSWORD = "Test@1234!";
}
