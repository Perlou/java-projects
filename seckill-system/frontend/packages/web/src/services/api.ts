import type { ApiResult, SeckillGoods } from "../types";

const API_BASE = "/api/seckill";

export const api = {
  // 获取秒杀商品列表
  async getGoodsList(): Promise<SeckillGoods[]> {
    const response = await fetch(`${API_BASE}/goods`);
    const result: ApiResult<SeckillGoods[]> = await response.json();
    if (result.code === 200) {
      return result.data || [];
    }
    throw new Error(result.message);
  },

  // 获取商品详情
  async getGoods(goodsId: number): Promise<SeckillGoods> {
    const response = await fetch(`${API_BASE}/goods/${goodsId}`);
    const result: ApiResult<SeckillGoods> = await response.json();
    if (result.code === 200) {
      return result.data;
    }
    throw new Error(result.message);
  },

  // 执行秒杀
  async doSeckill(userId: number, goodsId: number): Promise<ApiResult<string>> {
    const response = await fetch(
      `${API_BASE}/do?userId=${userId}&goodsId=${goodsId}`,
      { method: "POST" }
    );
    return response.json();
  },

  // 查询秒杀结果
  async getSeckillResult(
    userId: number,
    goodsId: number
  ): Promise<ApiResult<any>> {
    const response = await fetch(
      `${API_BASE}/result?userId=${userId}&goodsId=${goodsId}`
    );
    return response.json();
  },

  // 重置秒杀
  async resetSeckill(goodsId: number): Promise<ApiResult<string>> {
    const response = await fetch(`${API_BASE}/reset/${goodsId}`, {
      method: "POST",
    });
    return response.json();
  },
};
