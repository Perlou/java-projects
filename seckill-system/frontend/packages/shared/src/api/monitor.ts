import request from "./request";
import type { JvmInfo, CacheStats, PoolStatus } from "../types";

export const monitorApi = {
  // 综合面板
  getDashboard() {
    return request.get<any>("/monitor/dashboard");
  },

  // JVM 完整信息
  getJvmInfo() {
    return request.get<JvmInfo>("/monitor/jvm");
  },

  // 内存信息
  getMemory() {
    return request.get<any>("/monitor/jvm/memory");
  },

  // GC 信息
  getGc() {
    return request.get<any>("/monitor/jvm/gc");
  },

  // 线程信息
  getThreads() {
    return request.get<any>("/monitor/jvm/threads");
  },

  // 触发 GC
  triggerGc() {
    return request.post<any>("/monitor/jvm/gc");
  },

  // 缓存统计
  getCacheStats() {
    return request.get<CacheStats>("/monitor/cache/stats");
  },

  // 缓存压测
  cacheStressTest(iterations: number = 1000) {
    return request.post<any>(`/monitor/cache/stress?iterations=${iterations}`);
  },

  // 连接池状态
  getPoolStatus() {
    return request.get<PoolStatus>("/monitor/pool/datasource");
  },

  // 线程池状态
  getThreadPool() {
    return request.get<any>("/monitor/pool/thread");
  },
};
