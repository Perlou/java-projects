import { createRouter, createWebHistory } from "vue-router";
import type { RouteRecordRaw } from "vue-router";
import Layout from "@/layouts/Layout.vue";

const routes: RouteRecordRaw[] = [
  {
    path: "/",
    component: Layout,
    redirect: "/dashboard",
    children: [
      {
        path: "dashboard",
        name: "Dashboard",
        component: () => import("@/views/dashboard/index.vue"),
        meta: { title: "首页", icon: "HomeFilled" },
      },
      {
        path: "user",
        name: "User",
        component: () => import("@/views/user/index.vue"),
        meta: { title: "用户管理", icon: "User" },
      },
      {
        path: "product",
        name: "Product",
        component: () => import("@/views/product/index.vue"),
        meta: { title: "商品管理", icon: "Goods" },
      },
      {
        path: "seckill",
        name: "Seckill",
        component: () => import("@/views/seckill/index.vue"),
        meta: { title: "秒杀商品", icon: "Timer" },
      },
      {
        path: "order",
        name: "Order",
        component: () => import("@/views/order/index.vue"),
        meta: { title: "订单管理", icon: "List" },
      },
      {
        path: "report",
        name: "Report",
        component: () => import("@/views/report/index.vue"),
        meta: { title: "报表统计", icon: "DataAnalysis" },
      },
      {
        path: "monitor",
        name: "Monitor",
        component: () => import("@/views/monitor/index.vue"),
        meta: { title: "性能监控", icon: "Monitor" },
      },
      {
        path: "observability",
        name: "Observability",
        component: () => import("@/views/observability/index.vue"),
        meta: { title: "可观测性", icon: "View" },
      },
      {
        path: "security",
        name: "Security",
        component: () => import("@/views/security/index.vue"),
        meta: { title: "安全架构", icon: "Lock" },
      },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
