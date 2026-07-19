package com.gov.interaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gov.interaction.entity.MessageReply;
import org.apache.ibatis.annotations.Mapper;

/**
 * 留言回复Mapper
 */
@Mapper
public interface MessageReplyMapper extends BaseMapper<MessageReply> {
}
