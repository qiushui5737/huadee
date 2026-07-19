package com.gov.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gov.service.entity.ServiceItem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ServiceItemMapper extends BaseMapper<ServiceItem> {

    @Select("SELECT * FROM service_item WHERE item_code = #{itemCode} AND deleted = 0 AND status = 1")
    ServiceItem findByItemCode(String itemCode);

    @Delete("DELETE FROM service_item")
    void physicalDeleteAll();
}