import request from "./request";
import type { SeckillGoods, SeckillOrder } from "../types";

export const seckillApi = {
  // 获取秒杀商品列表
  getGoodsList() {
    return request.get<SeckillGoods[]>("/seckill/goods");
  },

  // 获取商品详情
  getGoods(goodsId: number) {
    return request.get<SeckillGoods>(`/seckill/goods/${goodsId}`);
  },

  // 执行秒杀
  doSeckill(userId: number, goodsId: number) {
    return request.post<string>(
      `/seckill/do?userId=${userId}&goodsId=${goodsId}`
    );
  },

  // 查询秒杀结果
  getResult(userId: number, goodsId: number) {
    return request.get<any>(
      `/seckill/result?userId=${userId}&goodsId=${goodsId}`
    );
  },

  // 重置秒杀
  reset(goodsId: number) {
    return request.post<string>(`/seckill/reset/${goodsId}`);
  },

  // 获取秒杀订单列表
  getOrders() {
    return request.get<SeckillOrder[]>("/seckill/orders");
  },

  // ========== 管理接口 (新增) ==========

  // 创建秒杀商品
  create(goods: Partial<SeckillGoods>) {
    return request.post<SeckillGoods>("/seckill/goods", goods);
  },

  // 更新秒杀商品
  update(id: number, goods: Partial<SeckillGoods>) {
    return request.put<SeckillGoods>(`/seckill/goods/${id}`, goods);
  },

  // 删除秒杀商品
  delete(id: number) {
    return request.delete<void>(`/seckill/goods/${id}`);
  },
};
