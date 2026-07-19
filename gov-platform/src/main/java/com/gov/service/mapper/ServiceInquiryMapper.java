package com.gov.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gov.service.entity.ServiceInquiry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ServiceInquiryMapper extends BaseMapper<ServiceInquiry> {

    @Select("SELECT * FROM service_inquiry WHERE accept_no = #{acceptNo} ORDER BY create_time DESC")
    List<ServiceInquiry> selectByAcceptNo(String acceptNo);
}