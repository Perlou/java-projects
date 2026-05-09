import request from "./request";
import type { SalesReport, ProductRank } from "../types";

export const reportApi = {
  // 销售报表
  getDailySales(startDate: string, endDate: string) {
    return request.get<SalesReport[]>("/reports/sales", { startDate, endDate });
  },

  // 热销商品
  getTopProducts(days: number = 30, limit: number = 10) {
    return request.get<ProductRank[]>("/reports/top-products", { days, limit });
  },

  // 清除缓存
  clearCache() {
    return request.delete<string>("/reports/cache");
  },
};
