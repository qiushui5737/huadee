package com.gov.common.config;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gov.admin.entity.SysUser;
import com.gov.admin.mapper.SysUserMapper;
import com.gov.service.entity.ServiceItem;
import com.gov.service.entity.ServiceRecord;
import com.gov.service.mapper.ServiceItemMapper;
import com.gov.service.mapper.ServiceRecordMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper userMapper;
    private final ServiceItemMapper serviceItemMapper;
    private final ServiceRecordMapper recordMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public DataInitializer(SysUserMapper userMapper, ServiceItemMapper serviceItemMapper, ServiceRecordMapper recordMapper) {
        this.userMapper = userMapper;
        this.serviceItemMapper = serviceItemMapper;
        this.recordMapper = recordMapper;
    }

    @Override
    public void run(String... args) {
        // 密码初始化已禁用，避免每次启动覆盖用户密码
        // updatePasswordIfInvalid("admin");
        // updatePasswordIfInvalid("edu_user");
        // updatePasswordIfInvalid("hea_user");
        // updatePasswordIfInvalid("pol_user");
        // updatePasswordIfInvalid("soc_user");
        // updatePasswordIfInvalid("med_user");
        // 只在无数据时初始化，避免重启丢失用户数据
        if (serviceItemMapper.selectCount(null) == 0) {
            initServiceItems();
        } else {
            System.out.println("Service items already exist (" + serviceItemMapper.selectCount(null) + " items), skipping initialization");
        }
        if (recordMapper.selectCount(null) == 0) {
            initServiceRecords();
        } else {
            System.out.println("Service records already exist (" + recordMapper.selectCount(null) + " records), skipping initialization");
        }
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
        List<ServiceItem> items = new ArrayList<>();
        
        items.add(createItem("EDU-001", "义务教育入学登记", "education", "EDU", "为适龄儿童办理义务教育阶段入学登记", 0));
        items.add(createItem("EDU-002", "转学申请", "education", "EDU", "办理中小学生转学手续", 0));
        items.add(createItem("EDU-003", "学籍档案查询", "education", "EDU", "查询个人学籍档案信息", 0));
        items.add(createItem("EDU-004", "学历认证", "education", "EDU", "办理学历证书认证", 50));
        
        items.add(createItem("HOU-001", "公共租赁住房申请", "housing", "HOU", "申请公共租赁住房保障", 0));
        items.add(createItem("HOU-002", "住房公积金提取", "housing", "HOU", "办理住房公积金提取业务", 100));
        items.add(createItem("HOU-003", "商品房预售备案", "housing", "HOU", "商品房预售合同备案登记", 200));
        items.add(createItem("HOU-004", "经济适用房申请", "housing", "HOU", "申请经济适用住房", 0));
        
        items.add(createItem("HEA-001", "医疗费用报销", "health", "MED", "办理医保医疗费用报销", 50));
        items.add(createItem("HEA-002", "医保定点医院变更", "health", "MED", "变更个人医保定点医院", 30));
        items.add(createItem("HEA-003", "电子健康卡申领", "health", "MED", "申领电子健康卡", 20));
        items.add(createItem("HEA-004", "家庭医生签约", "health", "MED", "签订家庭医生服务协议", 0));
        
        items.add(createItem("EMP-001", "就业创业证办理", "employment", "EMP", "办理就业创业登记证", 80));
        items.add(createItem("EMP-002", "创业补贴申请", "employment", "EMP", "申请创业扶持补贴", 0));
        items.add(createItem("EMP-003", "职业技能培训补贴", "employment", "EMP", "申请职业技能培训补贴", 0));
        items.add(createItem("EMP-004", "失业登记", "employment", "EMP", "办理失业登记手续", 0));
        
        items.add(createItem("SOC-001", "社会保险参保登记", "social", "SOC", "办理城乡居民社保参保", 60));
        items.add(createItem("SOC-002", "社保卡挂失补办", "social", "SOC", "社保卡挂失及补办", 40));
        items.add(createItem("SOC-003", "养老保险转移", "social", "SOC", "办理养老保险关系转移", 0));
        items.add(createItem("SOC-004", "工伤保险待遇申领", "social", "SOC", "申请工伤保险待遇", 0));
        
        items.add(createItem("TRA-001", "机动车驾驶证申领", "traffic", "TRA", "初次申领机动车驾驶证", 150));
        items.add(createItem("TRA-002", "车辆年检预约", "traffic", "TRA", "预约机动车年检服务", 100));
        items.add(createItem("TRA-003", "道路运输证办理", "traffic", "TRA", "办理道路运输经营许可证", 200));
        items.add(createItem("TRA-004", "网约车驾驶员证", "traffic", "TRA", "申请网络预约出租汽车驾驶员证", 120));
        
        items.add(createItem("TAX-001", "增值税一般纳税人登记", "tax", "TAX", "办理增值税一般纳税人资格登记", 0));
        items.add(createItem("TAX-002", "个人所得税汇算清缴", "tax", "TAX", "办理个人所得税年度汇算", 0));
        items.add(createItem("TAX-003", "发票领用", "tax", "TAX", "领用增值税发票", 0));
        items.add(createItem("TAX-004", "税收减免申请", "tax", "TAX", "申请税收优惠减免", 0));
        
        items.add(createItem("CER-001", "居民身份证办理", "certificate", "POL", "办理居民身份证", 50));
        items.add(createItem("CER-002", "户口迁移", "certificate", "POL", "办理户口迁移手续", 30));
        items.add(createItem("CER-003", "护照申请", "certificate", "POL", "申请普通护照", 200));
        items.add(createItem("CER-004", "不动产权证办理", "certificate", "POL", "办理不动产权证书", 300));
        
        for (ServiceItem item : items) {
            serviceItemMapper.insert(item);
        }
        System.out.println("Initialized " + items.size() + " service items");
    }

    private ServiceItem createItem(String code, String name, String category, String deptCode, String description, double price) {
        ServiceItem item = new ServiceItem();
        item.setItemCode(code);
        item.setItemName(name);
        item.setCategory(category);
        item.setDeptCode(deptCode);
        item.setDescription(description);
        item.setStatus(1);
        return item;
    }

    private void initServiceRecords() {
        SysUser admin = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, "admin"));
        SysUser eduUser = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, "edu_user"));
        SysUser heaUser = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, "hea_user"));
        SysUser polUser = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, "pol_user"));
        SysUser socUser = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, "soc_user"));
        SysUser medUser = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, "med_user"));

        List<ServiceRecord> records = new ArrayList<>();
        
        if (admin != null) {
            records.add(createRecord(1L, admin.getId(), "管理员", "已办结", "已支付", "已发放", "{\"userName\":\"管理员\",\"idCard\":\"110101199001011234\",\"phone\":\"13800000001\",\"address\":\"北京市东城区长安街1号\",\"reason\":\"办理教育服务相关事项\"}"));
            records.add(createRecord(6L, admin.getId(), "管理员", "审核中", "待支付", "未领取", "{\"userName\":\"管理员\",\"idCard\":\"110101199001011234\",\"phone\":\"13800000001\",\"address\":\"北京市东城区长安街1号\",\"reason\":\"申请住房保障\"}"));
            records.add(createRecord(17L, admin.getId(), "管理员", "提交申请", "待支付", "未领取", "{\"userName\":\"管理员\",\"idCard\":\"110101199001011234\",\"phone\":\"13800000001\",\"address\":\"北京市东城区长安街1号\",\"reason\":\"申请社保服务\"}"));
        }
        
        if (eduUser != null) {
            records.add(createRecord(1L, eduUser.getId(), "教育用户", "已办结", "已支付", "已发放", "{\"userName\":\"张三\",\"idCard\":\"110101199503052345\",\"phone\":\"13900001001\",\"address\":\"北京市海淀区中关村大街10号\",\"reason\":\"义务教育入学登记\"}"));
            records.add(createRecord(2L, eduUser.getId(), "教育用户", "审核中", "待支付", "未领取", "{\"userName\":\"张三\",\"idCard\":\"110101199503052345\",\"phone\":\"13900001001\",\"address\":\"北京市海淀区中关村大街10号\",\"reason\":\"因搬家申请转学\"}"));
            records.add(createRecord(4L, eduUser.getId(), "教育用户", "提交申请", "待支付", "未领取", "{\"userName\":\"张三\",\"idCard\":\"110101199503052345\",\"phone\":\"13900001001\",\"address\":\"北京市海淀区中关村大街10号\",\"reason\":\"申请学历认证\"}"));
        }
        
        if (heaUser != null) {
            records.add(createRecord(9L, heaUser.getId(), "卫生用户", "已办结", "已支付", "已发放", "{\"userName\":\"李四\",\"idCard\":\"310101198805063456\",\"phone\":\"13700002001\",\"address\":\"上海市黄浦区南京东路20号\",\"reason\":\"医疗费用报销\"}"));
            records.add(createRecord(11L, heaUser.getId(), "卫生用户", "审核中", "待支付", "未领取", "{\"userName\":\"李四\",\"idCard\":\"310101198805063456\",\"phone\":\"13700002001\",\"address\":\"上海市黄浦区南京东路20号\",\"reason\":\"申领电子健康卡\"}"));
            records.add(createRecord(10L, heaUser.getId(), "卫生用户", "提交申请", "待支付", "未领取", "{\"userName\":\"李四\",\"idCard\":\"310101198805063456\",\"phone\":\"13700002001\",\"address\":\"上海市黄浦区南京东路20号\",\"reason\":\"医保定点医院变更\"}"));
        }
        
        if (polUser != null) {
            records.add(createRecord(33L, polUser.getId(), "公安用户", "已办结", "已支付", "已发放", "{\"userName\":\"王五\",\"idCard\":\"440101199207084567\",\"phone\":\"13600003001\",\"address\":\"广州市天河区天河路30号\",\"reason\":\"办理居民身份证\"}"));
            records.add(createRecord(34L, polUser.getId(), "公安用户", "审核中", "待支付", "未领取", "{\"userName\":\"王五\",\"idCard\":\"440101199207084567\",\"phone\":\"13600003001\",\"address\":\"广州市天河区天河路30号\",\"reason\":\"户口迁移\"}"));
            records.add(createRecord(35L, polUser.getId(), "公安用户", "提交申请", "待支付", "未领取", "{\"userName\":\"王五\",\"idCard\":\"440101199207084567\",\"phone\":\"13600003001\",\"address\":\"广州市天河区天河路30号\",\"reason\":\"申请护照\"}"));
        }
        
        if (socUser != null) {
            records.add(createRecord(17L, socUser.getId(), "社保用户", "已办结", "已支付", "已发放", "{\"userName\":\"赵六\",\"idCard\":\"510101199409095678\",\"phone\":\"13500004001\",\"address\":\"成都市武侯区人民南路40号\",\"reason\":\"社保参保登记\"}"));
            records.add(createRecord(18L, socUser.getId(), "社保用户", "审核中", "待支付", "未领取", "{\"userName\":\"赵六\",\"idCard\":\"510101199409095678\",\"phone\":\"13500004001\",\"address\":\"成都市武侯区人民南路40号\",\"reason\":\"社保卡挂失补办\"}"));
            records.add(createRecord(20L, socUser.getId(), "社保用户", "提交申请", "待支付", "未领取", "{\"userName\":\"赵六\",\"idCard\":\"510101199409095678\",\"phone\":\"13500004001\",\"address\":\"成都市武侯区人民南路40号\",\"reason\":\"工伤保险待遇申领\"}"));
        }
        
        if (medUser != null) {
            records.add(createRecord(9L, medUser.getId(), "医疗用户", "已办结", "已支付", "已发放", "{\"userName\":\"孙七\",\"idCard\":\"330101199611116789\",\"phone\":\"13400005001\",\"address\":\"杭州市西湖区文三路50号\",\"reason\":\"医疗费用报销\"}"));
            records.add(createRecord(12L, medUser.getId(), "医疗用户", "审核中", "待支付", "未领取", "{\"userName\":\"孙七\",\"idCard\":\"330101199611116789\",\"phone\":\"13400005001\",\"address\":\"杭州市西湖区文三路50号\",\"reason\":\"家庭医生签约\"}"));
            records.add(createRecord(10L, medUser.getId(), "医疗用户", "提交申请", "待支付", "未领取", "{\"userName\":\"孙七\",\"idCard\":\"330101199611116789\",\"phone\":\"13400005001\",\"address\":\"杭州市西湖区文三路50号\",\"reason\":\"医保定点医院变更\"}"));
        }

        for (ServiceRecord record : records) {
            recordMapper.insert(record);
        }
        System.out.println("Initialized " + records.size() + " service records");
    }

    private ServiceRecord createRecord(Long itemId, Long userId, String userName, String status, String payStatus, String licenseStatus, String formData) {
        ServiceRecord record = new ServiceRecord();
        record.setItemId(itemId);
        record.setUserId(userId);
        record.setUserName(userName);
        record.setStatus(status);
        record.setPayStatus(payStatus);
        record.setLicenseStatus(licenseStatus);
        record.setFormData(formData);
        record.setAcceptNo("SL" + System.currentTimeMillis() + (int)(Math.random() * 1000));
        record.setSubmitTime(LocalDateTime.now().minusDays((int)(Math.random() * 7)));
        record.setPayAmount(new java.math.BigDecimal("100.00"));
        record.setPayTime(payStatus.equals("已支付") ? LocalDateTime.now().minusDays((int)(Math.random() * 3)) : null);
        return record;
    }
}