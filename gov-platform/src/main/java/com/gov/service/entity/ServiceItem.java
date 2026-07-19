package com.gov.service.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.gov.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("service_item")
public class ServiceItem extends BaseEntity {

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

    @TableField("price")
    private java.math.BigDecimal price;
}
