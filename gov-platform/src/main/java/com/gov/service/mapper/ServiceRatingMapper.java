package com.gov.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gov.service.entity.ServiceRating;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ServiceRatingMapper extends BaseMapper<ServiceRating> {

    @Select("SELECT * FROM service_rating WHERE accept_no = #{acceptNo}")
    List<ServiceRating> findByAcceptNo(String acceptNo);
}
