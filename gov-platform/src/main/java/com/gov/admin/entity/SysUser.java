package com.gov.admin.entity;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
@Data
@TableName("sys_user")
public class SysUser {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String deptCode;
    private Integer status;
    @TableLogic private Integer deleted;
}
