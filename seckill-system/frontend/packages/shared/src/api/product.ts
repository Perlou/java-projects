import request from "./request";
import type { Product, PageResult } from "../types";

export const productApi = {
  // 获取商品详情
  getById(id: number) {
    return request.get<Product>(`/products/${id}`);
  },

  // 获取上架商品列表
  getOnSale() {
    return request.get<Product[]>("/products");
  },

  // 分页查询
  getPage(pageNum: number = 1, pageSize: number = 10) {
    return request.get<PageResult<Product>>("/products/page", {
      pageNum,
      pageSize,
    });
  },

  // 创建商品
  create(product: Partial<Product>) {
    return request.post<Product>("/products", product);
  },

  // 更新商品
  update(id: number, product: Partial<Product>) {
    return request.put<Product>(`/products/${id}`, product);
  },
};
