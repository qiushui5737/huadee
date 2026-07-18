package com.gov.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.gov.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    private String username;

    private String password;

    @TableField("real_name")
    private String realName;

    private String gender;

    @TableField("id_card")
    private String idCard;

    private String phone;

    private String email;

    @TableField("dept_code")
    private String deptCode;

    private String address;

    private Integer status;
}