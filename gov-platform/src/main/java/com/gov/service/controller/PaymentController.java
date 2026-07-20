package com.gov.service.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gov.common.result.Result;
import com.gov.service.entity.ServiceItem;
import com.gov.service.entity.ServiceRecord;
import com.gov.service.service.CertificateGenerator;
import com.gov.service.service.ServiceItemService;
import com.gov.service.service.ServiceRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @Autowired
    private CertificateGenerator certificateGenerator;

    @GetMapping("/calculate/{acceptNo}")
    public Result<Map<String,Object>> calculate(@RequestAttribute("jwtToken") String token, @PathVariable String acceptNo) {
        Long userId = com.gov.common.utils.JwtUtil.getUserIdFromToken(token);
        ServiceRecord record = serviceRecordService.getByAcceptNo(acceptNo);
        if (record == null) {
            return Result.error("办件记录不存在");
        }
        if (!userId.equals(record.getUserId())) {
            return Result.error("无权访问此办件");
        }
        // 草稿不能缴费
        if (record.getDraft() != null && record.getDraft() == 1) {
            return Result.error("草稿状态，请先提交申请");
        }

        // 确定实际缴费金额：优先用记录的payAmount，否则从事项获取price
        BigDecimal actualAmount = record.getPayAmount();
        if (actualAmount == null) {
            ServiceItem item = serviceItemService.getById(record.getItemId());
            if (item != null && item.getPrice() != null) {
                actualAmount = item.getPrice();
                // 同步更新记录的payAmount
                record.setPayAmount(actualAmount);
                serviceRecordService.updateById(record);
            } else {
                actualAmount = BigDecimal.ZERO;
            }
        }

        Map<String,Object> result = new HashMap<>();
        result.put("acceptNo", acceptNo);
        result.put("amount", actualAmount);
        result.put("description", "办事服务费");
        result.put("payStatus", record.getPayStatus() != null ? record.getPayStatus() : "待支付");
        return Result.success(result);
    }

    @PostMapping("/pay")
    public Result<Map<String,Object>> pay(@RequestAttribute("jwtToken") String token, @RequestBody Map<String,Object> payData) {
        Long userId = com.gov.common.utils.JwtUtil.getUserIdFromToken(token);
        String acceptNo = (String) payData.get("acceptNo");
        BigDecimal amount = new BigDecimal(payData.get("amount").toString());

        ServiceRecord record = serviceRecordService.getByAcceptNo(acceptNo);
        if (record == null) {
            return Result.error("办件记录不存在");
        }
        if (!userId.equals(record.getUserId())) {
            return Result.error("无权支付此办件");
        }
        // 草稿不能缴费
        if (record.getDraft() != null && record.getDraft() == 1) {
            return Result.error("草稿状态，请先提交申请");
        }

        if ("已支付".equals(record.getPayStatus())) {
            return Result.error("该办件已支付");
        }

        record.setPayStatus("已支付");
        record.setPayAmount(amount);
        record.setPayTime(LocalDateTime.now());
        
        // 缴费完成后自动推进到已办结
        // 兼容多阶段审批的"待缴费办结"和旧流程的"已审批"
        String status = record.getStatus();
        String stage = record.getCurrentStage();
        if ("待缴费办结".equals(stage) || "已审批".equals(status)) {
            record.setStatus("已办结");
            record.setCurrentStage("已办结");
            record.setFinishTime(LocalDateTime.now());
        }
        
        serviceRecordService.updateById(record);

        Map<String,Object> result = new HashMap<>();
        result.put("acceptNo", acceptNo);
        result.put("amount", amount);
        result.put("payTime", record.getPayTime());
        result.put("payNo", "PAY" + System.currentTimeMillis());
        result.put("payStatus", "已支付");
        result.put("serviceStatus", record.getStatus());
        return Result.success(result, "支付成功，办件已办结");
    }

    @GetMapping("/license/{acceptNo}")
    public Result<Map<String,Object>> license(@RequestAttribute("jwtToken") String token, @PathVariable String acceptNo) {
        Long userId = com.gov.common.utils.JwtUtil.getUserIdFromToken(token);
        ServiceRecord record = serviceRecordService.getByAcceptNo(acceptNo);
        if (record == null) {
            return Result.error("办件记录不存在");
        }
        if (!userId.equals(record.getUserId())) {
            return Result.error("无权领取此证照");
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
    public ResponseEntity<byte[]> download(@RequestAttribute("jwtToken") String token, @PathVariable String acceptNo) {
        Long userId = com.gov.common.utils.JwtUtil.getUserIdFromToken(token);
        ServiceRecord record = serviceRecordService.getByAcceptNo(acceptNo);
        if (record == null) {
            return ResponseEntity.notFound().build();
        }
        if (!userId.equals(record.getUserId())) {
            return ResponseEntity.status(403).build();
        }

        // 获取事项名称
        ServiceItem item = serviceItemService.getById(record.getItemId());
        String itemName = item != null ? item.getItemName() : "政务服务事项";

        // 解析表单数据
        java.util.Map<String, Object> formData = new java.util.HashMap<>();
        if (record.getFormData() != null) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                formData = mapper.readValue(record.getFormData(), java.util.Map.class);
            } catch (Exception e) {
                // ignore
            }
        }

        // 生成证照PDF
        byte[] pdfBytes = certificateGenerator.generateCertificate(
                acceptNo, itemName, record.getUserName(), formData);

        String fileName = itemName + "_证照_" + acceptNo + ".pdf";
        try {
            fileName = java.net.URLEncoder.encode(fileName, "UTF-8");
        } catch (Exception e) {
            // ignore
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/records")
    public Result<List<Map<String,Object>>> paymentRecords(@RequestAttribute("jwtToken") String token) {
        Long userId = com.gov.common.utils.JwtUtil.getUserIdFromToken(token);
        List<ServiceRecord> records = serviceRecordService.list(Wrappers.<ServiceRecord>lambdaQuery()
                .eq(ServiceRecord::getUserId, userId)
                .orderByDesc(ServiceRecord::getSubmitTime));
        List<Map<String,Object>> result = new ArrayList<>();

        for (ServiceRecord record : records) {
            // 过滤草稿
            if (record.getDraft() != null && record.getDraft() == 1) {
                continue;
            }
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