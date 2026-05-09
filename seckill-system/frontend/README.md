# 前端 Monorepo

基于 pnpm workspace 的前端项目。

## 快速开始

```bash
pnpm install

# 后台管理 (http://localhost:5174)
pnpm dev:admin

# 用户端 (http://localhost:5173)
pnpm dev:web
```

## 项目结构

| 包             | 技术栈                 | 说明           |
| -------------- | ---------------------- | -------------- |
| `@mall/shared` | TypeScript + Axios     | 共享 API/类型  |
| `@mall/web`    | React 19 + Vite        | 用户端秒杀页面 |
| `@mall/admin`  | Vue 3.5 + Element Plus | 后台管理系统   |

## Admin 页面

| 路由           | 功能             |
| -------------- | ---------------- |
| /dashboard     | 数据概览         |
| /seckill       | 秒杀商品管理     |
| /order         | 订单管理         |
| /monitor       | 性能监控         |
| /observability | Phase18 可观测性 |
| /security      | Phase19 安全架构 |

## API 模块 (`@mall/shared/api`)

- `seckillApi` - 秒杀接口
- `monitorApi` - 监控接口
- `observabilityApi` - 可观测性 (Phase18)
- `securityApi` - 安全架构 (Phase19)
