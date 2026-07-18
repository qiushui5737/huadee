package com.gov.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gov.admin.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
    SysUser findByUsername(String username);
}