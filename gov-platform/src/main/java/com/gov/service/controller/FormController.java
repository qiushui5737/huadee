package com.gov.service.controller;

import com.gov.common.result.Result;
import com.gov.service.entity.ServiceItem;
import com.gov.service.entity.ServiceRecord;
import com.gov.service.entity.ServiceInquiry;
import com.gov.service.entity.ServiceAttachment;
import com.gov.service.entity.ServiceRating;
import com.gov.service.service.ServiceItemService;
import com.gov.service.service.ServiceRecordService;
import com.gov.service.mapper.ServiceInquiryMapper;
import com.gov.service.mapper.ServiceAttachmentMapper;
import com.gov.service.mapper.ServiceRatingMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/service/form")
public class FormController {

    @Autowired
    private ServiceItemService serviceItemService;

    @Autowired
    private ServiceRecordService serviceRecordService;

    @Autowired
    private ServiceInquiryMapper serviceInquiryMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ServiceAttachmentMapper attachmentMapper;

    @Autowired
    private ServiceRatingMapper ratingMapper;

    @Value("${app.upload-dir:./uploads}")
    private String uploadDir;

    @GetMapping("/schema/{itemId}")
    public Result<Map<String,Object>> formSchema(@PathVariable Long itemId) {
        ServiceItem item = serviceItemService.getItemById(itemId);
        if (item == null) {
            return Result.error("事项不存在");
        }
        Map<String,Object> schema = new HashMap<>();
        if (item.getFormSchema() != null && !item.getFormSchema().isEmpty()) {
            try {
                schema = objectMapper.readValue(item.getFormSchema(), Map.class);
                // 如果schema中有materials列表，将其转换为fields供前端渲染
                if (schema.containsKey("materials") && !schema.containsKey("fields")) {
                    List<?> materialsList = (List<?>) schema.get("materials");
                    if (materialsList != null && !materialsList.isEmpty()) {
                        List<Map<String,Object>> fields = new ArrayList<>();
                        // 添加基础字段
                        fields.addAll(getBaseFields(item.getItemName()));
                        // 将每个材料转换为一个表单字段
                        for (int i = 0; i < materialsList.size(); i++) {
                            Map<String,Object> field = new HashMap<>();
                            field.put("key", "material_" + i);
                            field.put("label", materialsList.get(i).toString());
                            field.put("type", "textarea");
                            field.put("required", false);
                            fields.add(field);
                        }
                        schema.put("fields", fields);
                    } else {
                        schema.put("fields", getBaseFields(item.getItemName()));
                    }
                }
            } catch (JsonProcessingException e) {
                schema = getDefaultSchema(item.getItemName());
            }
        } else {
            schema = getDefaultSchema(item.getItemName());
        }
        return Result.success(schema);
    }

    private List<Map<String,Object>> getBaseFields(String itemName) {
        List<Map<String,Object>> fields = new ArrayList<>();
        Map<String,Object> nameField = new HashMap<>();
        nameField.put("key", "userName");
        nameField.put("label", "申请人姓名");
        nameField.put("type", "input");
        nameField.put("required", true);
        fields.add(nameField);

        Map<String,Object> idCardField = new HashMap<>();
        idCardField.put("key", "idCard");
        idCardField.put("label", "身份证号");
        idCardField.put("type", "input");
        idCardField.put("required", true);
        fields.add(idCardField);

        Map<String,Object> phoneField = new HashMap<>();
        phoneField.put("key", "phone");
        phoneField.put("label", "联系电话");
        phoneField.put("type", "input");
        phoneField.put("required", true);
        fields.add(phoneField);

        Map<String,Object> addressField = new HashMap<>();
        addressField.put("key", "address");
        addressField.put("label", "联系地址");
        addressField.put("type", "textarea");
        addressField.put("required", true);
        fields.add(addressField);

        Map<String,Object> reasonField = new HashMap<>();
        reasonField.put("key", "reason");
        reasonField.put("label", "申请事由");
        reasonField.put("type", "textarea");
        reasonField.put("required", true);
        fields.add(reasonField);
        return fields;
    }

    private Map<String,Object> getDefaultSchema(String itemName) {
        Map<String,Object> schema = new HashMap<>();
        schema.put("title", itemName + "申请表");
        List<Map<String,Object>> fields = new ArrayList<>();

        Map<String,Object> nameField = new HashMap<>();
        nameField.put("key", "userName");
        nameField.put("label", "申请人姓名");
        nameField.put("type", "input");
        nameField.put("required", true);
        fields.add(nameField);

        Map<String,Object> idCardField = new HashMap<>();
        idCardField.put("key", "idCard");
        idCardField.put("label", "身份证号");
        idCardField.put("type", "input");
        idCardField.put("required", true);
        fields.add(idCardField);

        Map<String,Object> phoneField = new HashMap<>();
        phoneField.put("key", "phone");
        phoneField.put("label", "联系电话");
        phoneField.put("type", "input");
        phoneField.put("required", true);
        fields.add(phoneField);

        Map<String,Object> addressField = new HashMap<>();
        addressField.put("key", "address");
        addressField.put("label", "联系地址");
        addressField.put("type", "textarea");
        addressField.put("required", true);
        fields.add(addressField);

        Map<String,Object> reasonField = new HashMap<>();
        reasonField.put("key", "reason");
        reasonField.put("label", "申请事由");
        reasonField.put("type", "textarea");
        reasonField.put("required", true);
        fields.add(reasonField);

        schema.put("fields", fields);
        return schema;
    }

    @PostMapping("/submit")
    public Result<Map<String,Object>> submit(@RequestAttribute("jwtToken") String token, @RequestBody Map<String,Object> formData) {
        if (formData == null) {
            return Result.error("表单数据不能为空");
        }
        
        Long userId = com.gov.common.utils.JwtUtil.getUserIdFromToken(token);
        
        Object itemIdObj = formData.get("itemId");
        if (itemIdObj == null) {
            return Result.error("事项ID不能为空");
        }
        
        Long itemId;
        if (itemIdObj instanceof Number) {
            itemId = ((Number) itemIdObj).longValue();
        } else {
            try {
                itemId = Long.parseLong(itemIdObj.toString());
            } catch (NumberFormatException e) {
                return Result.error("事项ID格式错误");
            }
        }
        
        String userName = (String) formData.get("userName");
        if (userName == null || userName.trim().isEmpty()) {
            userName = "匿名用户";
        }

        ServiceItem item = serviceItemService.getItemById(itemId);
        if (item == null) {
            return Result.error("事项不存在");
        }

        String formDataJson;
        try {
            Map<String,Object> cleanData = new HashMap<>(formData);
            cleanData.keySet().removeAll(Arrays.asList("itemId", "acceptNo", "itemName", "formData"));
            formDataJson = objectMapper.writeValueAsString(cleanData);
        } catch (JsonProcessingException e) {
            return Result.error("表单数据序列化失败");
        }

        // 判断是否草稿
        boolean isDraft = formData.get("isDraft") != null && Boolean.parseBoolean(formData.get("isDraft").toString());
        
        // 检查是否是更新已有草稿
        String existingAcceptNo = (String) formData.get("acceptNo");
        ServiceRecord record = null;
        if (existingAcceptNo != null && !existingAcceptNo.isEmpty()) {
            record = serviceRecordService.getByAcceptNo(existingAcceptNo);
            if (record != null && record.getDraft() != null && record.getDraft() == 1) {
                // 更新已有草稿
                record.setFormData(formDataJson);
                if (userName != null) record.setUserName(userName);
                if (!isDraft) {
                    // 草稿提交为正式，清除草稿标记
                    record.setDraft(0);
                    record.setStatus("提交申请");
                    record.setCurrentStage("窗口受理");
                }
                serviceRecordService.updateById(record);
            } else {
                record = null; // 不是草稿或不存在，重新创建
            }
        }
        
        if (record == null) {
            // 创建新记录
            record = serviceRecordService.createRecord(itemId, userId, userName, formDataJson);
            if (isDraft) {
                record.setDraft(1);
                record.setStatus("草稿");
                serviceRecordService.updateById(record);
            }
        }
        
        if (item.getPrice() != null) {
            record.setPayAmount(item.getPrice());
            serviceRecordService.updateById(record);
        }

        Map<String,Object> result = new HashMap<>();
        result.put("acceptNo", record.getAcceptNo());
        result.put("status", record.getStatus());
        result.put("submitTime", record.getSubmitTime());
        result.put("isDraft", isDraft);
        return Result.success(result, isDraft ? "草稿保存成功" : "提交成功，受理号：" + record.getAcceptNo());
    }

    @GetMapping("/progress/{acceptNo}")
    public Result<List<Map<String,Object>>> progress(@PathVariable String acceptNo) {
        ServiceRecord record = serviceRecordService.getByAcceptNo(acceptNo);
        if (record == null) {
            return Result.error("办件记录不存在");
        }

        List<Map<String,Object>> progressList = new ArrayList<>();
        String stage = record.getCurrentStage();
        if (stage == null || stage.isEmpty()) stage = "窗口受理";

        // 阶段1: 提交申请
        Map<String,Object> step1 = new HashMap<>();
        step1.put("step", 1);
        step1.put("title", "提交申请");
        step1.put("desc", "申请已提交");
        step1.put("time", record.getSubmitTime());
        step1.put("completed", true);
        progressList.add(step1);

        // 阶段2: 窗口受理
        Map<String,Object> step2 = new HashMap<>();
        step2.put("step", 2);
        step2.put("title", "窗口受理");
        step2.put("desc", "窗口人员受理中");
        boolean step2Done = !"窗口受理".equals(stage) || "已办结".equals(record.getStatus()) || "已驳回".equals(record.getStatus());
        step2.put("completed", step2Done);
        progressList.add(step2);

        // 阶段3: 材料初审
        Map<String,Object> step3 = new HashMap<>();
        step3.put("step", 3);
        step3.put("title", "材料初审");
        step3.put("desc", "审核材料是否齐全");
        boolean step3Done = "实质审核".equals(stage) || "领导审批".equals(stage) || "已办结".equals(record.getStatus()) || "已驳回".equals(record.getStatus());
        step3.put("completed", step3Done);
        progressList.add(step3);

        // 阶段4: 实质审核
        Map<String,Object> step4 = new HashMap<>();
        step4.put("step", 4);
        step4.put("title", "实质审核");
        step4.put("desc", "业务部门实质审核");
        boolean step4Done = "领导审批".equals(stage) || "已办结".equals(record.getStatus()) || "已驳回".equals(record.getStatus());
        step4.put("completed", step4Done);
        progressList.add(step4);

        // 阶段5: 领导审批
        Map<String,Object> step5 = new HashMap<>();
        step5.put("step", 5);
        step5.put("title", "领导审批");
        step5.put("desc", "领导最终审批");
        boolean step5Done = "已办结".equals(record.getStatus()) || "已驳回".equals(record.getStatus());
        step5.put("completed", step5Done);
        if ("已办结".equals(record.getStatus()) && record.getFinishTime() != null) {
            step5.put("time", record.getFinishTime());
        }
        progressList.add(step5);

        // 阶段6: 结果送达
        Map<String,Object> step6 = new HashMap<>();
        step6.put("step", 6);
        step6.put("title", "结果送达");
        step6.put("desc", "办理完成，可下载电子证照");
        step6.put("completed", "已办结".equals(record.getStatus()));
        if ("已办结".equals(record.getStatus()) && record.getFinishTime() != null) {
            step6.put("time", record.getFinishTime());
        }
        progressList.add(step6);

        // 补充材料状态
        if ("待补充".equals(record.getSupplementStatus())) {
            Map<String,Object> supplementStep = new HashMap<>();
            supplementStep.put("step", 0);
            supplementStep.put("title", "补充材料");
            supplementStep.put("desc", record.getSupplementReason());
            supplementStep.put("completed", false);
            supplementStep.put("isSupplement", true);
            progressList.add(supplementStep);
        }

        return Result.success(progressList);
    }

    @GetMapping("/records")
    public Result<List<Map<String,Object>>> records(@RequestParam(required=false) Long userId) {
        List<ServiceRecord> records;
        if (userId != null) {
            records = serviceRecordService.getUserRecords(userId);
        } else {
            records = serviceRecordService.list();
        }

        List<Map<String,Object>> result = new ArrayList<>();
        for (ServiceRecord record : records) {
            Map<String,Object> map = new HashMap<>();
            map.put("id", record.getId());
            map.put("acceptNo", record.getAcceptNo());
            map.put("itemId", record.getItemId());
            map.put("userName", record.getUserName());
            map.put("status", record.getStatus());
            map.put("submitTime", record.getSubmitTime());
            map.put("finishTime", record.getFinishTime());
            result.add(map);
        }
        return Result.success(result);
    }

    @GetMapping("/records/{acceptNo}")
    public Result<Map<String,Object>> recordDetail(@PathVariable String acceptNo) {
        ServiceRecord record = serviceRecordService.getByAcceptNo(acceptNo);
        if (record == null) {
            return Result.error("办件记录不存在");
        }

        Map<String,Object> map = new HashMap<>();
        map.put("id", record.getId());
        map.put("acceptNo", record.getAcceptNo());
        map.put("itemId", record.getItemId());
        map.put("userName", record.getUserName());
        map.put("formData", record.getFormData());
        map.put("status", record.getStatus());
        map.put("currentStage", record.getCurrentStage());
        map.put("supplementStatus", record.getSupplementStatus());
        map.put("supplementReason", record.getSupplementReason());
        map.put("rejectReason", record.getRejectReason());
        map.put("rating", record.getRating());
        map.put("ratingContent", record.getRatingContent());
        map.put("submitTime", record.getSubmitTime());
        map.put("finishTime", record.getFinishTime());
        map.put("comment", record.getComment());
        map.put("payStatus", record.getPayStatus() != null ? record.getPayStatus() : "待支付");
        map.put("payTime", record.getPayTime());
        map.put("draft", record.getDraft());

        ServiceItem item = serviceItemService.getById(record.getItemId());
        if (item != null) {
            map.put("itemName", item.getItemName());
            map.put("category", item.getCategory());
            map.put("deptCode", item.getDeptCode());
            map.put("formSchema", item.getFormSchema());
            map.put("conditions", item.getConditions());
            map.put("timeLimit", item.getTimeLimit());
            map.put("processDesc", item.getProcessDesc());
            if (record.getPayAmount() == null && item.getPrice() != null) {
                map.put("payAmount", item.getPrice());
            } else {
                map.put("payAmount", record.getPayAmount());
            }
        } else {
            map.put("payAmount", record.getPayAmount());
        }

        // 附件列表
        List<ServiceAttachment> attachments = attachmentMapper.findByAcceptNo(acceptNo);
        List<Map<String,Object>> attList = new ArrayList<>();
        for (ServiceAttachment a : attachments) {
            Map<String,Object> am = new HashMap<>();
            am.put("id", a.getId());
            am.put("fileName", a.getFileName());
            am.put("fileType", a.getFileType());
            am.put("fileSize", a.getFileSize());
            am.put("uploadTime", a.getUploadTime());
            attList.add(am);
        }
        map.put("attachments", attList);

        return Result.success(map);
    }

    @GetMapping("/records/approval")
    public Result<List<Map<String,Object>>> approvalRecords(@RequestParam(required=false) String status, @RequestParam(required=false) String keyword) {
        List<ServiceRecord> records = serviceRecordService.list();
        
        List<Map<String,Object>> result = new ArrayList<>();
        for (ServiceRecord record : records) {
            // 过滤草稿，草稿不进入审批流程
            if (record.getDraft() != null && record.getDraft() == 1) {
                continue;
            }
            if (status != null && !status.isEmpty() && !status.equals(record.getStatus())) {
                continue;
            }
            
            Map<String,Object> map = new HashMap<>();
            map.put("id", record.getId());
            map.put("acceptNo", record.getAcceptNo());
            map.put("itemId", record.getItemId());
            map.put("userName", record.getUserName());
            map.put("formData", record.getFormData());
            map.put("status", record.getStatus());
            map.put("submitTime", record.getSubmitTime());
            map.put("finishTime", record.getFinishTime());
            map.put("comment", record.getComment());
            map.put("payStatus", record.getPayStatus() != null ? record.getPayStatus() : "待支付");
            map.put("payAmount", record.getPayAmount());
            map.put("currentStage", record.getCurrentStage());
            map.put("supplementStatus", record.getSupplementStatus());
            
            ServiceItem item = serviceItemService.getById(record.getItemId());
            if (item != null) {
                map.put("itemName", item.getItemName());
                map.put("category", item.getCategory());
                map.put("deptCode", item.getDeptCode());
                map.put("formSchema", item.getFormSchema());
                // 如果record没有设置payAmount，从事项获取
                if (record.getPayAmount() == null && item.getPrice() != null) {
                    map.put("payAmount", item.getPrice());
                }
                // 解析材料列表供前端直接展示
                if (item.getFormSchema() != null && !item.getFormSchema().isEmpty()) {
                    try {
                        Map<String,Object> schema = objectMapper.readValue(item.getFormSchema(), Map.class);
                        if (schema.containsKey("materials")) {
                            map.put("materials", schema.get("materials"));
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
            
            // 关键字搜索过滤
            if (keyword != null && !keyword.isEmpty()) {
                String kw = keyword.toLowerCase();
                boolean match = false;
                if (record.getAcceptNo() != null && record.getAcceptNo().toLowerCase().contains(kw)) match = true;
                if (record.getUserName() != null && record.getUserName().toLowerCase().contains(kw)) match = true;
                if (item != null && item.getItemName() != null && item.getItemName().toLowerCase().contains(kw)) match = true;
                if (!match) continue;
            }
            
            result.add(map);
        }
        
        result.sort((a, b) -> {
            LocalDateTime timeA = (LocalDateTime) a.get("submitTime");
            LocalDateTime timeB = (LocalDateTime) b.get("submitTime");
            return timeB.compareTo(timeA);
        });
        
        return Result.success(result);
    }

    @PostMapping("/approve/{acceptNo}")
    public Result<Map<String,Object>> approve(@PathVariable String acceptNo, @RequestBody Map<String,Object> approveData) {
        ServiceRecord record = serviceRecordService.getByAcceptNo(acceptNo);
        if (record == null) {
            return Result.error("办件记录不存在");
        }
        // 草稿不能审批
        if (record.getDraft() != null && record.getDraft() == 1) {
            return Result.error("草稿状态，无法审批");
        }

        String action = (String) approveData.get("action");
        String comment = (String) approveData.get("comment");
        Boolean completeDirectly = (Boolean) approveData.get("completeDirectly");
        String message = "操作成功";

        // 初始化currentStage
        if (record.getCurrentStage() == null || record.getCurrentStage().isEmpty()) {
            record.setCurrentStage("窗口受理");
        }

        if ("approve".equals(action)) {
            if ("已办结".equals(record.getStatus())) {
                return Result.error("该办件已办结，无法重复审批");
            }
            if (completeDirectly != null && completeDirectly) {
                record.setStatus("已办结");
                record.setFinishTime(LocalDateTime.now());
                record.setCurrentStage("已办结");
            } else {
                // 多阶段审批流程：窗口受理 → 材料初审 → 实质审核 → 领导审批 → 已办结
                String stage = record.getCurrentStage();
                switch (stage) {
                    case "窗口受理":
                        record.setCurrentStage("材料初审");
                        record.setStatus("审核中");
                        message = "已受理，进入材料初审阶段";
                        break;
                    case "材料初审":
                        record.setCurrentStage("实质审核");
                        message = "材料初审通过，进入实质审核阶段";
                        break;
                    case "实质审核":
                        record.setCurrentStage("领导审批");
                        message = "实质审核通过，进入领导审批阶段";
                        break;
                    case "领导审批":
                        // 领导审批通过后检查缴费状态
                        boolean paid = "已支付".equals(record.getPayStatus());
                        java.math.BigDecimal actualAmount = record.getPayAmount();
                        if (actualAmount == null) {
                            ServiceItem priceItem = serviceItemService.getById(record.getItemId());
                            if (priceItem != null && priceItem.getPrice() != null) {
                                actualAmount = priceItem.getPrice();
                            }
                        }
                        boolean free = actualAmount == null || actualAmount.compareTo(java.math.BigDecimal.ZERO) <= 0;
                        if (paid || free) {
                            record.setStatus("已办结");
                            record.setFinishTime(LocalDateTime.now());
                            record.setCurrentStage("已办结");
                            message = "领导审批通过，办件已办结";
                        } else {
                            record.setCurrentStage("待缴费办结");
                            message = "领导审批已通过，但该事项需缴费（¥" + actualAmount + "），请等待申请人完成缴费后自动办结";
                        }
                        break;
                    case "待缴费办结":
                        // 缴费完成后办结
                        if ("已支付".equals(record.getPayStatus())) {
                            record.setStatus("已办结");
                            record.setFinishTime(LocalDateTime.now());
                            record.setCurrentStage("已办结");
                            message = "缴费完成，办件已办结";
                        }
                        break;
                    default:
                        // 兼容旧状态
                        if ("提交申请".equals(record.getStatus()) || "已驳回".equals(record.getStatus())) {
                            record.setStatus("审核中");
                            record.setCurrentStage("材料初审");
                        } else if ("审核中".equals(record.getStatus())) {
                            record.setStatus("已审批");
                            record.setCurrentStage("领导审批");
                        } else if ("已审批".equals(record.getStatus())) {
                            record.setStatus("已办结");
                            record.setFinishTime(LocalDateTime.now());
                            record.setCurrentStage("已办结");
                        }
                        break;
                }
            }
        } else if ("reject".equals(action)) {
            if ("已办结".equals(record.getStatus())) {
                return Result.error("该办件已办结，无法驳回");
            }
            if ("已驳回".equals(record.getStatus())) {
                return Result.error("该办件已被驳回，无法重复驳回");
            }
            record.setStatus("已驳回");
            String rejectReason = (String) approveData.get("rejectReason");
            if (rejectReason != null) {
                record.setRejectReason(rejectReason);
            }
        } else if ("return".equals(action)) {
            // 窗口受理不通过，退回申请
            record.setStatus("已驳回");
            String returnReason = (String) approveData.get("rejectReason");
            if (returnReason != null) {
                record.setRejectReason(returnReason);
            }
            message = "已退回申请";
        }

        if (comment != null) {
            record.setComment(comment);
        }

        serviceRecordService.updateById(record);

        Map<String,Object> result = new HashMap<>();
        result.put("acceptNo", record.getAcceptNo());
        result.put("status", record.getStatus());
        result.put("currentStage", record.getCurrentStage());
        return Result.success(result, message);
    }

    @PostMapping("/batch-approve")
    public Result<List<Map<String,Object>>> batchApprove(@RequestBody Map<String,Object> data) {
        List<String> acceptNos = (List<String>) data.get("acceptNos");
        String action = (String) data.get("action");
        
        List<Map<String,Object>> results = new ArrayList<>();
        for (String acceptNo : acceptNos) {
            ServiceRecord record = serviceRecordService.getByAcceptNo(acceptNo);
            if (record != null) {
                // 跳过草稿
                if (record.getDraft() != null && record.getDraft() == 1) {
                    continue;
                }
                if ("approve".equals(action)) {
                    if ("已办结".equals(record.getStatus())) {
                        continue;
                    }
                    if ("提交申请".equals(record.getStatus()) || "已驳回".equals(record.getStatus())) {
                        record.setStatus("审核中");
                    } else if ("审核中".equals(record.getStatus())) {
                        record.setStatus("已审批");
                    } else if ("已审批".equals(record.getStatus())) {
                        // 审批完成后检查缴费状态
                        boolean paid = "已支付".equals(record.getPayStatus());
                        java.math.BigDecimal batchAmount = record.getPayAmount();
                        if (batchAmount == null) {
                            ServiceItem batchItem = serviceItemService.getById(record.getItemId());
                            if (batchItem != null && batchItem.getPrice() != null) {
                                batchAmount = batchItem.getPrice();
                            }
                        }
                        boolean free = batchAmount == null || batchAmount.compareTo(java.math.BigDecimal.ZERO) <= 0;
                        if (paid || free) {
                            record.setStatus("已办结");
                            record.setFinishTime(LocalDateTime.now());
                        }
                        // 未缴费且需要缴费，保持已审批状态
                    }
                } else if ("reject".equals(action)) {
                    if ("已办结".equals(record.getStatus()) || "已驳回".equals(record.getStatus())) {
                        continue;
                    }
                    record.setStatus("已驳回");
                }
                serviceRecordService.updateById(record);
                
                Map<String,Object> result = new HashMap<>();
                result.put("acceptNo", record.getAcceptNo());
                result.put("status", record.getStatus());
                results.add(result);
            }
        }
        
        return Result.success(results, "批量操作成功");
    }

    @PostMapping("/inquiry")
    public Result<Map<String,Object>> inquiry(@RequestBody Map<String,Object> data) {
        String acceptNo = (String) data.get("acceptNo");
        String content = (String) data.get("content");
        
        ServiceRecord record = serviceRecordService.getByAcceptNo(acceptNo);
        if (record == null) {
            return Result.error("办件记录不存在");
        }

        ServiceInquiry inquiry = new ServiceInquiry();
        inquiry.setAcceptNo(acceptNo);
        inquiry.setContent(content);
        inquiry.setCreateTime(LocalDateTime.now());
        serviceInquiryMapper.insert(inquiry);

        Map<String,Object> result = new HashMap<>();
        result.put("id", inquiry.getId());
        result.put("acceptNo", inquiry.getAcceptNo());
        result.put("content", inquiry.getContent());
        result.put("reply", inquiry.getReply());
        result.put("createTime", inquiry.getCreateTime());
        return Result.success(result, "询问已提交");
    }

    @GetMapping("/inquiries/{acceptNo}")
    public Result<List<Map<String,Object>>> getInquiries(@PathVariable String acceptNo) {
        List<ServiceInquiry> inquiries = serviceInquiryMapper.selectByAcceptNo(acceptNo);
        
        List<Map<String,Object>> result = new ArrayList<>();
        for (ServiceInquiry inquiry : inquiries) {
            Map<String,Object> map = new HashMap<>();
            map.put("id", inquiry.getId());
            map.put("acceptNo", inquiry.getAcceptNo());
            map.put("content", inquiry.getContent());
            map.put("reply", inquiry.getReply());
            map.put("createTime", inquiry.getCreateTime());
            map.put("replyTime", inquiry.getReplyTime());
            result.add(map);
        }
        return Result.success(result);
    }

    @GetMapping("/inquiries")
    public Result<List<Map<String,Object>>> getAllInquiries() {
        List<ServiceInquiry> inquiries = serviceInquiryMapper.selectList(null);
        
        List<Map<String,Object>> result = new ArrayList<>();
        for (ServiceInquiry inquiry : inquiries) {
            Map<String,Object> map = new HashMap<>();
            map.put("id", inquiry.getId());
            map.put("acceptNo", inquiry.getAcceptNo());
            map.put("content", inquiry.getContent());
            map.put("reply", inquiry.getReply());
            map.put("createTime", inquiry.getCreateTime());
            map.put("replyTime", inquiry.getReplyTime());
            
            ServiceRecord record = serviceRecordService.getByAcceptNo(inquiry.getAcceptNo());
            if (record != null) {
                map.put("userName", record.getUserName());
                map.put("status", record.getStatus());
            }
            result.add(map);
        }
        
        result.sort((a, b) -> {
            LocalDateTime timeA = (LocalDateTime) a.get("createTime");
            LocalDateTime timeB = (LocalDateTime) b.get("createTime");
            return timeB.compareTo(timeA);
        });
        
        return Result.success(result);
    }

    @PostMapping("/inquiry/{inquiryId}/reply")
    public Result<Map<String,Object>> replyInquiry(@PathVariable Long inquiryId, @RequestBody Map<String,Object> data) {
        String reply = (String) data.get("reply");
        
        ServiceInquiry inquiry = serviceInquiryMapper.selectById(inquiryId);
        if (inquiry == null) {
            return Result.error("询问记录不存在");
        }
        
        inquiry.setReply(reply);
        inquiry.setReplyTime(LocalDateTime.now());
        serviceInquiryMapper.updateById(inquiry);

        Map<String,Object> result = new HashMap<>();
        result.put("id", inquiry.getId());
        result.put("acceptNo", inquiry.getAcceptNo());
        result.put("content", inquiry.getContent());
        result.put("reply", inquiry.getReply());
        result.put("createTime", inquiry.getCreateTime());
        result.put("replyTime", inquiry.getReplyTime());
        return Result.success(result, "回复成功");
    }

    // ===== 草稿列表 =====
    @GetMapping("/drafts")
    public Result<List<Map<String,Object>>> getDrafts(@RequestAttribute("jwtToken") String token) {
        Long userId = com.gov.common.utils.JwtUtil.getUserIdFromToken(token);
        List<ServiceRecord> records = serviceRecordService.listByUserId(userId);
        List<Map<String,Object>> result = new ArrayList<>();
        for (ServiceRecord r : records) {
            if (r.getDraft() != null && r.getDraft() == 1) {
                Map<String,Object> map = new HashMap<>();
                map.put("id", r.getId());
                map.put("acceptNo", r.getAcceptNo());
                map.put("itemId", r.getItemId());
                map.put("formData", r.getFormData());
                map.put("submitTime", r.getSubmitTime());
                ServiceItem item = serviceItemService.getById(r.getItemId());
                if (item != null) map.put("itemName", item.getItemName());
                result.add(map);
            }
        }
        return Result.success(result);
    }

    // ===== 删除草稿 =====
    @DeleteMapping("/drafts/{acceptNo}")
    public Result<String> deleteDraft(@RequestAttribute("jwtToken") String token, @PathVariable String acceptNo) {
        Long userId = com.gov.common.utils.JwtUtil.getUserIdFromToken(token);
        ServiceRecord record = serviceRecordService.getByAcceptNo(acceptNo);
        if (record == null) {
            return Result.error("草稿不存在");
        }
        if (!userId.equals(record.getUserId())) {
            return Result.error("无权删除此草稿");
        }
        if (record.getDraft() == null || record.getDraft() != 1) {
            return Result.error("只能删除草稿状态的记录");
        }
        serviceRecordService.removeById(record.getId());
        return Result.success("草稿已删除");
    }

    // ===== 文件上传 =====
    @PostMapping("/upload")
    public Result<Map<String,Object>> uploadFile(@RequestParam("file") MultipartFile file,
                                                  @RequestParam("acceptNo") String acceptNo) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        ServiceRecord record = serviceRecordService.getByAcceptNo(acceptNo);
        if (record == null) {
            return Result.error("办件记录不存在");
        }
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        }
        String[] allowedExts = {".pdf", ".jpg", ".jpeg", ".png"};
        boolean allowed = false;
        for (String e : allowedExts) { if (ext.equals(e)) { allowed = true; break; } }
        if (!allowed) {
            return Result.error("仅支持PDF/JPG/PNG格式");
        }
        try {
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();
            String savedName = System.currentTimeMillis() + "_" + (int)(Math.random()*10000) + ext;
            File dest = new File(dir, savedName);
            file.transferTo(dest);

            ServiceAttachment att = new ServiceAttachment();
            att.setAcceptNo(acceptNo);
            att.setFileName(originalName);
            att.setFilePath(dest.getAbsolutePath());
            att.setFileType(ext.replace(".", "").toUpperCase());
            att.setFileSize(file.getSize());
            att.setUploadTime(LocalDateTime.now());
            attachmentMapper.insert(att);

            Map<String,Object> result = new HashMap<>();
            result.put("id", att.getId());
            result.put("fileName", originalName);
            result.put("fileType", att.getFileType());
            result.put("fileSize", file.getSize());
            result.put("uploadTime", att.getUploadTime());
            return Result.success(result, "上传成功");
        } catch (IOException e) {
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }

    // ===== 附件列表 =====
    @GetMapping("/attachments/{acceptNo}")
    public Result<List<Map<String,Object>>> getAttachments(@PathVariable String acceptNo) {
        List<ServiceAttachment> attachments = attachmentMapper.findByAcceptNo(acceptNo);
        List<Map<String,Object>> result = new ArrayList<>();
        for (ServiceAttachment a : attachments) {
            Map<String,Object> map = new HashMap<>();
            map.put("id", a.getId());
            map.put("fileName", a.getFileName());
            map.put("fileType", a.getFileType());
            map.put("fileSize", a.getFileSize());
            map.put("uploadTime", a.getUploadTime());
            result.add(map);
        }
        return Result.success(result);
    }

    // ===== 补充材料通知 =====
    @PostMapping("/supplement/{acceptNo}")
    public Result<Map<String,Object>> requestSupplement(@PathVariable String acceptNo, @RequestBody Map<String,Object> data) {
        ServiceRecord record = serviceRecordService.getByAcceptNo(acceptNo);
        if (record == null) {
            return Result.error("办件记录不存在");
        }
        String reason = (String) data.get("reason");
        if (reason == null || reason.trim().isEmpty()) {
            return Result.error("请说明需要补充的材料");
        }
        record.setSupplementReason(reason);
        record.setSupplementStatus("待补充");
        record.setComment("【补充材料通知】" + reason);
        serviceRecordService.updateById(record);

        Map<String,Object> result = new HashMap<>();
        result.put("acceptNo", acceptNo);
        result.put("supplementStatus", "待补充");
        return Result.success(result, "已通知申请人补充材料");
    }

    // ===== 申请人确认补充完成 =====
    @PostMapping("/supplement-done/{acceptNo}")
    public Result<Map<String,Object>> supplementDone(@PathVariable String acceptNo) {
        ServiceRecord record = serviceRecordService.getByAcceptNo(acceptNo);
        if (record == null) {
            return Result.error("办件记录不存在");
        }
        record.setSupplementStatus("已补充");
        serviceRecordService.updateById(record);

        Map<String,Object> result = new HashMap<>();
        result.put("acceptNo", acceptNo);
        result.put("supplementStatus", "已补充");
        return Result.success(result, "材料已重新提交");
    }

    // ===== 服务评价 =====
    @PostMapping("/rating")
    public Result<Map<String,Object>> submitRating(@RequestAttribute("jwtToken") String token, @RequestBody Map<String,Object> data) {
        Long userId = com.gov.common.utils.JwtUtil.getUserIdFromToken(token);
        String acceptNo = (String) data.get("acceptNo");
        Object ratingObj = data.get("rating");
        String content = (String) data.get("content");

        if (acceptNo == null || ratingObj == null) {
            return Result.error("受理号和评分不能为空");
        }
        int rating;
        if (ratingObj instanceof Number) {
            rating = ((Number) ratingObj).intValue();
        } else {
            rating = Integer.parseInt(ratingObj.toString());
        }
        if (rating < 1 || rating > 5) {
            return Result.error("评分范围为1-5");
        }

        ServiceRecord record = serviceRecordService.getByAcceptNo(acceptNo);
        if (record == null) {
            return Result.error("办件记录不存在");
        }

        // 检查是否已评价
        if (record.getRating() != null) {
            return Result.error("该办件已评价，不可重复评价");
        }

        // 同时更新record的评价字段
        record.setRating(rating);
        record.setRatingContent(content);
        serviceRecordService.updateById(record);

        ServiceRating sr = new ServiceRating();
        sr.setAcceptNo(acceptNo);
        sr.setUserId(userId);
        sr.setRating(rating);
        sr.setContent(content);
        sr.setCreateTime(LocalDateTime.now());
        ratingMapper.insert(sr);

        Map<String,Object> result = new HashMap<>();
        result.put("id", sr.getId());
        result.put("rating", rating);
        return Result.success(result, "评价成功");
    }

    @GetMapping("/ratings")
    public Result<List<Map<String,Object>>> getAllRatings() {
        List<ServiceRating> ratings = ratingMapper.selectList(null);
        List<Map<String,Object>> list = new java.util.ArrayList<>();
        for (ServiceRating sr : ratings) {
            Map<String,Object> map = new HashMap<>();
            map.put("id", sr.getId());
            map.put("acceptNo", sr.getAcceptNo());
            map.put("userId", sr.getUserId());
            map.put("rating", sr.getRating());
            map.put("content", sr.getContent());
            map.put("createTime", sr.getCreateTime());
            // 查询关联的办件记录获取事项名称和申请人
            ServiceRecord record = serviceRecordService.getByAcceptNo(sr.getAcceptNo());
            if (record != null) {
                map.put("userName", record.getUserName());
                map.put("itemName", getItemNameById(record.getItemId()));
            }
            list.add(map);
        }
        return Result.success(list);
    }

    private String getItemNameById(Long itemId) {
        if (itemId == null) return "未知事项";
        try {
            com.gov.service.entity.ServiceItem item = serviceItemService.getById(itemId);
            return item != null ? item.getItemName() : "未知事项";
        } catch (Exception e) {
            return "未知事项";
        }
    }
}