/**
 * 统一 API 响应类型
 */
export interface ApiResult<T = any> {
  code: number;
  message: string;
  data: T;
}

/**
 * 分页结果
 */
export interface PageResult<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

/**
 * 用户
 */
export interface User {
  id: number;
  username: string;
  nickname: string;
  phone?: string;
  email?: string;
  status: number;
  createdAt: string;
}

/**
 * 商品分类
 */
export interface Category {
  id: number;
  name: string;
  parentId: number;
  level: number;
  sortOrder: number;
}

/**
 * 商品
 */
export interface Product {
  id: number;
  categoryId: number;
  name: string;
  description: string;
  imageUrl: string;
  price: number;
  stock: number;
  status: number;
  version: number;
  createdAt: string;
}

/**
 * 秒杀商品
 */
export interface SeckillGoods {
  id: number;
  goodsName: string;
  goodsImg: string;
  originalPrice: number;
  seckillPrice: number;
  stockCount: number;
  startTime: string;
  endTime: string;
  status: number;
}

/**
 * 订单
 */
export interface Order {
  id: number;
  orderNo: string;
  userId: number;
  totalAmount: number;
  status: number;
  statusName: string;
  paymentTime?: string;
  createdAt: string;
  items?: OrderItem[];
  username?: string;
}

/**
 * 订单项
 */
export interface OrderItem {
  id: number;
  orderId: number;
  productId: number;
  productName: string;
  unitPrice: number;
  quantity: number;
}

/**
 * 创建订单请求
 */
export interface CreateOrderDTO {
  userId: number;
  items: { productId: number; quantity: number }[];
}

/**
 * 销售报表
 */
export interface SalesReport {
  saleDate: string;
  orderCount: number;
  totalSales: number;
}

/**
 * 热销商品排行
 */
export interface ProductRank {
  productName: string;
  totalQuantity: number;
  totalSales: number;
}

/**
 * JVM 信息
 */
export interface JvmInfo {
  memory: {
    heap: { used_mb: number; max_mb: number; usage_percent: string };
    non_heap: { used_mb: number };
  };
  gc: {
    total_gc_count: number;
    total_gc_time_ms: number;
  };
  thread: {
    thread_count: number;
    peak_thread_count: number;
  };
}

/**
 * 缓存统计
 */
export interface CacheStats {
  cache_type: string;
  estimated_size: number;
  hit_count: number;
  miss_count: number;
  hit_rate: string;
}

/**
 * 连接池状态
 */
export interface PoolStatus {
  config: {
    pool_name: string;
    maximum_pool_size: number;
  };
  runtime: {
    total_connections: number;
    active_connections: number;
    idle_connections: number;
  };
  usage_percent: string;
}

/**
 * 秒杀订单
 */
export interface SeckillOrder {
  id: number;
  userId: number;
  goodsId: number;
  goodsName: string;
  seckillPrice: number;
  status: number;
  createdAt: string;
  updatedAt: string;
}
