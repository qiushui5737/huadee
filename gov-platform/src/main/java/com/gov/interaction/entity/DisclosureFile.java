package com.gov.interaction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 保密文件实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("disclosure_file")
public class DisclosureFile extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String fileCode;

    private String fileName;

    private String fileCategory;

    private String fileType;

    private Long fileSize;

    private String fileUrl;

    private String description;

    private String deptCode;

    private String deptName;

    private String confidentialityLevel;

    private String status;

    private LocalDateTime publishTime;

    @TableField(exist = false)
    private LocalDate deadline;
}
