# HƯỚNG DẪN THỰC HÀNH SQL DATABASE TESTING CHO FRESHER TESTER (nopCommerce Local)

File này được tạo để bạn **tự thực hành truy vấn SQL bằng DBeaver** và phục vụ cho việc **Manual Database Testing** khi kiểm thử hệ thống nopCommerce local. Bạn có thể mở file này trên IDE/VS Code và **chỉnh sửa (edit) trực tiếp** để gõ câu lệnh SQL, lưu chú thích hoặc kết quả test của mình.
---

## 2. CÁC BẢNG NỀN TẢNG CẦN NẮM TRONG NOPCOMMERCE

| Tên Bảng | Ý Nghĩa / Mục Đích | Tên Cột Khóa Ngoại (Foreign Key) Cần Lưu Ý |
|---|---|---|
| `Customer` | thông tin tài khoản người dùng (Email, Username, sdt..| `BillingAddress_Id`, `ShippingAddress_Id` (Có dấu gạch dưới `_`) |
| `CustomerRole` | role hệ thống (`Administrators`, `Registered`, `Guests`, `Vendors`...) | `Id` |
| `Customer_CustomerRole_Mapping` | Bảng trung gian nối Customer với CustomerRole (Mối quan hệ N - N) | `Customer_Id`, `CustomerRole_Id` (Có dấu gạch dưới `_`) |
| `Product` | Danh mục sản phẩm (Name, SKU, Price, StockQuantity, Published, Deleted...) | `Id` |
| `Category` | Danh mục ngành hàng (Electronics, Apparel, Computers...) | `Id` |
| `Product_Category_Mapping` | Bảng trung gian gán sản phẩm vào danh mục | `ProductId`, `CategoryId` (Viết liền, KHÔNG gạch dưới) |
| `ShoppingCartItem` | Lưu danh sách sản phẩm nằm trong giỏ hàng hoặc Wishlist | `CustomerId`, `ProductId`, `ShoppingCartTypeId` |
| `Order` | Lưu thông tin đơn hàng đã đặt (`OrderTotal`, `OrderStatusId`, `PaymentStatusId`...) | `CustomerId`, `BillingAddressId`, `ShippingAddressId` |
| `OrderItem` | Chi tiết từng mặt hàng nằm trong đơn hàng (`UnitPriceInclTax`, `Quantity`...) | `OrderId`, `ProductId` |
| `Address` | Lưu địa chỉ nhận hàng / thanh toán của người dùng | `Id` |

---

## 2.1 BẢNG MÃ TRẠNG THÁI (ENUM VALUES) CHUẨN TRÊN NOPCOMMERCE

#### 🟢 Loại Giỏ Hàng (`ShoppingCartTypeId` trong bảng `ShoppingCartItem`)
- `1`: ShoppingCart (Giỏ hàng chính)
- `2`: Wishlist (Danh sách yêu thích)

#### 🔵 Trạng Thái Đơn Hàng (`OrderStatusId` trong bảng `[Order]`)
- `10`: Pending (Chờ xử lý)
- `20`: Processing (Đang xử lý)
- `30`: Complete (Đã hoàn thành)
- `40`: Cancelled (Đã hủy)

#### 🟡 Trạng Thái Thanh Toán (`PaymentStatusId` trong bảng `[Order]`)
- `10`: Pending (Chờ thanh toán)
- `20`: Authorized (Đã xác thực thẻ)
- `30`: Paid (Đã thanh toán)
- `35`: PartiallyRefunded (Hoàn tiền một phần)
- `40`: Refunded (Đã hoàn tiền)
- `50`: Voided (Đã hủy giao dịch)

---

## 3. CHI TIẾT CÁC CÂU QUERY SQL ĐÃ VERIFY VỚI LOCALHOST (100% KHỚP NOPCOMMERCE)

Dạng 1: Kiểm tra Dữ liệu Cơ bản (`SELECT`, `WHERE`, `ORDER BY`, `TOP`)

1.1. Tìm thông tin Customer theo Email đăng ký
SELECT Id, CustomerGuid, Email, Username, Active, Deleted, CreatedOnUtc
FROM Customer
WHERE Email = 'admin@yourstore.com';

1.2. Lấy 5 tài khoản mới nhất (Lưu ý: Khách vãng lai Guest sẽ có `Email IS NULL`)
SELECT TOP 5 Id, Email, Active, CreatedOnUtc 
FROM Customer 
ORDER BY CreatedOnUtc DESC;
```

1.3. Tìm các sản phẩm có giá từ $100 đến $500 và đang hiển thị (Published)
SELECT Id, Name, Price, StockQuantity, Published
FROM Product
WHERE Price BETWEEN 100 AND 500
  AND Published = 1
  AND Deleted = 0
ORDER BY Price ASC;


1.4. Tìm kiếm sản phẩm theo từ khóa gần đúng
SELECT Id, Name, Sku, Price 
FROM Product 
WHERE Name LIKE '%Computer%' OR Name LIKE '%Laptop%';


Dạng 2: Liên kết nhiều Bảng (`INNER JOIN`, `LEFT JOIN`)

2.1. Lấy thông tin Customer kèm theo Tên Vai Trò (Role Name) của họ
*(Chuẩn cột: `Customer_Id` & `CustomerRole_Id` trong bảng Mapping)*

SELECT 
    c.Id AS CustomerId, 
    c.Email, 
    cr.Name AS RoleName,
    cr.SystemName
FROM Customer c
INNER JOIN Customer_CustomerRole_Mapping ccrm ON c.Id = ccrm.Customer_Id
INNER JOIN CustomerRole cr ON ccrm.CustomerRole_Id = cr.Id
WHERE c.Email = 'admin@yourstore.com';
```

 2.2. Kiểm tra sản phẩm nằm trong Giỏ Hàng chính (`ShoppingCartTypeId = 1`)
SELECT 
    sci.Id AS CartItemId,
    c.Email AS CustomerEmail,
    p.Name AS ProductName,
    p.Price AS UnitPrice,
    sci.Quantity,
    (p.Price * sci.Quantity) AS TotalPrice,
    sci.CreatedOnUtc
FROM ShoppingCartItem sci
INNER JOIN Customer c ON sci.CustomerId = c.Id
INNER JOIN Product p ON sci.ProductId = p.Id
WHERE c.Email = 'admin@yourstore.com'
  AND sci.ShoppingCartTypeId = 1; -- 1: ShoppingCart
```

 2.3. Xem Chi tiết Đơn hàng (Order Detail) kèm Tên Sản phẩm & Địa chỉ Billing

SELECT 
    o.Id AS OrderId,
    o.OrderGuid,
    c.Email AS CustomerEmail,
    a.FirstName + ' ' + a.LastName AS FullName,
    a.Address1,
    a.City,
    p.Name AS ProductName,
    oi.Quantity,
    oi.UnitPriceInclTax,
    o.OrderTotal,
    o.OrderStatusId
FROM [Order] o
INNER JOIN Customer c ON o.CustomerId = c.Id
INNER JOIN Address a ON o.BillingAddressId = a.Id
INNER JOIN OrderItem oi ON o.Id = oi.OrderId
INNER JOIN Product p ON oi.ProductId = p.Id
WHERE o.Id = 1;  -- Thay ID đơn hàng bạn muốn xem
```

---

Dạng 3: Gom nhóm & Thống kê (`COUNT`, `SUM`, `AVG`, `GROUP BY`, `HAVING`)

3.1. Đếm tổng số lượng Customer thuộc từng vai trò (Role)
SELECT 
    cr.Name AS RoleName, 
    COUNT(ccrm.Customer_Id) AS TotalUsers
FROM CustomerRole cr
LEFT JOIN Customer_CustomerRole_Mapping ccrm ON cr.Id = ccrm.CustomerRole_Id
GROUP BY cr.Name;

3.2. Tính Tổng doanh thu & Số đơn hàng đã hoàn thành của từng Khách hàng

SELECT 
    c.Email,
    COUNT(o.Id) AS TotalOrders,
    SUM(o.OrderTotal) AS TotalSpent
FROM [Order] o
INNER JOIN Customer c ON o.CustomerId = c.Id
WHERE o.OrderStatusId = 30  -- 30: Complete
GROUP BY c.Email
HAVING COUNT(o.Id) >= 1
ORDER BY TotalSpent DESC;
```

---

Dạng 4: Truy vấn Tìm Lỗi / Sai lệch Dữ liệu (Manual DB Testing Bugs)
4.1. [Bug Check] Sản phẩm đã HẾT HÀNG (`StockQuantity <= 0`) nhưng vẫn mở bán (`Published = 1`)

SELECT Id, Name, StockQuantity, Published
FROM Product
WHERE StockQuantity <= 0 AND Published = 1 AND Deleted = 0;

4.2. [Bug Check] Đơn hàng thành công nhưng Tổng tiền bằng 0 (`OrderTotal <= 0`)

SELECT Id, CustomerId, OrderTotal, OrderStatusId, PaymentStatusId, CreatedOnUtc
FROM [Order]
WHERE OrderTotal <= 0 AND OrderStatusId <> 40; -- 40: Cancelled

4.3. [Bug Check] Tài khoản Customer không gán bất kỳ Role nào
SELECT c.Id, c.Email, c.CreatedOnUtc
FROM Customer c
LEFT JOIN Customer_CustomerRole_Mapping ccrm ON c.Id = ccrm.Customer_Id
WHERE ccrm.CustomerRole_Id IS NULL AND c.Deleted = 0;
```

---

## 4. QUY TRÌNH MANUAL DATABASE TESTING THEO KỊCH BẢN GIAO DIỆN (UI WORKFLOWS)

Kịch bản 1: Đăng ký tài khoản người dùng mới (Register User)
- **Thao tác UI:**
  1. Truy cập `http://localhost:8080/register`.
  2. Điền email: `qa_fresher_01@gmail.com`, password: `Password123`, Họ tên, Ngày sinh.
  3. Bấm **Register**.
- **Các bước Test DB trên DBeaver:**
  1. Chạy query tìm tài khoản vừa tạo:
     ```sql
     SELECT Id, Email, Active, Deleted, CreatedOnUtc 
     FROM Customer 
     WHERE Email = 'qa_fresher_01@gmail.com';
     ```
  2. Verify:
     - [ ] Email chính xác = `qa_fresher_01@gmail.com`
     - [ ] `Active` = `1` (True)
     - [ ] `Deleted` = `0` (False)
     - [ ] `CreatedOnUtc` lưu theo UTC chính xác với giờ thao tác.
  3. Kiểm tra Role mặc định là `Registered` (SystemName = `Registered`):
     ```sql
     SELECT cr.SystemName 
     FROM Customer c
     JOIN Customer_CustomerRole_Mapping ccrm ON c.Id = ccrm.Customer_Id
     JOIN CustomerRole cr ON ccrm.CustomerRole_Id = cr.Id
     WHERE c.Email = 'qa_fresher_01@gmail.com';
     ```

---

 Kịch bản 2: Thêm sản phẩm vào Giỏ Hàng (Add to Cart)
- **Thao tác UI:**
  1. Đăng nhập bằng `qa_fresher_01@gmail.com`.
  2. Tìm sản phẩm "Build your own computer", chọn số lượng = `3`, nhấn **Add to cart**.
- **Các bước Test DB trên DBeaver:**
  1. Chạy query kiểm tra bảng giỏ hàng:
     ```sql
     SELECT sci.CustomerId, p.Name, sci.Quantity, sci.UpdatedOnUtc
     FROM ShoppingCartItem sci
     JOIN Customer c ON sci.CustomerId = c.Id
     JOIN Product p ON sci.ProductId = p.Id
     WHERE c.Email = 'qa_fresher_01@gmail.com'
       AND sci.ShoppingCartTypeId = 1;
     ```
  2. Verify:
     - [ ] Cột `Quantity` trong DB phải bằng đúng `3`.
     - [ ] `ProductId` phải khớp với ID của sản phẩm "Build your own computer".

---

## 5. BÀI TẬP TỰ THỰC HÀNH (ĐÃ CHUẨN HÓA CỘT THEO LOCALHOST DB)

Bạn hãy thực hiện các bài tập bên dưới bằng cách **gõ trực tiếp câu lệnh SQL của bạn vào vị trí đánh dấu `[GÕ QUERY CỦA BẠN TẠI ĐÂY]`**.

---

### 📝 BÀI TẬP 1: Lọc danh sách sản phẩm bị xóa tạm (Soft Delete)
- **Yêu cầu:** Tìm tất cả sản phẩm trong bảng `Product` bị đánh dấu xóa (`Deleted = 1`).
- **Gõ câu lệnh SQL của bạn bên dưới:**

```sql
-- [GÕ QUERY CỦA BẠN TẠI ĐÂY]

```

<details>
<summary>💡 Gợi ý / Đáp án mẫu (Nhấn vào để mở)</summary>

```sql
SELECT Id, Name, Price, Deleted 
FROM Product 
WHERE Deleted = 1;
```
</details>

---

### 📝 BÀI TẬP 2: Đếm số lượng sản phẩm trong từng Danh Mục (Category)
- **Lưu ý tên cột:** Bảng `Product_Category_Mapping` dùng cột `ProductId` và `CategoryId` (không có dấu `_`).
- **Yêu cầu:** Liệt kê Tên Danh Mục (`Category.Name`) và Số Lượng Sản Phẩm thuộc danh mục đó (`COUNT(pcm.ProductId)`).
- **Gõ câu lệnh SQL của bạn bên dưới:**


<details>
<summary>💡 Gợi ý / Đáp án mẫu (Nhấn vào để mở)</summary>

```sql
SELECT 
    cat.Name AS CategoryName, 
    COUNT(pcm.ProductId) AS TotalProducts
FROM Category cat
LEFT JOIN Product_Category_Mapping pcm ON cat.Id = pcm.CategoryId
GROUP BY cat.Name
ORDER BY TotalProducts DESC;
```
</details>

---

### 📝 BÀI TẬP 3: Tìm địa chỉ nhận hàng của một Customer bất kỳ
- **Lưu ý tên cột:** Bảng `Customer` dùng tên cột địa chỉ là `BillingAddress_Id` và `ShippingAddress_Id` (có dấu `_`).
- **Yêu cầu:** Lấy danh sách Địa chỉ (`Address1`, `City`, `ZipPostalCode`, `PhoneNumber`) từ bảng `Address` tương ứng với Customer có email `'admin@yourstore.com'`.
- **Gõ câu lệnh SQL của bạn bên dưới:**

```sql
-- [GÕ QUERY CỦA BẠN TẠI ĐÂY]

```

<details>
<summary>💡 Gợi ý / Đáp án mẫu (Nhấn vào để mở)</summary>

```sql
SELECT a.FirstName, a.LastName, a.Address1, a.City, a.ZipPostalCode, a.PhoneNumber
FROM Address a
INNER JOIN Customer c ON a.Id = c.BillingAddress_Id OR a.Id = c.ShippingAddress_Id
WHERE c.Email = 'admin@yourstore.com';
```
</details>

---

### 📝 BÀI TẬP 4: Tìm 3 Đơn hàng có Tổng Giá Trị (`OrderTotal`) cao nhất
- **Yêu cầu:** Lấy Top 3 đơn hàng trong bảng `[Order]` có `OrderTotal` lớn nhất, bao gồm thông tin: `Id`, `OrderGuid`, `OrderTotal`, `CreatedOnUtc`.
- **Gõ câu lệnh SQL của bạn bên dưới:**

```sql
-- [GÕ QUERY CỦA BẠN TẠI ĐÂY]

```

<details>
<summary>💡 Gợi ý / Đáp án mẫu (Nhấn vào để mở)</summary>

```sql
SELECT TOP 3 Id, OrderGuid, OrderTotal, CreatedOnUtc
FROM [Order]
ORDER BY OrderTotal DESC;
```
</details>


