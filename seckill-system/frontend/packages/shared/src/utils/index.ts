/**
 * 格式化价格
 */
export function formatPrice(price: number): string {
  return `¥${price.toFixed(2)}`;
}

/**
 * 格式化日期时间
 */
export function formatDateTime(dateStr: string): string {
  const date = new Date(dateStr);
  return date.toLocaleString("zh-CN");
}

/**
 * 格式化日期
 */
export function formatDate(dateStr: string): string {
  const date = new Date(dateStr);
  return date.toLocaleDateString("zh-CN");
}

/**
 * 订单状态名称
 */
export function getOrderStatusName(status: number): string {
  const statusMap: Record<number, string> = {
    0: "待支付",
    1: "已支付",
    2: "已发货",
    3: "已完成",
    4: "已取消",
  };
  return statusMap[status] || "未知";
}

/**
 * 订单状态颜色
 */
export function getOrderStatusColor(status: number): string {
  const colorMap: Record<number, string> = {
    0: "warning",
    1: "success",
    2: "primary",
    3: "info",
    4: "danger",
  };
  return colorMap[status] || "info";
}

/**
 * 商品状态名称
 */
export function getProductStatusName(status: number): string {
  return status === 1 ? "上架" : "下架";
}

/**
 * 用户状态名称
 */
export function getUserStatusName(status: number): string {
  return status === 1 ? "正常" : "禁用";
}

/**
 * 格式化文件大小
 */
export function formatFileSize(bytes: number): string {
  if (bytes < 1024) return bytes + " B";
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + " KB";
  if (bytes < 1024 * 1024 * 1024)
    return (bytes / 1024 / 1024).toFixed(2) + " MB";
  return (bytes / 1024 / 1024 / 1024).toFixed(2) + " GB";
}
