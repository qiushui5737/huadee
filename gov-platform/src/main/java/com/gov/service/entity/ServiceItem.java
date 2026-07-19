package com.gov.service.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("service_item")
public class ServiceItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("item_code")
    private String itemCode;

    @TableField("item_name")
    private String itemName;

    private String category;

    @TableField("dept_code")
    private String deptCode;

    private String description;

    @TableField("form_schema")
    private String formSchema;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}