import request from "./request";
import type { Order, CreateOrderDTO, PageResult } from "../types";

export const orderApi = {
  // 创建订单
  create(dto: CreateOrderDTO) {
    return request.post<Order>("/orders", dto);
  },

  // 支付订单
  pay(id: number) {
    return request.post<Order>(`/orders/${id}/pay`);
  },

  // 取消订单
  cancel(id: number) {
    return request.post<Order>(`/orders/${id}/cancel`);
  },

  // 获取订单详情
  getById(id: number) {
    return request.get<Order>(`/orders/${id}`);
  },

  // 根据订单号查询
  getByOrderNo(orderNo: string) {
    return request.get<Order>(`/orders/no/${orderNo}`);
  },

  // 获取用户订单
  getByUserId(userId: number) {
    return request.get<Order[]>(`/orders/user/${userId}`);
  },

  // 分页查询
  getPage(pageNum: number = 1, pageSize: number = 10) {
    return request.get<PageResult<Order>>("/orders/page", {
      pageNum,
      pageSize,
    });
  },
};
