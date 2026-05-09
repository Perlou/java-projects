<script setup lang="ts">
import { ref, onMounted } from "vue";
import { orderApi, seckillApi } from "@mall/shared/api";
import type { Order, SeckillOrder, PageResult } from "@mall/shared/types";
import {
  formatPrice,
  formatDateTime,
  getOrderStatusName,
  getOrderStatusColor,
} from "@mall/shared/utils";
import { ElMessage } from "element-plus";

const activeTab = ref("seckill");
const loading = ref(false);

// 秒杀订单
const seckillOrders = ref<SeckillOrder[]>([]);

// 普通订单
const orderList = ref<Order[]>([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);

const loadSeckillOrders = async () => {
  loading.value = true;
  try {
    const res = await seckillApi.getOrders();
    if (res.code === 200) {
      seckillOrders.value = res.data || [];
    }
  } catch (e) {
    console.error("加载秒杀订单失败", e);
  } finally {
    loading.value = false;
  }
};

const loadOrders = async () => {
  loading.value = true;
  try {
    const res = await orderApi.getPage(pageNum.value, pageSize.value);
    if (res.code === 200) {
      const data = res.data as PageResult<Order>;
      orderList.value = data.records || [];
      total.value = data.total;
    }
  } catch (e) {
    console.error("加载订单失败", e);
  } finally {
    loading.value = false;
  }
};

const handlePageChange = (page: number) => {
  pageNum.value = page;
  loadOrders();
};

const handleTabChange = (tab: string) => {
  if (tab === "seckill") {
    loadSeckillOrders();
  } else {
    loadOrders();
  }
};

const getSeckillStatusName = (status: number) => {
  if (status === 0) return "排队中";
  if (status === 1) return "成功";
  return "失败";
};

const getSeckillStatusType = (status: number) => {
  if (status === 0) return "warning";
  if (status === 1) return "success";
  return "danger";
};

onMounted(() => {
  loadSeckillOrders();
});
</script>

<template>
  <div class="order-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单管理</span>
          <el-button
            @click="
              activeTab === 'seckill' ? loadSeckillOrders() : loadOrders()
            "
          >
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 秒杀订单 -->
        <el-tab-pane label="🔥 秒杀订单" name="seckill">
          <el-table :data="seckillOrders" v-loading="loading" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="userId" label="用户ID" width="100" />
            <el-table-column
              prop="goodsName"
              label="商品名称"
              min-width="200"
            />
            <el-table-column prop="seckillPrice" label="秒杀价" width="120">
              <template #default="{ row }">
                <span class="price">{{ formatPrice(row.seckillPrice) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getSeckillStatusType(row.status)">
                  {{ getSeckillStatusName(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="下单时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.createdAt) }}
              </template>
            </el-table-column>
          </el-table>

          <el-empty
            v-if="seckillOrders.length === 0 && !loading"
            description="暂无秒杀订单"
          />
        </el-tab-pane>

        <!-- 普通订单 -->
        <el-tab-pane label="📦 普通订单" name="normal">
          <el-table :data="orderList" v-loading="loading" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="orderNo" label="订单号" width="220" />
            <el-table-column prop="userId" label="用户ID" width="80" />
            <el-table-column prop="totalAmount" label="订单金额" width="120">
              <template #default="{ row }">
                <span class="price">{{ formatPrice(row.totalAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getOrderStatusColor(row.status)">
                  {{ getOrderStatusName(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="下单时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button size="small" type="primary" link>详情</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination" v-if="orderList.length > 0">
            <el-pagination
              v-model:current-page="pageNum"
              :page-size="pageSize"
              :total="total"
              layout="total, prev, pager, next"
              @current-change="handlePageChange"
            />
          </div>

          <el-empty
            v-if="orderList.length === 0 && !loading"
            description="暂无普通订单"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price {
  color: #f56c6c;
  font-weight: bold;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
