package com.gov.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gov.service.entity.ServiceAttachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ServiceAttachmentMapper extends BaseMapper<ServiceAttachment> {

    @Select("SELECT * FROM service_attachment WHERE accept_no = #{acceptNo}")
    List<ServiceAttachment> findByAcceptNo(String acceptNo);
}
