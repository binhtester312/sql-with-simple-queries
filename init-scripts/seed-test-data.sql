-- ============================================================
-- NopCommerce Seed Test Data Script
-- Dùng để bơm dữ liệu mẫu thực tế vào NopCommerceDB
-- Phục vụ cho Manual DB Testing trong sql_db_testing_guide.md
-- ============================================================

USE NopCommerceDB;
GO

BEGIN TRANSACTION;

PRINT '🚀 Dang xoa du lieu cu va khoi tao du lieu seed...';

-- 1. Unlink FK Address trước khi xóa
UPDATE Customer SET BillingAddress_Id = NULL, ShippingAddress_Id = NULL;

-- Xóa dữ liệu cũ của các bảng liên quan
DELETE FROM OrderItem;
DELETE FROM [Order];
DELETE FROM ShoppingCartItem;
DELETE FROM Product_Category_Mapping;
DELETE FROM Product;
DELETE FROM Category;
DELETE FROM Customer_CustomerRole_Mapping WHERE Customer_Id > 1;
DELETE FROM Customer WHERE Id > 1;
DELETE FROM Address;

-- Reset Identity cho các bảng
DBCC CHECKIDENT ('Product', RESEED, 0);
DBCC CHECKIDENT ('Category', RESEED, 0);
DBCC CHECKIDENT ('[Order]', RESEED, 0);
DBCC CHECKIDENT ('OrderItem', RESEED, 0);
DBCC CHECKIDENT ('ShoppingCartItem', RESEED, 0);
DBCC CHECKIDENT ('Address', RESEED, 0);
DBCC CHECKIDENT ('Product_Category_Mapping', RESEED, 0);

PRINT '✅ Da xoa xong du lieu cu.';

-- 2. Tạo Address
INSERT INTO Address (FirstName, LastName, Email, Company, CountryId, StateProvinceId, City, Address1, ZipPostalCode, PhoneNumber, CreatedOnUtc)
VALUES 
('Admin', 'User', 'admin@yourstore.com', 'NopCommerce Ltd', 242, NULL, 'Ha Noi', '123 Main St', '100000', '0901234567', GETUTCDATE()),
('Test', 'User', 'testuser@example.com', 'Testing Co', 242, NULL, 'Da Nang', '456 Le Duan', '500000', '0912345678', GETUTCDATE()),
('John', 'Doe', 'john.doe@example.com', 'Global Tech', 237, NULL, 'New York', '789 5th Ave', '10001', '+12125550199', GETUTCDATE()),
('Jane', 'Smith', 'jane.smith@example.com', 'Smith Corp', 237, NULL, 'San Francisco', '101 Market St', '94105', '+14155550123', GETUTCDATE());

DECLARE @AddrAdmin INT = 1;
DECLARE @AddrTest INT = 2;
DECLARE @AddrJohn INT = 3;
DECLARE @AddrJane INT = 4;

-- Cập nhật địa chỉ cho admin@yourstore.com
UPDATE Customer 
SET BillingAddress_Id = @AddrAdmin, ShippingAddress_Id = @AddrAdmin
WHERE Email = 'admin@yourstore.com';

-- 3. Tạo Thêm Customer Test
INSERT INTO Customer 
(
    CustomerGuid, Email, Username, Active, Deleted, IsSystemAccount, CreatedOnUtc, LastActivityDateUtc, 
    BillingAddress_Id, ShippingAddress_Id, FailedLoginAttempts, RequireReLogin, MustChangePassword, 
    CountryId, StateProvinceId, RegisteredInStoreId, VatNumberStatusId, IsTaxExempt, AffiliateId, VendorId, HasShoppingCartItems
)
VALUES 
(NEWID(), 'testuser@example.com', 'testuser@example.com', 1, 0, 0, GETUTCDATE(), GETUTCDATE(), @AddrTest, @AddrTest, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
(NEWID(), 'john.doe@example.com', 'john.doe@example.com', 1, 0, 0, GETUTCDATE(), GETUTCDATE(), @AddrJohn, @AddrJohn, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
(NEWID(), 'jane.smith@example.com', 'jane.smith@example.com', 1, 0, 0, GETUTCDATE(), GETUTCDATE(), @AddrJane, @AddrJane, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
(NEWID(), 'guest@example.com', 'guest@example.com', 1, 0, 0, GETUTCDATE(), GETUTCDATE(), NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

DECLARE @CustAdmin INT = (SELECT Id FROM Customer WHERE Email = 'admin@yourstore.com');
DECLARE @CustTest INT = (SELECT Id FROM Customer WHERE Email = 'testuser@example.com');
DECLARE @CustJohn INT = (SELECT Id FROM Customer WHERE Email = 'john.doe@example.com');
DECLARE @CustJane INT = (SELECT Id FROM Customer WHERE Email = 'jane.smith@example.com');

-- Gán Role (CustomerRole_Id = 3 là Registered)
INSERT INTO Customer_CustomerRole_Mapping (Customer_Id, CustomerRole_Id)
VALUES 
(@CustTest, 3),
(@CustJohn, 3),
(@CustJane, 3);

-- 4. Tạo Category
INSERT INTO Category (Name, Description, CategoryTemplateId, ParentCategoryId, PageSize, AllowCustomersToSelectPageSize, ShowOnHomepage, Published, Deleted, DisplayOrder, CreatedOnUtc, UpdatedOnUtc, RestrictFromVendors, PictureId, SubjectToAcl, LimitedToStores, PriceRangeFiltering, ManuallyPriceRange, PriceFrom, PriceTo)
VALUES 
('Computers', 'Computer hardware and accessories', 1, 0, 6, 1, 1, 1, 0, 1, GETUTCDATE(), GETUTCDATE(), 0, 0, 0, 0, 1, 0, 0.00, 0.00),
('Electronics', 'Consumer electronics & gadgets', 1, 0, 6, 1, 1, 1, 0, 2, GETUTCDATE(), GETUTCDATE(), 0, 0, 0, 0, 1, 0, 0.00, 0.00),
('Apparel', 'Clothing and shoes', 1, 0, 6, 1, 1, 1, 0, 3, GETUTCDATE(), GETUTCDATE(), 0, 0, 0, 0, 1, 0, 0.00, 0.00),
('Cell phones', 'Smartphones and accessories', 1, 0, 6, 1, 1, 1, 0, 4, GETUTCDATE(), GETUTCDATE(), 0, 0, 0, 0, 1, 0, 0.00, 0.00);

DECLARE @CatComputers INT = (SELECT Id FROM Category WHERE Name = 'Computers');
DECLARE @CatElectronics INT = (SELECT Id FROM Category WHERE Name = 'Electronics');
DECLARE @CatApparel INT = (SELECT Id FROM Category WHERE Name = 'Apparel');
DECLARE @CatCellPhones INT = (SELECT Id FROM Category WHERE Name = 'Cell phones');

-- 5. Tạo Product
INSERT INTO Product 
(
    ProductTypeId, ParentGroupedProductId, VisibleIndividually,
    Name, ShortDescription, FullDescription, AdminComment, ProductTemplateId, VendorId, ShowOnHomepage, 
    MetaKeywords, MetaDescription, MetaTitle, AllowCustomerReviews, ApprovedRatingSum, NotApprovedRatingSum, 
    ApprovedTotalReviews, NotApprovedTotalReviews, SubjectToAcl, LimitedToStores, Sku, ManufacturerPartNumber, Gtin, 
    IsGiftCard, GiftCardTypeId, OverriddenGiftCardAmount, RequireOtherProducts, RequiredProductIds, AutomaticallyAddRequiredProducts, 
    IsDownload, DownloadId, UnlimitedDownloads, MaxNumberOfDownloads, DownloadExpirationDays, DownloadActivationTypeId, 
    HasSampleDownload, SampleDownloadId, HasUserAgreement, UserAgreementText, IsRecurring, RecurringCycleLength, 
    RecurringCyclePeriodId, RecurringTotalCycles, IsRental, RentalPriceLength, RentalPricePeriodId, IsShipEnabled, 
    IsFreeShipping, ShipSeparately, AdditionalShippingCharge, DeliveryDateId, IsTaxExempt, TaxCategoryId, 
    ManageInventoryMethodId, ProductAvailabilityRangeId, UseMultipleWarehouses, WarehouseId, StockQuantity, 
    DisplayStockAvailability, DisplayStockQuantity, MinStockQuantity, LowStockActivityId, NotifyAdminForQuantityBelow, 
    BackorderModeId, AllowBackInStockSubscriptions, OrderMinimumQuantity, OrderMaximumQuantity, 
    AllowAddingOnlyExistingAttributeCombinations, DisplayAttributeCombinationImagesOnly, NotReturnable, DisableBuyButton, 
    DisableWishlistButton, AvailableForPreOrder, PreOrderAvailabilityStartDateTimeUtc, CallForPrice, Price, OldPrice, 
    ProductCost, CustomerEntersPrice, MinimumCustomerEnteredPrice, MaximumCustomerEnteredPrice, BasepriceEnabled, 
    BasepriceAmount, BasepriceUnitId, BasepriceBaseAmount, BasepriceBaseUnitId, MarkAsNew, MarkAsNewStartDateTimeUtc, 
    MarkAsNewEndDateTimeUtc, Weight, Length, Width, Height, DisplayOrder, Published, Deleted, CreatedOnUtc, UpdatedOnUtc, 
    AgeVerification, MinimumAgeToPurchase
)
VALUES 
(1, 0, 1, 'Build your own computer', 'High performance desktop PC', 'Full custom workstation desktop computer', '', 1, 0, 1, '', '', '', 1, 0, 0, 0, 0, 0, 0, 'COMP_CUST', '', '', 0, 0, NULL, 0, '', 0, 0, 0, 1, 10, NULL, 1, 0, 0, 0, '', 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0.00, 0, 0, 0, 1, 0, 0, 0, 50, 1, 1, 1, 0, 1, 0, 0, 1, 10000, 0, 0, 0, 0, 0, 0, NULL, 0, 1200.00, 1300.00, 900.00, 0, 0.00, 0.00, 0, 0.00, 0, 0.00, 0, 0, NULL, NULL, 5.0, 40.0, 20.0, 45.0, 1, 1, 0, GETUTCDATE(), GETUTCDATE(), 0, 0),
(1, 0, 1, 'Desktop Computer Core i7', 'Powerful i7 desktop', 'Desktop computer suitable for office and light gaming', '', 1, 0, 1, '', '', '', 1, 0, 0, 0, 0, 0, 0, 'COMP_DT_i7', '', '', 0, 0, NULL, 0, '', 0, 0, 0, 1, 10, NULL, 1, 0, 0, 0, '', 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0.00, 0, 0, 0, 1, 0, 0, 0, 30, 1, 1, 1, 0, 1, 0, 0, 1, 10000, 0, 0, 0, 0, 0, 0, NULL, 0, 850.00, 900.00, 600.00, 0, 0.00, 0.00, 0, 0.00, 0, 0.00, 0, 0, NULL, NULL, 4.5, 35.0, 18.0, 40.0, 2, 1, 0, GETUTCDATE(), GETUTCDATE(), 0, 0),
(1, 0, 1, 'Laptop Slim 15 inch', 'Ultra portable laptop', 'Lightweight 15 inch laptop for business and work', '', 1, 0, 1, '', '', '', 1, 0, 0, 0, 0, 0, 0, 'LAP_SLIM_15', '', '', 0, 0, NULL, 0, '', 0, 0, 0, 1, 10, NULL, 1, 0, 0, 0, '', 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0.00, 0, 0, 0, 1, 0, 0, 0, 25, 1, 1, 1, 0, 1, 0, 0, 1, 10000, 0, 0, 0, 0, 0, 0, NULL, 0, 450.00, 500.00, 300.00, 0, 0.00, 0.00, 0, 0.00, 0, 0.00, 0, 0, NULL, NULL, 1.8, 35.0, 24.0, 2.0, 3, 1, 0, GETUTCDATE(), GETUTCDATE(), 0, 0),
(1, 0, 1, 'Gaming Laptop Pro', 'Extreme gaming laptop', 'High frame rate gaming laptop with RTX GPU', '', 1, 0, 1, '', '', '', 1, 0, 0, 0, 0, 0, 0, 'LAP_GAMING', '', '', 0, 0, NULL, 0, '', 0, 0, 0, 1, 10, NULL, 1, 0, 0, 0, '', 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0.00, 0, 0, 0, 1, 0, 0, 0, 15, 1, 1, 1, 0, 1, 0, 0, 1, 10000, 0, 0, 0, 0, 0, 0, NULL, 0, 1350.00, 1500.00, 1000.00, 0, 0.00, 0.00, 0, 0.00, 0, 0.00, 0, 0, NULL, NULL, 2.5, 38.0, 26.0, 2.5, 4, 1, 0, GETUTCDATE(), GETUTCDATE(), 0, 0),
(1, 0, 1, 'Ultra Laptop 13 inch', 'Compact 13 inch notebook', 'Ultra portable notebook with long battery life', '', 1, 0, 1, '', '', '', 1, 0, 0, 0, 0, 0, 0, 'LAP_ULTRA_13', '', '', 0, 0, NULL, 0, '', 0, 0, 0, 1, 10, NULL, 1, 0, 0, 0, '', 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0.00, 0, 0, 0, 1, 0, 0, 0, 40, 1, 1, 1, 0, 1, 0, 0, 1, 10000, 0, 0, 0, 0, 0, 0, NULL, 0, 350.00, 400.00, 220.00, 0, 0.00, 0.00, 0, 0.00, 0, 0.00, 0, 0, NULL, NULL, 1.2, 30.0, 21.0, 1.5, 5, 1, 0, GETUTCDATE(), GETUTCDATE(), 0, 0),
(1, 0, 1, 'Smartphone Pro Max', 'Flagship mobile phone', 'OLED Display 120Hz flagship smartphone', '', 1, 0, 1, '', '', '', 1, 0, 0, 0, 0, 0, 0, 'PHONE_PRO', '', '', 0, 0, NULL, 0, '', 0, 0, 0, 1, 10, NULL, 1, 0, 0, 0, '', 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0.00, 0, 0, 0, 1, 0, 0, 0, 100, 1, 1, 1, 0, 1, 0, 0, 1, 10000, 0, 0, 0, 0, 0, 0, NULL, 0, 999.00, 1099.00, 750.00, 0, 0.00, 0.00, 0, 0.00, 0, 0.00, 0, 0, NULL, NULL, 0.2, 16.0, 7.5, 0.8, 6, 1, 0, GETUTCDATE(), GETUTCDATE(), 0, 0),
(1, 0, 1, 'Budget Smart Phone', 'Affordable smartphone', 'Entry level Android smartphone with dual SIM', '', 1, 0, 1, '', '', '', 1, 0, 0, 0, 0, 0, 0, 'PHONE_BUDGET', '', '', 0, 0, NULL, 0, '', 0, 0, 0, 1, 10, NULL, 1, 0, 0, 0, '', 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0.00, 0, 0, 0, 1, 0, 0, 0, 60, 1, 1, 1, 0, 1, 0, 0, 1, 10000, 0, 0, 0, 0, 0, 0, NULL, 0, 150.00, 180.00, 90.00, 0, 0.00, 0.00, 0, 0.00, 0, 0.00, 0, 0, NULL, NULL, 0.18, 15.0, 7.2, 0.8, 7, 1, 0, GETUTCDATE(), GETUTCDATE(), 0, 0),
(1, 0, 1, 'Wireless Headphones', 'Noise cancelling headphones', 'Over-ear Bluetooth headphones with active noise cancelling', '', 1, 0, 1, '', '', '', 1, 0, 0, 0, 0, 0, 0, 'ACC_HEADPHONE', '', '', 0, 0, NULL, 0, '', 0, 0, 0, 1, 10, NULL, 1, 0, 0, 0, '', 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0.00, 0, 0, 0, 1, 0, 0, 0, 80, 1, 1, 1, 0, 1, 0, 0, 1, 10000, 0, 0, 0, 0, 0, 0, NULL, 0, 120.00, 150.00, 70.00, 0, 0.00, 0.00, 0, 0.00, 0, 0.00, 0, 0, NULL, NULL, 0.25, 20.0, 18.0, 8.0, 8, 1, 0, GETUTCDATE(), GETUTCDATE(), 0, 0),
(1, 0, 1, 'Mechanical Gaming Keyboard', 'RGB Mechanical Keyboard', 'Tactile RGB backlighting mechanical keyboard', '', 1, 0, 1, '', '', '', 1, 0, 0, 0, 0, 0, 0, 'ACC_KEYBOARD', '', '', 0, 0, NULL, 0, '', 0, 0, 0, 1, 10, NULL, 1, 0, 0, 0, '', 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0.00, 0, 0, 0, 1, 0, 0, 0, 100, 1, 1, 1, 0, 1, 0, 0, 1, 10000, 0, 0, 0, 0, 0, 0, NULL, 0, 85.00, 100.00, 45.00, 0, 0.00, 0.00, 0, 0.00, 0, 0.00, 0, 0, NULL, NULL, 1.1, 44.0, 14.0, 3.5, 9, 1, 0, GETUTCDATE(), GETUTCDATE(), 0, 0),
(1, 0, 1, 'Ergonomic Wireless Mouse', 'Silent wireless mouse', 'Ergonomic design wireless mouse for daily work', '', 1, 0, 1, '', '', '', 1, 0, 0, 0, 0, 0, 0, 'ACC_MOUSE', '', '', 0, 0, NULL, 0, '', 0, 0, 0, 1, 10, NULL, 1, 0, 0, 0, '', 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0.00, 0, 0, 0, 1, 0, 0, 0, 120, 1, 1, 1, 0, 1, 0, 0, 1, 10000, 0, 0, 0, 0, 0, 0, NULL, 0, 45.00, 60.00, 20.00, 0, 0.00, 0.00, 0, 0.00, 0, 0.00, 0, 0, NULL, NULL, 0.1, 12.0, 7.0, 4.0, 10, 1, 0, GETUTCDATE(), GETUTCDATE(), 0, 0),
(1, 0, 1, 'Nike Running Shoes', 'Sports running sneakers', 'Comfortable Nike running sneakers for sports & daily wear', '', 1, 0, 1, '', '', '', 1, 0, 0, 0, 0, 0, 0, 'APP_NIKE_SHOE', '', '', 0, 0, NULL, 0, '', 0, 0, 0, 1, 10, NULL, 1, 0, 0, 0, '', 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0.00, 0, 0, 0, 1, 0, 0, 0, 50, 1, 1, 1, 0, 1, 0, 0, 1, 10000, 0, 0, 0, 0, 0, 0, NULL, 0, 110.00, 130.00, 65.00, 0, 0.00, 0.00, 0, 0.00, 0, 0.00, 0, 0, NULL, NULL, 0.8, 30.0, 20.0, 12.0, 11, 1, 0, GETUTCDATE(), GETUTCDATE(), 0, 0),
(1, 0, 1, 'Classic Cotton T-Shirt', '100% Cotton T-Shirt', 'Breathable cotton t-shirt unisex style', '', 1, 0, 1, '', '', '', 1, 0, 0, 0, 0, 0, 0, 'APP_TSHIRT', '', '', 0, 0, NULL, 0, '', 0, 0, 0, 1, 10, NULL, 1, 0, 0, 0, '', 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0.00, 0, 0, 0, 1, 0, 0, 0, 200, 1, 1, 1, 0, 1, 0, 0, 1, 10000, 0, 0, 0, 0, 0, 0, NULL, 0, 25.00, 30.00, 10.00, 0, 0.00, 0.00, 0, 0.00, 0, 0.00, 0, 0, NULL, NULL, 0.2, 25.0, 20.0, 1.0, 12, 1, 0, GETUTCDATE(), GETUTCDATE(), 0, 0);

DECLARE @P_BuildComp INT = (SELECT Id FROM Product WHERE Sku = 'COMP_CUST');
DECLARE @P_DesktopI7 INT = (SELECT Id FROM Product WHERE Sku = 'COMP_DT_i7');
DECLARE @P_Laptop15 INT = (SELECT Id FROM Product WHERE Sku = 'LAP_SLIM_15');
DECLARE @P_LaptopGam INT = (SELECT Id FROM Product WHERE Sku = 'LAP_GAMING');
DECLARE @P_Laptop13 INT = (SELECT Id FROM Product WHERE Sku = 'LAP_ULTRA_13');
DECLARE @P_PhonePro INT = (SELECT Id FROM Product WHERE Sku = 'PHONE_PRO');
DECLARE @P_PhoneBud INT = (SELECT Id FROM Product WHERE Sku = 'PHONE_BUDGET');
DECLARE @P_Headphone INT = (SELECT Id FROM Product WHERE Sku = 'ACC_HEADPHONE');
DECLARE @P_Keyboard INT = (SELECT Id FROM Product WHERE Sku = 'ACC_KEYBOARD');
DECLARE @P_Mouse INT = (SELECT Id FROM Product WHERE Sku = 'ACC_MOUSE');
DECLARE @P_NikeShoe INT = (SELECT Id FROM Product WHERE Sku = 'APP_NIKE_SHOE');
DECLARE @P_TShirt INT = (SELECT Id FROM Product WHERE Sku = 'APP_TSHIRT');

-- 6. Product_Category_Mapping
INSERT INTO Product_Category_Mapping (CategoryId, ProductId, IsFeaturedProduct, DisplayOrder)
VALUES 
(@CatComputers, @P_BuildComp, 1, 1),
(@CatComputers, @P_DesktopI7, 0, 2),
(@CatComputers, @P_Laptop15, 1, 3),
(@CatComputers, @P_LaptopGam, 0, 4),
(@CatComputers, @P_Laptop13, 0, 5),
(@CatCellPhones, @P_PhonePro, 1, 1),
(@CatCellPhones, @P_PhoneBud, 0, 2),
(@CatElectronics, @P_Headphone, 1, 1),
(@CatElectronics, @P_Keyboard, 0, 2),
(@CatElectronics, @P_Mouse, 0, 3),
(@CatApparel, @P_NikeShoe, 1, 1),
(@CatApparel, @P_TShirt, 0, 2);

-- 7. ShoppingCartItem
-- ShoppingCartTypeId = 1 (Cart), 2 (Wishlist)
INSERT INTO ShoppingCartItem (CustomerId, ProductId, CustomWishlistId, StoreId, ShoppingCartTypeId, CustomerEnteredPrice, Quantity, CreatedOnUtc, UpdatedOnUtc)
VALUES 
(@CustAdmin, @P_Laptop15, NULL, 1, 1, 0.00, 2, GETUTCDATE(), GETUTCDATE()),
(@CustAdmin, @P_Headphone, NULL, 1, 1, 0.00, 1, GETUTCDATE(), GETUTCDATE()),
(@CustAdmin, @P_PhonePro, NULL, 1, 2, 0.00, 1, GETUTCDATE(), GETUTCDATE()),
(@CustTest, @P_NikeShoe, NULL, 1, 1, 0.00, 1, GETUTCDATE(), GETUTCDATE()),
(@CustTest, @P_Keyboard, NULL, 1, 1, 0.00, 1, GETUTCDATE(), GETUTCDATE()),
(@CustJohn, @P_Laptop13, NULL, 1, 2, 0.00, 1, GETUTCDATE(), GETUTCDATE());

-- 8. Order & OrderItem
-- Order 1: Admin - Completed & Paid
INSERT INTO [Order]
(CustomOrderNumber, OrderGuid, CustomerId, StoreId, BillingAddressId, ShippingAddressId, PickupAddressId, PickupInStore, OrderStatusId, ShippingStatusId, PaymentStatusId, PaymentMethodSystemName, CustomerCurrencyCode, CurrencyRate, CustomerTaxDisplayTypeId, OrderSubtotalInclTax, OrderSubtotalExclTax, OrderSubTotalDiscountInclTax, OrderSubTotalDiscountExclTax, OrderShippingInclTax, OrderShippingExclTax, PaymentMethodAdditionalFeeInclTax, PaymentMethodAdditionalFeeExclTax, TaxRates, OrderTax, OrderDiscount, OrderTotal, RefundedAmount, RewardPointsHistoryEntryId, CustomerLanguageId, AffiliateId, AllowStoringCreditCardNumber, Deleted, CreatedOnUtc)
VALUES 
('1001', NEWID(), @CustAdmin, 1, @AddrAdmin, @AddrAdmin, NULL, 0, 30, 30, 30, 'Payments.Manual', 'USD', 1.0, 1, 1020.00, 1020.00, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, '', 0.0, 0.0, 1020.00, 0.0, NULL, 1, 0, 0, 0, DATEADD(DAY, -5, GETUTCDATE()));

DECLARE @Ord1 INT = SCOPE_IDENTITY();

INSERT INTO OrderItem 
(OrderItemGuid, OrderId, ProductId, Quantity, UnitPriceInclTax, UnitPriceExclTax, PriceInclTax, PriceExclTax, DiscountAmountInclTax, DiscountAmountExclTax, OriginalProductCost, DownloadCount, IsDownloadActivated, LicenseDownloadId)
VALUES 
(NEWID(), @Ord1, @P_Laptop15, 2, 450.00, 450.00, 900.00, 900.00, 0.0, 0.0, 300.00, 0, 0, NULL),
(NEWID(), @Ord1, @P_Headphone, 1, 120.00, 120.00, 120.00, 120.00, 0.0, 0.0, 70.00, 0, 0, NULL);

-- Order 2: Admin - Pending Order & Payment
INSERT INTO [Order]
(CustomOrderNumber, OrderGuid, CustomerId, StoreId, BillingAddressId, ShippingAddressId, PickupAddressId, PickupInStore, OrderStatusId, ShippingStatusId, PaymentStatusId, PaymentMethodSystemName, CustomerCurrencyCode, CurrencyRate, CustomerTaxDisplayTypeId, OrderSubtotalInclTax, OrderSubtotalExclTax, OrderSubTotalDiscountInclTax, OrderSubTotalDiscountExclTax, OrderShippingInclTax, OrderShippingExclTax, PaymentMethodAdditionalFeeInclTax, PaymentMethodAdditionalFeeExclTax, TaxRates, OrderTax, OrderDiscount, OrderTotal, RefundedAmount, RewardPointsHistoryEntryId, CustomerLanguageId, AffiliateId, AllowStoringCreditCardNumber, Deleted, CreatedOnUtc)
VALUES 
('1002', NEWID(), @CustAdmin, 1, @AddrAdmin, @AddrAdmin, NULL, 0, 10, 10, 10, 'Payments.CheckMoneyOrder', 'USD', 1.0, 1, 850.00, 850.00, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, '', 0.0, 0.0, 850.00, 0.0, NULL, 1, 0, 0, 0, DATEADD(DAY, -2, GETUTCDATE()));

DECLARE @Ord2 INT = SCOPE_IDENTITY();

INSERT INTO OrderItem 
(OrderItemGuid, OrderId, ProductId, Quantity, UnitPriceInclTax, UnitPriceExclTax, PriceInclTax, PriceExclTax, DiscountAmountInclTax, DiscountAmountExclTax, OriginalProductCost, DownloadCount, IsDownloadActivated, LicenseDownloadId)
VALUES 
(NEWID(), @Ord2, @P_DesktopI7, 1, 850.00, 850.00, 850.00, 850.00, 0.0, 0.0, 600.00, 0, 0, NULL);

-- Order 3: Test User - Processing & Paid
INSERT INTO [Order]
(CustomOrderNumber, OrderGuid, CustomerId, StoreId, BillingAddressId, ShippingAddressId, PickupAddressId, PickupInStore, OrderStatusId, ShippingStatusId, PaymentStatusId, PaymentMethodSystemName, CustomerCurrencyCode, CurrencyRate, CustomerTaxDisplayTypeId, OrderSubtotalInclTax, OrderSubtotalExclTax, OrderSubTotalDiscountInclTax, OrderSubTotalDiscountExclTax, OrderShippingInclTax, OrderShippingExclTax, PaymentMethodAdditionalFeeInclTax, PaymentMethodAdditionalFeeExclTax, TaxRates, OrderTax, OrderDiscount, OrderTotal, RefundedAmount, RewardPointsHistoryEntryId, CustomerLanguageId, AffiliateId, AllowStoringCreditCardNumber, Deleted, CreatedOnUtc)
VALUES 
('1003', NEWID(), @CustTest, 1, @AddrTest, @AddrTest, NULL, 0, 20, 20, 30, 'Payments.Manual', 'USD', 1.0, 1, 1200.00, 1200.00, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, '', 0.0, 0.0, 1200.00, 0.0, NULL, 1, 0, 0, 0, DATEADD(DAY, -1, GETUTCDATE()));

DECLARE @Ord3 INT = SCOPE_IDENTITY();

INSERT INTO OrderItem 
(OrderItemGuid, OrderId, ProductId, Quantity, UnitPriceInclTax, UnitPriceExclTax, PriceInclTax, PriceExclTax, DiscountAmountInclTax, DiscountAmountExclTax, OriginalProductCost, DownloadCount, IsDownloadActivated, LicenseDownloadId)
VALUES 
(NEWID(), @Ord3, @P_BuildComp, 1, 1200.00, 1200.00, 1200.00, 1200.00, 0.0, 0.0, 900.00, 0, 0, NULL);

-- Order 4: John Doe - Cancelled & Refunded
INSERT INTO [Order]
(CustomOrderNumber, OrderGuid, CustomerId, StoreId, BillingAddressId, ShippingAddressId, PickupAddressId, PickupInStore, OrderStatusId, ShippingStatusId, PaymentStatusId, PaymentMethodSystemName, CustomerCurrencyCode, CurrencyRate, CustomerTaxDisplayTypeId, OrderSubtotalInclTax, OrderSubtotalExclTax, OrderSubTotalDiscountInclTax, OrderSubTotalDiscountExclTax, OrderShippingInclTax, OrderShippingExclTax, PaymentMethodAdditionalFeeInclTax, PaymentMethodAdditionalFeeExclTax, TaxRates, OrderTax, OrderDiscount, OrderTotal, RefundedAmount, RewardPointsHistoryEntryId, CustomerLanguageId, AffiliateId, AllowStoringCreditCardNumber, Deleted, CreatedOnUtc)
VALUES 
('1004', NEWID(), @CustJohn, 1, @AddrJohn, @AddrJohn, NULL, 0, 40, 40, 40, 'Payments.PayPalStandard', 'USD', 1.0, 1, 150.00, 150.00, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, '', 0.0, 0.0, 150.00, 150.00, NULL, 1, 0, 0, 0, DATEADD(DAY, -10, GETUTCDATE()));

DECLARE @Ord4 INT = SCOPE_IDENTITY();

INSERT INTO OrderItem 
(OrderItemGuid, OrderId, ProductId, Quantity, UnitPriceInclTax, UnitPriceExclTax, PriceInclTax, PriceExclTax, DiscountAmountInclTax, DiscountAmountExclTax, OriginalProductCost, DownloadCount, IsDownloadActivated, LicenseDownloadId)
VALUES 
(NEWID(), @Ord4, @P_PhoneBud, 1, 150.00, 150.00, 150.00, 150.00, 0.0, 0.0, 90.00, 0, 0, NULL);

COMMIT TRANSACTION;

PRINT '🎉 SEED DATA SUCCESSFUL! Ban da co day du du lieu de thuc hanh Manual DB Testing!';
GO
