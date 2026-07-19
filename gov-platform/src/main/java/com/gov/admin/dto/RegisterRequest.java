package com.gov.admin.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String confirmPassword;
    private String realName;
    private String gender;
    private String idCard;
    private String phone;
    private String email;
    private String deptCode;
    private String address;
}
