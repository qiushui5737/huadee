package com.gov.service.controller;

import com.gov.common.result.Result;
import com.gov.service.entity.ServiceItem;
import com.gov.service.entity.ServiceRecord;
import com.gov.service.service.ServiceItemService;
import com.gov.service.service.ServiceRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/service/payment")
public class PaymentController {

    @Autowired
    private ServiceRecordService serviceRecordService;
    
    @Autowired
    private ServiceItemService serviceItemService;

    @GetMapping("/calculate/{acceptNo}")
    public Result<Map<String,Object>> calculate(@PathVariable String acceptNo) {
        ServiceRecord record = serviceRecordService.getByAcceptNo(acceptNo);
        if (record == null) {
            return Result.error("办件记录不存在");
        }

        Map<String,Object> result = new HashMap<>();
        result.put("acceptNo", acceptNo);
        result.put("amount", record.getPayAmount() != null ? record.getPayAmount() : new BigDecimal("100.00"));
        result.put("description", "办事服务费");
        result.put("payStatus", record.getPayStatus() != null ? record.getPayStatus() : "待支付");
        return Result.success(result);
    }

    @PostMapping("/pay")
    public Result<Map<String,Object>> pay(@RequestBody Map<String,Object> payData) {
        String acceptNo = (String) payData.get("acceptNo");
        BigDecimal amount = new BigDecimal(payData.get("amount").toString());

        ServiceRecord record = serviceRecordService.getByAcceptNo(acceptNo);
        if (record == null) {
            return Result.error("办件记录不存在");
        }

        if ("已支付".equals(record.getPayStatus())) {
            return Result.error("该办件已支付");
        }

        record.setPayStatus("已支付");
        record.setPayAmount(amount);
        record.setPayTime(LocalDateTime.now());
        serviceRecordService.updateById(record);

        Map<String,Object> result = new HashMap<>();
        result.put("acceptNo", acceptNo);
        result.put("amount", amount);
        result.put("payTime", record.getPayTime());
        result.put("payNo", "PAY" + System.currentTimeMillis());
        result.put("payStatus", "已支付");
        return Result.success(result, "支付成功");
    }

    @GetMapping("/license/{acceptNo}")
    public Result<Map<String,Object>> license(@PathVariable String acceptNo) {
        ServiceRecord record = serviceRecordService.getByAcceptNo(acceptNo);
        if (record == null) {
            return Result.error("办件记录不存在");
        }

        if (!"已办结".equals(record.getStatus())) {
            return Result.error("办件尚未办结，暂无法领取证照");
        }

        record.setLicenseStatus("已发放");
        serviceRecordService.updateById(record);

        Map<String,Object> result = new HashMap<>();
        result.put("acceptNo", acceptNo);
        result.put("licenseName", "电子证照");
        result.put("licenseNo", "LIC" + System.currentTimeMillis());
        result.put("issueDate", LocalDateTime.now());
        result.put("expireDate", LocalDateTime.now().plusYears(5));
        result.put("status", "已发放");
        result.put("downloadUrl", "/api/v1/service/payment/download/" + acceptNo);
        return Result.success(result);
    }

    @GetMapping("/download/{acceptNo}")
    public Result<Map<String,Object>> download(@PathVariable String acceptNo) {
        ServiceRecord record = serviceRecordService.getByAcceptNo(acceptNo);
        if (record == null) {
            return Result.error("办件记录不存在");
        }

        Map<String,Object> result = new HashMap<>();
        result.put("acceptNo", acceptNo);
        result.put("message", "证照下载链接已生成");
        result.put("url", "/download/license/" + acceptNo + ".pdf");
        return Result.success(result);
    }

    @GetMapping("/records")
    public Result<List<Map<String,Object>>> paymentRecords(@RequestParam(required=false) String status) {
        List<ServiceRecord> records = serviceRecordService.list();
        List<Map<String,Object>> result = new ArrayList<>();

        for (ServiceRecord record : records) {
            Map<String,Object> map = new HashMap<>();
            map.put("acceptNo", record.getAcceptNo());
            map.put("userName", record.getUserName());
            map.put("serviceStatus", record.getStatus());
            map.put("submitTime", record.getSubmitTime());
            map.put("payStatus", record.getPayStatus() != null ? record.getPayStatus() : "待支付");
            map.put("payAmount", record.getPayAmount());
            map.put("payTime", record.getPayTime());
            map.put("licenseStatus", record.getLicenseStatus() != null ? record.getLicenseStatus() : "办理中");
            
            ServiceItem item = serviceItemService.getById(record.getItemId());
            if (item != null) {
                map.put("itemName", item.getItemName());
            }
            
            result.add(map);
        }
        return Result.success(result);
    }
}