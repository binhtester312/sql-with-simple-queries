package com.nopcommerce.data;

public class LoginData {
    private String description;
    private String email;
    private String password;
    private String expectedErrorMessage;

    // Getter & Setter (Bắt buộc để Jackson đọc/ghi dữ liệu)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getExpectedErrorMessage() {
        return expectedErrorMessage;
    }

    public void setExpectedErrorMessage(String expectedErrorMessage) {
        this.expectedErrorMessage = expectedErrorMessage;
    }
}
