package com.example.seckill.security.rbac;

import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Phase 19: RBAC 权限服务
 * 
 * 实现基于角色的访问控制 (Role-Based Access Control)
 */
@Service
public class RBACService {

    // 模拟角色定义
    private final Map<String, Role> roles = new LinkedHashMap<>();

    // 模拟用户角色分配
    private final Map<Long, Set<String>> userRoles = new HashMap<>();

    public RBACService() {
        initRoles();
    }

    private void initRoles() {
        // 定义权限
        Permission userRead = new Permission("user", "read");
        Permission userWrite = new Permission("user", "write");
        Permission userDelete = new Permission("user", "delete");
        Permission orderRead = new Permission("order", "read");
        Permission orderWrite = new Permission("order", "write");
        Permission seckillExecute = new Permission("seckill", "execute");
        Permission adminAll = new Permission("*", "*");

        // 定义角色
        roles.put("VIEWER", new Role("VIEWER", "查看者",
                Set.of(userRead, orderRead)));

        roles.put("USER", new Role("USER", "普通用户",
                Set.of(userRead, userWrite, orderRead, orderWrite, seckillExecute)));

        roles.put("OPERATOR", new Role("OPERATOR", "运营人员",
                Set.of(userRead, orderRead, orderWrite)));

        roles.put("ADMIN", new Role("ADMIN", "管理员",
                Set.of(adminAll)));

        // 设置角色继承（RBAC1）
        roles.get("ADMIN").parentRole = "OPERATOR";
        roles.get("OPERATOR").parentRole = "USER";
        roles.get("USER").parentRole = "VIEWER";
    }

    // ==================== 权限检查 ====================

    /**
     * 检查用户是否有指定权限
     */
    public boolean hasPermission(Long userId, String resource, String action) {
        Set<String> userRoleNames = userRoles.getOrDefault(userId, Set.of());

        for (String roleName : userRoleNames) {
            if (roleHasPermission(roleName, resource, action)) {
                return true;
            }
        }
        return false;
    }

    private boolean roleHasPermission(String roleName, String resource, String action) {
        Role role = roles.get(roleName);
        if (role == null)
            return false;

        // 检查直接权限
        for (Permission perm : role.permissions) {
            if (perm.matches(resource, action)) {
                return true;
            }
        }

        // 检查继承的权限（RBAC1）
        if (role.parentRole != null) {
            return roleHasPermission(role.parentRole, resource, action);
        }

        return false;
    }

    /**
     * 检查用户是否有指定角色
     */
    public boolean hasRole(Long userId, String roleName) {
        Set<String> userRoleNames = userRoles.getOrDefault(userId, Set.of());
        return userRoleNames.contains(roleName);
    }

    // ==================== 角色管理 ====================

    /**
     * 为用户分配角色
     */
    public void assignRole(Long userId, String roleName) {
        if (!roles.containsKey(roleName)) {
            throw new IllegalArgumentException("未知角色: " + roleName);
        }
        userRoles.computeIfAbsent(userId, k -> new HashSet<>()).add(roleName);
    }

    /**
     * 移除用户角色
     */
    public void removeRole(Long userId, String roleName) {
        Set<String> roleSet = userRoles.get(userId);
        if (roleSet != null) {
            roleSet.remove(roleName);
        }
    }

    /**
     * 获取用户所有权限
     */
    public Set<Permission> getUserPermissions(Long userId) {
        Set<Permission> permissions = new HashSet<>();
        Set<String> userRoleNames = userRoles.getOrDefault(userId, Set.of());

        for (String roleName : userRoleNames) {
            permissions.addAll(getRoleAllPermissions(roleName));
        }
        return permissions;
    }

    private Set<Permission> getRoleAllPermissions(String roleName) {
        Set<Permission> permissions = new HashSet<>();
        Role role = roles.get(roleName);

        while (role != null) {
            permissions.addAll(role.permissions);
            role = role.parentRole != null ? roles.get(role.parentRole) : null;
        }
        return permissions;
    }

    // ==================== RBAC 概念说明 ====================

    /**
     * 获取 RBAC 模型层级说明
     */
    public Map<String, Object> getRBACModelsInfo() {
        Map<String, Object> models = new LinkedHashMap<>();

        models.put("RBAC0", Map.of(
                "name", "基础模型",
                "description", "用户 ↔ 角色 ↔ 权限，最简单的 RBAC"));

        models.put("RBAC1", Map.of(
                "name", "角色继承",
                "description", "角色可以继承其他角色的权限（角色层级）",
                "example", "管理员 继承 编辑员 继承 查看者"));

        models.put("RBAC2", Map.of(
                "name", "约束模型",
                "constraints", List.of(
                        "互斥角色 - 用户不能同时拥有「出纳」和「会计」",
                        "角色数量限制 - 一个用户最多 N 个角色",
                        "先决条件 - 必须先有「员工」才能被分配「经理」")));

        models.put("RBAC3", Map.of(
                "name", "统一模型",
                "description", "RBAC1 + RBAC2，同时支持继承和约束"));

        return models;
    }

    /**
     * 获取所有角色定义
     */
    public List<Map<String, Object>> getAllRoles() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Role role : roles.values()) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("name", role.name);
            r.put("displayName", role.displayName);
            r.put("permissions", role.permissions.stream()
                    .map(p -> p.resource + ":" + p.action).toList());
            r.put("parentRole", role.parentRole);
            result.add(r);
        }
        return result;
    }

    // ==================== 权限命名规范 ====================

    /**
     * 获取权限命名规范
     */
    public Map<String, Object> getPermissionNamingConvention() {
        Map<String, Object> convention = new LinkedHashMap<>();

        convention.put("format", "{resource}:{action}");
        convention.put("examples", List.of(
                "user:read - 读取用户",
                "user:write - 创建/修改用户",
                "user:delete - 删除用户",
                "order:* - 订单所有权限",
                "*:* - 超级管理员"));

        return convention;
    }

    // ==================== 内部类 ====================

    public static class Role {
        public String name;
        public String displayName;
        public Set<Permission> permissions;
        public String parentRole;

        public Role(String name, String displayName, Set<Permission> permissions) {
            this.name = name;
            this.displayName = displayName;
            this.permissions = permissions;
        }
    }

    public static class Permission {
        public String resource;
        public String action;

        public Permission(String resource, String action) {
            this.resource = resource;
            this.action = action;
        }

        public boolean matches(String targetResource, String targetAction) {
            boolean resourceMatch = "*".equals(this.resource) ||
                    this.resource.equals(targetResource);
            boolean actionMatch = "*".equals(this.action) ||
                    this.action.equals(targetAction);
            return resourceMatch && actionMatch;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof Permission p))
                return false;
            return resource.equals(p.resource) && action.equals(p.action);
        }

        @Override
        public int hashCode() {
            return Objects.hash(resource, action);
        }
    }
}
