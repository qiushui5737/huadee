package com.gov.service.controller;

import com.gov.admin.entity.SysUser;
import com.gov.admin.mapper.SysUserMapper;
import com.gov.common.result.Result;
import com.gov.common.result.PageResult;
import com.gov.common.utils.JwtUtil;
import com.gov.service.entity.ServiceItem;
import com.gov.service.service.ServiceItemService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * B1-办事事项目录管理
 */
@RestController
@RequestMapping("/api/v1/service/catalog")
public class CatalogController {

    @Autowired
    private ServiceItemService serviceItemService;

    @Autowired
    private SysUserMapper userMapper;

    @GetMapping("/categories")
    public Result<List<Map<String,Object>>> categories() {
        return Result.success(List.of(
            Map.of("code","education","name","教育服务"),
            Map.of("code","housing","name","住房保障"),
            Map.of("code","health","name","医疗卫生"),
            Map.of("code","employment","name","就业创业"),
            Map.of("code","social","name","社会保障"),
            Map.of("code","traffic","name","交通出行"),
            Map.of("code","tax","name","税费办理"),
            Map.of("code","certificate","name","证件办理")
        ));
    }

    @GetMapping("/items")
    public Result<PageResult<Map<String,Object>>> items(@RequestParam(required=false) String category,
            @RequestParam(required=false) String keyword,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        List<ServiceItem> items;
        if (category != null && !category.isEmpty()) {
            items = serviceItemService.listByCategory(category);
        } else {
            items = serviceItemService.list();
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            items = items.stream()
                .filter(item -> item.getItemName().contains(keyword) || 
                               (item.getDescription() != null && item.getDescription().contains(keyword)))
                .collect(Collectors.toList());
        }
        
        int total = items.size();
        int start = (page - 1) * size;
        List<ServiceItem> pageItems = start < total ? 
            items.subList(start, Math.min(start + size, total)) : Collections.emptyList();
        
        List<Map<String, Object>> result = pageItems.stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", item.getId());
            map.put("itemCode", item.getItemCode());
            map.put("itemName", item.getItemName());
            map.put("category", item.getCategory());
            map.put("deptCode", item.getDeptCode());
            map.put("description", item.getDescription());
            map.put("formSchema", item.getFormSchema());
            map.put("price", item.getPrice() != null ? item.getPrice() : java.math.BigDecimal.ZERO);
            map.put("conditions", item.getConditions());
            map.put("timeLimit", item.getTimeLimit());
            map.put("processDesc", item.getProcessDesc());
            map.put("status", item.getStatus());
            map.put("createTime", item.getCreateTime());
            return map;
        }).collect(Collectors.toList());
        
        return Result.success(PageResult.of(result, total, page, size));
    }

    @GetMapping("/items/{id}")
    public Result<Map<String,Object>> itemDetail(@PathVariable Long id) {
        ServiceItem item = serviceItemService.getById(id);
        if (item == null) {
            return Result.error(404, "事项不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", item.getId());
        result.put("itemCode", item.getItemCode());
        result.put("itemName", item.getItemName());
        result.put("category", item.getCategory());
        result.put("deptCode", item.getDeptCode());
        result.put("description", item.getDescription());
        result.put("formSchema", item.getFormSchema());
        result.put("price", item.getPrice() != null ? item.getPrice() : java.math.BigDecimal.ZERO);
        result.put("conditions", item.getConditions());
        result.put("timeLimit", item.getTimeLimit());
        result.put("processDesc", item.getProcessDesc());
        result.put("status", item.getStatus());
        result.put("createTime", item.getCreateTime());
        
        return Result.success(result);
    }

    private String getToken(HttpServletRequest request) {
        String token = (String) request.getAttribute("jwtToken");
        if (token == null) {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                token = header.substring(7);
            }
        }
        return token;
    }

    private boolean isAdmin(HttpServletRequest request) {
        String token = getToken(request);
        if (token == null) return false;
        Claims claims = JwtUtil.parseToken(token);
        String role = claims.get("role", String.class);
        return "ADMIN".equals(role);
    }

    private SysUser getCurrentUser(HttpServletRequest request) {
        String token = getToken(request);
        if (token == null) return null;
        Claims claims = JwtUtil.parseToken(token);
        Number userId = claims.get("userId", Number.class);
        return userMapper.selectById(userId.longValue());
    }

    @GetMapping("/my-items")
    public Result<List<ServiceItem>> myItems(HttpServletRequest request) {
        SysUser user = getCurrentUser(request);
        if (user == null) {
            return Result.error(401, "用户不存在");
        }
        
        List<ServiceItem> items;
        if (isAdmin(request)) {
            items = serviceItemService.list();
        } else {
            items = serviceItemService.listByDeptCode(user.getDeptCode());
        }
        
        return Result.success(items);
    }

    @PostMapping("/items")
    public Result<ServiceItem> createItem(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        SysUser user = getCurrentUser(request);
        if (user == null) {
            return Result.error(401, "用户不存在");
        }
        
        String deptCode = user.getDeptCode();
        if (!isAdmin(request) && (deptCode == null || deptCode.isEmpty())) {
            return Result.error(403, "当前用户无部门信息");
        }
        
        ServiceItem item = new ServiceItem();
        item.setItemCode((String) body.get("itemCode"));
        item.setItemName((String) body.get("itemName"));
        item.setCategory((String) body.get("category"));
        
        if (isAdmin(request)) {
            String reqDeptCode = (String) body.get("deptCode");
            item.setDeptCode(reqDeptCode != null ? reqDeptCode : deptCode);
        } else {
            item.setDeptCode(deptCode);
        }
        
        item.setDescription((String) body.get("description"));
        item.setFormSchema((String) body.get("formSchema"));
        item.setConditions((String) body.get("conditions"));
        item.setTimeLimit((String) body.get("timeLimit"));
        item.setProcessDesc((String) body.get("processDesc"));
        item.setStatus(1);
        item.setCreateTime(LocalDateTime.now());
        
        // 设置缴费金额
        if (body.containsKey("price") && body.get("price") != null) {
            try {
                item.setPrice(new java.math.BigDecimal(body.get("price").toString()));
            } catch (Exception e) {
                item.setPrice(java.math.BigDecimal.ZERO);
            }
        } else {
            item.setPrice(java.math.BigDecimal.ZERO);
        }
        
        serviceItemService.save(item);
        
        return Result.success(item, "服务添加成功");
    }

    @PutMapping("/items/{id}")
    public Result<ServiceItem> updateItem(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        SysUser user = getCurrentUser(request);
        if (user == null) {
            return Result.error(401, "用户不存在");
        }
        
        ServiceItem item = serviceItemService.getById(id);
        if (item == null) {
            return Result.error(404, "服务不存在");
        }
        
        if (!isAdmin(request) && !user.getDeptCode().equals(item.getDeptCode())) {
            return Result.error(403, "无权限修改该服务");
        }
        
        if (body.containsKey("itemName")) {
            item.setItemName((String) body.get("itemName"));
        }
        if (body.containsKey("category")) {
            item.setCategory((String) body.get("category"));
        }
        if (body.containsKey("description")) {
            item.setDescription((String) body.get("description"));
        }
        if (body.containsKey("formSchema")) {
            item.setFormSchema((String) body.get("formSchema"));
        }
        if (body.containsKey("conditions")) {
            item.setConditions((String) body.get("conditions"));
        }
        if (body.containsKey("timeLimit")) {
            item.setTimeLimit((String) body.get("timeLimit"));
        }
        if (body.containsKey("processDesc")) {
            item.setProcessDesc((String) body.get("processDesc"));
        }
        if (body.containsKey("status")) {
            item.setStatus((Integer) body.get("status"));
        }
        if (isAdmin(request) && body.containsKey("deptCode")) {
            item.setDeptCode((String) body.get("deptCode"));
        }
        if (body.containsKey("price") && body.get("price") != null) {
            try {
                item.setPrice(new java.math.BigDecimal(body.get("price").toString()));
            } catch (Exception e) {
                // ignore
            }
        }
        
        serviceItemService.updateById(item);
        
        return Result.success(item, "服务更新成功");
    }

    @DeleteMapping("/items/{id}")
    public Result<Void> deleteItem(@PathVariable Long id, HttpServletRequest request) {
        SysUser user = getCurrentUser(request);
        if (user == null) {
            return Result.error(401, "用户不存在");
        }
        
        ServiceItem item = serviceItemService.getById(id);
        if (item == null) {
            return Result.error(404, "服务不存在");
        }
        
        if (!isAdmin(request) && !user.getDeptCode().equals(item.getDeptCode())) {
            return Result.error(403, "无权限删除该服务");
        }
        
        serviceItemService.removeById(id);
        
        return Result.success(null, "服务删除成功");
    }
}
