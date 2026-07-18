package com.gov.common.config;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gov.admin.entity.SysUser;
import com.gov.admin.mapper.SysUserMapper;
import com.gov.service.entity.ServiceItem;
import com.gov.service.mapper.ServiceItemMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper userMapper;
    private final ServiceItemMapper serviceItemMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public DataInitializer(SysUserMapper userMapper, ServiceItemMapper serviceItemMapper) {
        this.userMapper = userMapper;
        this.serviceItemMapper = serviceItemMapper;
    }

    @Override
    public void run(String... args) {
        updatePasswordIfInvalid("admin");
        updatePasswordIfInvalid("edu_user");
        updatePasswordIfInvalid("hea_user");
        updatePasswordIfInvalid("pol_user");
        updatePasswordIfInvalid("soc_user");
        updatePasswordIfInvalid("med_user");
        initServiceItems();
    }

    private void updatePasswordIfInvalid(String username) {
        SysUser user = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, username));
        if (user != null) {
            user.setPassword(passwordEncoder.encode("123456"));
            userMapper.updateById(user);
            System.out.println("Updated password for user: " + username);
        }
    }

    private void initServiceItems() {
        serviceItemMapper.delete(null);
        serviceItemMapper.physicalDeleteAll();
        if (serviceItemMapper.selectCount(null) > 0) {
            System.out.println("Service items already exist, skipping initialization");
            return;
        }
        
        List<ServiceItem> items = new ArrayList<>();
        
        items.add(createItem("EDU-001", "义务教育入学登记", "education", "EDU", "为适龄儿童办理义务教育阶段入学登记"));
        items.add(createItem("EDU-002", "转学申请", "education", "EDU", "办理中小学生转学手续"));
        items.add(createItem("EDU-003", "学籍档案查询", "education", "EDU", "查询个人学籍档案信息"));
        items.add(createItem("EDU-004", "学历认证", "education", "EDU", "办理学历证书认证"));
        
        items.add(createItem("HOU-001", "公共租赁住房申请", "housing", "HOU", "申请公共租赁住房保障"));
        items.add(createItem("HOU-002", "住房公积金提取", "housing", "HOU", "办理住房公积金提取业务"));
        items.add(createItem("HOU-003", "商品房预售备案", "housing", "HOU", "商品房预售合同备案登记"));
        items.add(createItem("HOU-004", "经济适用房申请", "housing", "HOU", "申请经济适用住房"));
        
        items.add(createItem("HEA-001", "医疗费用报销", "health", "MED", "办理医保医疗费用报销"));
        items.add(createItem("HEA-002", "医保定点医院变更", "health", "MED", "变更个人医保定点医院"));
        items.add(createItem("HEA-003", "电子健康卡申领", "health", "MED", "申领电子健康卡"));
        items.add(createItem("HEA-004", "家庭医生签约", "health", "MED", "签订家庭医生服务协议"));
        
        items.add(createItem("EMP-001", "就业创业证办理", "employment", "EMP", "办理就业创业登记证"));
        items.add(createItem("EMP-002", "创业补贴申请", "employment", "EMP", "申请创业扶持补贴"));
        items.add(createItem("EMP-003", "职业技能培训补贴", "employment", "EMP", "申请职业技能培训补贴"));
        items.add(createItem("EMP-004", "失业登记", "employment", "EMP", "办理失业登记手续"));
        
        items.add(createItem("SOC-001", "社会保险参保登记", "social", "SOC", "办理城乡居民社保参保"));
        items.add(createItem("SOC-002", "社保卡挂失补办", "social", "SOC", "社保卡挂失及补办"));
        items.add(createItem("SOC-003", "养老保险转移", "social", "SOC", "办理养老保险关系转移"));
        items.add(createItem("SOC-004", "工伤保险待遇申领", "social", "SOC", "申请工伤保险待遇"));
        
        items.add(createItem("TRA-001", "机动车驾驶证申领", "traffic", "TRA", "初次申领机动车驾驶证"));
        items.add(createItem("TRA-002", "车辆年检预约", "traffic", "TRA", "预约机动车年检服务"));
        items.add(createItem("TRA-003", "道路运输证办理", "traffic", "TRA", "办理道路运输经营许可证"));
        items.add(createItem("TRA-004", "网约车驾驶员证", "traffic", "TRA", "申请网络预约出租汽车驾驶员证"));
        
        items.add(createItem("TAX-001", "增值税一般纳税人登记", "tax", "TAX", "办理增值税一般纳税人资格登记"));
        items.add(createItem("TAX-002", "个人所得税汇算清缴", "tax", "TAX", "办理个人所得税年度汇算"));
        items.add(createItem("TAX-003", "发票领用", "tax", "TAX", "领用增值税发票"));
        items.add(createItem("TAX-004", "税收减免申请", "tax", "TAX", "申请税收优惠减免"));
        
        items.add(createItem("CER-001", "居民身份证办理", "certificate", "POL", "办理居民身份证"));
        items.add(createItem("CER-002", "户口迁移", "certificate", "POL", "办理户口迁移手续"));
        items.add(createItem("CER-003", "护照申请", "certificate", "POL", "申请普通护照"));
        items.add(createItem("CER-004", "不动产权证办理", "certificate", "POL", "办理不动产权证书"));
        
        for (ServiceItem item : items) {
            serviceItemMapper.insert(item);
        }
        System.out.println("Initialized " + items.size() + " service items");
    }

    private ServiceItem createItem(String code, String name, String category, String deptCode, String description) {
        ServiceItem item = new ServiceItem();
        item.setItemCode(code);
        item.setItemName(name);
        item.setCategory(category);
        item.setDeptCode(deptCode);
        item.setDescription(description);
        item.setStatus(1);
        return item;
    }
}