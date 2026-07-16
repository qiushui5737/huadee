package com.gov.admin.controller;

import com.gov.common.result.Result;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * E1-数据采集 & E2-可视化报表大屏
 */
@RestController
@RequestMapping("/api/v1/admin/stats")
public class StatsController {

    @GetMapping("/dashboard")
    public Result<Map<String,Object>> dashboard() {
        Map<String,Object> data = new HashMap<>();
        // 核心指标
        data.put("totalVisits", 1284567);
        data.put("totalServices", 35678);
        data.put("totalMessages", 8923);
        data.put("satisfactionRate", 96.8);
        // 趋势数据
        data.put("weeklyTrend", List.of(
            Map.of("date","周一","visits",18234,"services",4567),
            Map.of("date","周二","visits",19567,"services",5023),
            Map.of("date","周三","visits",20890,"services",5456),
            Map.of("date","周四","visits",22345,"services",5789),
            Map.of("date","周五","visits",23678,"services",6012),
            Map.of("date","周六","visits",12890,"services",3234),
            Map.of("date","周日","visits",10987,"services",2890)
        ));
        return Result.success(data);
    }

    @GetMapping("/report")
    public Result<Map<String,Object>> report(@RequestParam(required=false) String startDate,
                                              @RequestParam(required=false) String endDate) {
        // TODO: 统计报表
        return Result.success(Collections.emptyMap());
    }
}
