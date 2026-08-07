package com.nopcommerce.database;

import com.nopcommerce.config.NopCommerceConfig;
import java.sql.*;

//Helper class để verify database sau khi automation test chạy

public class DatabaseHelper {

    private static Connection connection;

    // Mở kết nối đến SQL Server local

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(
                    NopCommerceConfig.DB_URL,
                    NopCommerceConfig.DB_USERNAME,
                    NopCommerceConfig.DB_PASSWORD);
        }
        return connection;
    }

    /**
     * Ktra customer đã tồn tại trong DB sau khi Register
     * 
     * @param email email của customer vừa đăng ký
     * @return true nếu tồn tại
     */
    public static boolean isCustomerExist(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Customer WHERE Email = ? AND Deleted = 0";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Lấy số order mới nhất của một customer
     * 
     * @param email email của customer
     * @return số lượng order
     */
    public static int getOrderCount(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM [Order] o " +
                "JOIN Customer c ON o.CustomerId = c.Id " +
                "WHERE c.Email = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Xóa test data sau khi test xong (dùng trong @AfterClass hoặc @AfterMethod)
     * 
     * @param email email của test user cần cleanup
     */
    public static void cleanupTestCustomer(String email) throws SQLException {
        // Xóa shopping cart
        String deleteCart = "DELETE FROM ShoppingCartItem WHERE CustomerId = " +
                "(SELECT Id FROM Customer WHERE Email = ?)";
        // Xóa customer
        String deleteCustomer = "DELETE FROM Customer WHERE Email = ? AND Email NOT LIKE '%admin%'";

        try (PreparedStatement ps1 = getConnection().prepareStatement(deleteCart);
                PreparedStatement ps2 = getConnection().prepareStatement(deleteCustomer)) {
            ps1.setString(1, email);
            ps1.executeUpdate();
            ps2.setString(1, email);
            ps2.executeUpdate();
        }
    }

    // Đóng kết nối
    public static void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
