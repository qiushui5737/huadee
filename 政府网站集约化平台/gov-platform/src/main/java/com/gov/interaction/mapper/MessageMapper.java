package com.gov.interaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gov.interaction.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * 留言Mapper
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
