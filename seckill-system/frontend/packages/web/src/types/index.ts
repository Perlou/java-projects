export interface SeckillGoods {
  id: number;
  goodsName: string;
  goodsImg: string | null;
  originalPrice: number;
  seckillPrice: number;
  stockCount: number;
  startTime: string;
  endTime: string;
  status: number;
}

export interface SeckillOrder {
  id: number;
  userId: number;
  goodsId: number;
  goodsName: string;
  seckillPrice: number;
  status: number;
  createdAt: string;
}

export interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
}

export type SeckillResult = "PENDING" | "QUEUING" | "SUCCESS" | "FAIL";
