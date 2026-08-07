package com.nopcommerce.interfaceUI;

public class LoginPageUI {

    // === Returning Customer Form ===
    public static final String EMAIL_INPUT            = "id=Email";
    public static final String PASSWORD_INPUT         = "id=Password";
    public static final String LOGIN_BUTTON           = "css=button.login-button";

    // === Error Messages ===
    // Case: email trống / email sai định dạng → lỗi inline dưới field (span validation)
    public static final String EMAIL_INLINE_ERROR     = "css=#Email-error";

    // Case: email chưa đăng ký / password sai / password trống → lỗi summary đầu trang
    public static final String SUMMARY_ERROR_DETAIL   = "css=.message-error ul li";
}
