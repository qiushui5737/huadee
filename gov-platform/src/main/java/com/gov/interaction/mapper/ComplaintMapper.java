package com.gov.interaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gov.interaction.entity.Complaint;
import org.apache.ibatis.annotations.Mapper;

/**
 * 投诉Mapper
 */
@Mapper
public interface ComplaintMapper extends BaseMapper<Complaint> {
}
