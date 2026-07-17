package com.gov.admin.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gov.admin.entity.SysUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import java.util.List;
public interface SysUserMapper extends BaseMapper<SysUser> {
    @Select("SELECT r.role_code FROM sys_role r JOIN sys_user_role ur ON ur.role_id=r.id WHERE ur.user_id=#{userId}")
    List<String> selectRoleCodes(@Param("userId") Long userId);

    @Select("SELECT id FROM sys_role WHERE role_code=#{roleCode} LIMIT 1")
    Long selectRoleId(@Param("roleCode") String roleCode);

    @Insert("INSERT INTO sys_user_role(user_id, role_id) VALUES(#{userId}, #{roleId})")
    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
