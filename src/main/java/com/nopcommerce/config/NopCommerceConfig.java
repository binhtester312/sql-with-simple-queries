package com.nopcommerce.config;

/**
 * Cấu hình kết nối cho automation testing với nopCommerce local
 */

public class NopCommerceConfig {

    // ── Web App ──────────────────────────────────────────
    /** URL của nopCommerce (ưu tiên System property, Env var, mặc định localhost:8080) */
    public static final String BASE_URL = System.getProperty("baseUrl",
            System.getenv().getOrDefault("BASE_URL", "http://localhost:8080"));

    /** URL Selenium Grid (remote browser) */
    public static final String SELENIUM_GRID_URL = System.getProperty("gridUrl",
            System.getenv().getOrDefault("SELENIUM_HUB_URL", "http://localhost:4444/wd/hub"));

    // ── Database (SQL Server) ─────────────────────────────
    /** JDBC connection string cho SQL Server */
    public static final String DB_URL = System.getProperty("dbUrl",
            System.getenv().getOrDefault("DB_URL",
                    "jdbc:sqlserver://localhost:1433;databaseName=NopCommerce;encrypt=false;trustServerCertificate=true"));

    public static final String DB_USERNAME = System.getProperty("dbUser",
            System.getenv().getOrDefault("DB_USER", "sa"));

    public static final String DB_PASSWORD = System.getProperty("dbPassword",
            System.getenv().getOrDefault("DB_PASSWORD", "Test@123456!"));

    // ── Admin Credentials ─────────────────────────────────
    public static final String ADMIN_EMAIL = "admin@yourstore.com";
    public static final String ADMIN_PASSWORD = "admin";

    // ── Test User ─────────────────────────────────────────
    public static final String TEST_USER_EMAIL = "testuser@example.com";
    public static final String TEST_USER_PASSWORD = "Test@1234!";
}
