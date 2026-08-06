-- ============================================================
-- nopCommerce – SQL Server Init Script
-- Chạy sau khi nopCommerce tự tạo schema qua Installation Wizard
-- File này chứa các query kiểm tra và seed data test
-- ============================================================

-- Kiểm tra database đã được tạo chưa
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'NopCommerceDB')
BEGIN
    PRINT 'NopCommerceDB chưa được tạo. Hãy hoàn thành Installation Wizard trước.';
END
ELSE
BEGIN
    PRINT 'NopCommerceDB đã sẵn sàng!';
END

-- ============================================================
-- Sample queries dùng trong Automation Test (verify data)
-- ============================================================

-- 1. Kiểm tra customer đã được tạo sau khi Register
-- SELECT * FROM Customer WHERE Email = 'testuser@example.com';

-- 2. Kiểm tra order sau khi Checkout
-- SELECT TOP 5 * FROM [Order] ORDER BY CreatedOnUtc DESC;

-- 3. Kiểm tra sản phẩm trong giỏ hàng
-- SELECT * FROM ShoppingCartItem WHERE CustomerId = (
--     SELECT Id FROM Customer WHERE Email = 'testuser@example.com'
-- );

-- 4. Reset password test user (dùng khi cần fresh test)
-- UPDATE Customer SET Password = 'hashed_password' WHERE Email = 'testuser@example.com';

-- 5. Xóa test orders sau khi chạy test
-- DELETE FROM [Order] WHERE BillingAddressId IN (
--     SELECT Id FROM Address WHERE Email LIKE '%test%'
-- );
