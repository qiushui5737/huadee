package com.gov.service.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gov.service.entity.ServiceItem;
import com.gov.service.mapper.ServiceItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceItemService {

    @Autowired
    private ServiceItemMapper serviceItemMapper;

    public List<ServiceItem> list() {
        return serviceItemMapper.selectList(new QueryWrapper<ServiceItem>().eq("status", 1));
    }

    public List<ServiceItem> listByCategory(String category) {
        QueryWrapper<ServiceItem> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        if (category != null && !category.isEmpty()) {
            wrapper.eq("category", category);
        }
        return serviceItemMapper.selectList(wrapper);
    }

    public List<ServiceItem> listByDeptCode(String deptCode) {
        QueryWrapper<ServiceItem> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        if (deptCode != null && !deptCode.isEmpty()) {
            wrapper.eq("dept_code", deptCode);
        }
        return serviceItemMapper.selectList(wrapper);
    }

    public ServiceItem getById(Long id) {
        return serviceItemMapper.selectById(id);
    }

    public ServiceItem getByItemCode(String itemCode) {
        return serviceItemMapper.findByItemCode(itemCode);
    }

    public boolean save(ServiceItem item) {
        return serviceItemMapper.insert(item) > 0;
    }

    public boolean updateById(ServiceItem item) {
        return serviceItemMapper.updateById(item) > 0;
    }

    public boolean removeById(Long id) {
        return serviceItemMapper.deleteById(id) > 0;
    }

    public ServiceItem getItemById(Long id) {
        return getById(id);
    }
}
