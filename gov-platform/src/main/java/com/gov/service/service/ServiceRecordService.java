package com.gov.service.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gov.service.entity.ServiceRecord;
import com.gov.service.mapper.ServiceRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceRecordService {

    @Autowired
    private ServiceRecordMapper serviceRecordMapper;

    public List<ServiceRecord> list() {
        return serviceRecordMapper.selectList(new QueryWrapper<ServiceRecord>().orderByDesc("submit_time"));
    }

    public List<ServiceRecord> listByStatus(String status) {
        if (status == null || status.isEmpty()) {
            return list();
        }
        return serviceRecordMapper.findByStatus(status);
    }

    public List<ServiceRecord> listByUserId(Long userId) {
        return serviceRecordMapper.findByUserId(userId);
    }

    public ServiceRecord getById(Long id) {
        return serviceRecordMapper.selectById(id);
    }

    public ServiceRecord getByAcceptNo(String acceptNo) {
        return serviceRecordMapper.findByAcceptNo(acceptNo);
    }

    public boolean save(ServiceRecord record) {
        return serviceRecordMapper.insert(record) > 0;
    }

    public boolean updateById(ServiceRecord record) {
        return serviceRecordMapper.updateById(record) > 0;
    }

    public boolean removeById(Long id) {
        return serviceRecordMapper.deleteById(id) > 0;
    }

    public List<ServiceRecord> getUserRecords(Long userId) {
        return listByUserId(userId);
    }

    public ServiceRecord createRecord(Long itemId, String userName, String formData) {
        ServiceRecord record = new ServiceRecord();
        record.setItemId(itemId);
        record.setUserName(userName);
        record.setFormData(formData);
        record.setStatus("提交申请");
        String acceptNo = "SL" + System.currentTimeMillis() + (int)(Math.random() * 1000);
        record.setAcceptNo(acceptNo);
        record.setSubmitTime(java.time.LocalDateTime.now());
        record.setPayStatus("待支付");
        record.setLicenseStatus("未领取");
        save(record);
        return record;
    }
}