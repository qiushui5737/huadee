package com.gov.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gov.service.entity.ServiceRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ServiceRecordMapper extends BaseMapper<ServiceRecord> {

    @Select("SELECT * FROM service_record WHERE accept_no = #{acceptNo}")
    ServiceRecord findByAcceptNo(String acceptNo);

    @Select("SELECT * FROM service_record WHERE user_id = #{userId} ORDER BY submit_time DESC")
    List<ServiceRecord> findByUserId(Long userId);

    @Select("SELECT * FROM service_record WHERE status = #{status} ORDER BY submit_time DESC")
    List<ServiceRecord> findByStatus(String status);
}