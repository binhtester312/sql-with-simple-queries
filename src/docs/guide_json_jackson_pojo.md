# 📘 Hướng Dẫn Data-Driven Testing: JSON + Jackson + POJO + TestNG

Tách dữ liệu kiểm thử (Test Data) ra khỏi kịch bản test (Test Script) trong dự án Automation Testing (Java + Selenium + TestNG).

---

🎯 1. Tổng Quan 

Hardcode file Test (ví dụ: `enterEmail("user@gmail.com")`), mỗi khi muốn test 10 kịch bản khác nhau, ta phải viết lại 10 hàm test. 

Giải pháp là **Data-Driven Testing**: cất data file **JSON**, sau đó dùng code Java đọc dữ liệu này ra và truyền tự động vào kịch bản test.

4 Thành Phần Cốt Lõi:
1. File JSON (`login_invalid_data.json`) | Nơi chứa dữ liệu kiểm thử thô bên ngoài code.
2. Class POJO (`LoginData.java`) | Khai báo các thuộc tính tương ứng với dữ liệu JSON để Java dễ quản lý.
3. Jackson Library** (`ObjectMapper`) | Thư viện tự động đọc dữ liệu JSON và nạp vào các Java Object.
4. TestNG `@DataProvider`** | Chức năng lấy mảng Object từ Jackson và bơm lần lượt từng case vào `@Test`. 

---

 2. Dữ Liệu Kiểm Thử Trong Bài Này Là Gì?

**Kiểm thử đăng nhập thất bại (Negative Login Test)** cho trang nopCommerce.

* **Tên file data:** `login_invalid_data.json`
* **Loại dữ liệu:** Mảng (Array) gồm 3 kịch bản lỗi với các thông tin:
  * `description`: Mô tả trường hợp test (Email trống, sai định dạng, chưa đăng ký).
  * `email`: Email nhập vào màn hình.
  * `password`: Mật khẩu nhập vào màn hình.
  * `expectedErrorMessage`: Thông báo lỗi kỳ vọng hiển thị trên giao diện.

---

 3. Luồng Chuyển Đổi Dữ Liệu (Data Flow)

flowchart LR
    A[" File JSON<br/>(login_invalid_data.json)"] -->|"Jackson (ObjectMapper)"| B["Class POJO<br/>(LoginData[])"]
    B -->|"Nạp vào"| C[" TestNG @DataProvider<br/>(DataHelper)"]
    C -->|"Bơm từng case"| D[" Test Case<br/>(@Test testLoginInvalid)"]


 4. Hướng Dẫn Thực Hành Từng Bước

📌 BƯỚC 1: Thêm thư viện Jackson vào `pom.xml`
Thư viện `jackson-databind` giúp Java có thể đọc và hiểu được file định dạng JSON.

Mở file `pom.xml` và thêm dependency:

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.17.1</version>
</dependency>
```

---

 📌 BƯỚC 2: Tạo file dữ liệu `login_invalid_data.json`
Tạo file tại đường dẫn: `src/test/resources/data/login_invalid_data.json`

```json
[
  {
    "description": "Email trống",
    "email": "",
    "password": "Test@123456!",
    "expectedErrorMessage": "Please enter your email"
  },
  {
    "description": "Email sai định dạng",
    "email": "invalid-email-format",
    "password": "Test@123456!",
    "expectedErrorMessage": "Wrong email"
  },
  {
    "description": "Email chưa đăng ký",
    "email": "notfound9999@gmail.com",
    "password": "Test@123456!",
    "expectedErrorMessage": "Login was unsuccessful."
  }
]
```


📌 BƯỚC 3: Tạo Class POJO (`LoginData.java`)
Tạo file tại đường dẫn: `src/test/java/com/nopcommerce/data/LoginData.java`

**Lưu ý quan trọng:** Tên các biến trong Java phải **chính xác từng chữ** với các key trong file JSON (`description`, `email`, `password`, `expectedErrorMessage`).


package com.nopcommerce.data;

public class LoginData {
    private String description;
    private String email;
    private String password;
    private String expectedErrorMessage;

    // Getter & Setter (Bắt buộc để Jackson đọc/ghi dữ liệu)
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getExpectedErrorMessage() { return expectedErrorMessage; }
    public void setExpectedErrorMessage(String expectedErrorMessage) { this.expectedErrorMessage = expectedErrorMessage; }
}
```

---

 BƯỚC 4: Viết DataProvider đọc JSON bằng Jackson
Tạo file tại đường dẫn: `src/test/java/com/nopcommerce/data/DataHelper.java`


package com.nopcommerce.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.DataProvider;

import java.io.File;
import java.io.IOException;

public class DataHelper {

    @DataProvider(name = "invalidLoginData")
    public static Object[][] getInvalidLoginData() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File jsonFile = new File("src/test/resources/data/login_invalid_data.json");
        
        // 1. Chuyển file JSON thành mảng đối tượng LoginData[]
        LoginData[] dataArray = mapper.readValue(jsonFile, LoginData[].class);
        
        // 2. Chuyển sang mảng 2 chiều Object[][] để phù hợp với TestNG DataProvider
        Object[][] data = new Object[dataArray.length][1];
        for (int i = 0; i < dataArray.length; i++) {
            data[i][0] = dataArray[i];
        }
        return data;
    }
}
```

---

 BƯỚC 5: Sử dụng dữ liệu trong Test Script
Trong file Test (ví dụ: `LoginTest.java`), liên kết với `@DataProvider` vừa tạo:

package com.nopcommerce.tests;

import com.nopcommerce.data.DataHelper;
import com.nopcommerce.data.LoginData;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {

    @Test(dataProvider = "invalidLoginData", dataProviderClass = DataHelper.class)
    public void testLoginInvalidCases(LoginData loginData) {
        System.out.println("🧪 Đang kiểm thử kịch bản: " + loginData.getDescription());
        
        // 1. Nhập email & password từ file JSON vào giao diện
        // loginPage.enterEmail(loginData.getEmail());
        // loginPage.enterPassword(loginData.getPassword());
        // loginPage.clickLoginButton();
        
        // 2. Kiểm tra thông báo lỗi có đúng như mong đợi không
        // String actualError = loginPage.getErrorMessage();
        // Assert.assertEquals(actualError, loginData.getExpectedErrorMessage());
    }
}
```

---

 (Quick Rules)

1. **Khớp tên biến:** Tên biến trong Class POJO phải khớp 100% với thuộc tính trong JSON.
2. **Getter/Setter:** Đừng quên tạo các hàm Getter/Setter (hoặc dùng `@Data` của Lombok nếu dự án có cài Lombok).
3. **Thêm case mới dễ dàng:** Khi muốn test thêm 1 trường hợp lỗi mới, chỉ cần thêm 1 đoạn `{ ... }` vào file `login_invalid_data.json` mà **không cần sửa bất kỳ dòng code Java nào**.
