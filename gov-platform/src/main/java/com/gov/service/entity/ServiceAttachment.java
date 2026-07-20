package com.gov.service.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("service_attachment")
public class ServiceAttachment {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("accept_no")
    private String acceptNo;

    @TableField("file_name")
    private String fileName;

    @TableField("file_path")
    private String filePath;

    @TableField("file_type")
    private String fileType;

    @TableField("file_size")
    private Long fileSize;

    @TableField("upload_time")
    private LocalDateTime uploadTime;
}
