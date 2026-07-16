package com.gov;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 政府网站集约化平台 - 单体启动类
 * A/B/C/D/E五组Controller统一扫描，单端口8080启动
 */
@SpringBootApplication(scanBasePackages = {"com.gov"})
@MapperScan({
    "com.gov.ai.mapper",
    "com.gov.service.mapper",
    "com.gov.interaction.mapper",
    "com.gov.cms.mapper",
    "com.gov.admin.mapper"
})
public class GovApplication {

    public static void main(String[] args) {
        SpringApplication.run(GovApplication.class, args);
    }
}
