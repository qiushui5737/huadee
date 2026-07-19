package com.gov.interaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gov.interaction.entity.Suggestion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 建议Mapper
 */
@Mapper
public interface SuggestionMapper extends BaseMapper<Suggestion> {
}
