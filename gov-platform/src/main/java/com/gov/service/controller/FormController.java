package com.gov.service.controller;

import com.gov.common.result.Result;
import com.gov.service.entity.ServiceItem;
import com.gov.service.entity.ServiceRecord;
import com.gov.service.entity.ServiceInquiry;
import com.gov.service.service.ServiceItemService;
import com.gov.service.service.ServiceRecordService;
import com.gov.service.mapper.ServiceInquiryMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
            } catch (JsonProcessingException e) {
                schema = getDefaultSchema(item.getItemName());
            }
        } else {
            schema = getDefaultSchema(item.getItemName());
        }
        return Result.success(schema);
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
    public Result<Map<String,Object>> submit(@RequestBody Map<String,Object> formData) {
        if (formData == null) {
            return Result.error("表单数据不能为空");
        }
        
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
            formDataJson = objectMapper.writeValueAsString(formData);
        } catch (JsonProcessingException e) {
            return Result.error("表单数据序列化失败");
        }

        ServiceRecord record = serviceRecordService.createRecord(itemId, formDataJson, userName);

        Map<String,Object> result = new HashMap<>();
        result.put("acceptNo", record.getAcceptNo());
        result.put("status", record.getStatus());
        result.put("submitTime", record.getSubmitTime());
        return Result.success(result, "提交成功，受理号：" + record.getAcceptNo());
    }

    @GetMapping("/progress/{acceptNo}")
    public Result<List<Map<String,Object>>> progress(@PathVariable String acceptNo) {
        ServiceRecord record = serviceRecordService.getByAcceptNo(acceptNo);
        if (record == null) {
            return Result.error("办件记录不存在");
        }

        List<Map<String,Object>> progressList = new ArrayList<>();

        Map<String,Object> step1 = new HashMap<>();
        step1.put("step", 1);
        step1.put("title", "提交申请");
        step1.put("desc", "申请已提交");
        step1.put("time", record.getSubmitTime());
        step1.put("completed", true);
        progressList.add(step1);

        Map<String,Object> step2 = new HashMap<>();
        step2.put("step", 2);
        step2.put("title", "材料审核");
        step2.put("desc", "审核员正在审核材料");
        step2.put("completed", "审核中".equals(record.getStatus()) || "已审批".equals(record.getStatus()) || "已办结".equals(record.getStatus()));
        progressList.add(step2);

        Map<String,Object> step3 = new HashMap<>();
        step3.put("step", 3);
        step3.put("title", "审批决定");
        step3.put("desc", "领导审批中");
        step3.put("completed", "已审批".equals(record.getStatus()) || "已办结".equals(record.getStatus()));
        progressList.add(step3);

        Map<String,Object> step4 = new HashMap<>();
        step4.put("step", 4);
        step4.put("title", "办结通知");
        step4.put("desc", "办理完成，等待领取证照");
        step4.put("completed", "已办结".equals(record.getStatus()));
        if ("已办结".equals(record.getStatus()) && record.getFinishTime() != null) {
            step4.put("time", record.getFinishTime());
        }
        progressList.add(step4);

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
        map.put("submitTime", record.getSubmitTime());
        map.put("finishTime", record.getFinishTime());

        ServiceItem item = serviceItemService.getById(record.getItemId());
        if (item != null) {
            map.put("itemName", item.getItemName());
        }

        return Result.success(map);
    }

    @GetMapping("/records/approval")
    public Result<List<Map<String,Object>>> approvalRecords(@RequestParam(required=false) String status) {
        List<ServiceRecord> records = serviceRecordService.list();
        
        List<Map<String,Object>> result = new ArrayList<>();
        for (ServiceRecord record : records) {
            if (status != null && !status.isEmpty() && !status.equals(record.getStatus())) {
                continue;
            }
            
            Map<String,Object> map = new HashMap<>();
            map.put("id", record.getId());
            map.put("acceptNo", record.getAcceptNo());
            map.put("itemId", record.getItemId());
            map.put("userName", record.getUserName());
            map.put("status", record.getStatus());
            map.put("submitTime", record.getSubmitTime());
            map.put("finishTime", record.getFinishTime());
            
            ServiceItem item = serviceItemService.getById(record.getItemId());
            if (item != null) {
                map.put("itemName", item.getItemName());
                map.put("category", item.getCategory());
                map.put("deptCode", item.getDeptCode());
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

        String action = (String) approveData.get("action");
        String comment = (String) approveData.get("comment");
        Boolean completeDirectly = (Boolean) approveData.get("completeDirectly");

        if ("approve".equals(action)) {
            if ("已办结".equals(record.getStatus())) {
                return Result.error("该办件已办结，无法重复审批");
            }
            if (completeDirectly != null && completeDirectly) {
                record.setStatus("已办结");
                record.setFinishTime(LocalDateTime.now());
            } else {
                if ("提交申请".equals(record.getStatus()) || "已驳回".equals(record.getStatus())) {
                    record.setStatus("审核中");
                } else if ("审核中".equals(record.getStatus())) {
                    record.setStatus("已审批");
                } else if ("已审批".equals(record.getStatus())) {
                    record.setStatus("已办结");
                    record.setFinishTime(LocalDateTime.now());
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
        }

        if (comment != null) {
            record.setComment(comment);
        }

        serviceRecordService.updateById(record);

        Map<String,Object> result = new HashMap<>();
        result.put("acceptNo", record.getAcceptNo());
        result.put("status", record.getStatus());
        return Result.success(result, "操作成功");
    }

    @PostMapping("/batch-approve")
    public Result<List<Map<String,Object>>> batchApprove(@RequestBody Map<String,Object> data) {
        List<String> acceptNos = (List<String>) data.get("acceptNos");
        String action = (String) data.get("action");
        
        List<Map<String,Object>> results = new ArrayList<>();
        for (String acceptNo : acceptNos) {
            ServiceRecord record = serviceRecordService.getByAcceptNo(acceptNo);
            if (record != null) {
                if ("approve".equals(action)) {
                    if ("已办结".equals(record.getStatus())) {
                        continue;
                    }
                    if ("提交申请".equals(record.getStatus()) || "已驳回".equals(record.getStatus())) {
                        record.setStatus("审核中");
                    } else if ("审核中".equals(record.getStatus())) {
                        record.setStatus("已审批");
                    } else if ("已审批".equals(record.getStatus())) {
                        record.setStatus("已办结");
                        record.setFinishTime(LocalDateTime.now());
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
}