<script setup lang="ts">
import { ref, onMounted } from "vue";
import { reportApi } from "@mall/shared/api";
import type { SalesReport, ProductRank } from "@mall/shared/types";
import { formatPrice } from "@mall/shared/utils";

const loading = ref(false);
const salesData = ref<SalesReport[]>([]);
const topProducts = ref<ProductRank[]>([]);
const dateRange = ref<[Date, Date]>([
  new Date(Date.now() - 30 * 24 * 60 * 60 * 1000),
  new Date(),
]);

const loadSalesReport = async () => {
  if (!dateRange.value) return;
  loading.value = true;
  try {
    const startDate = dateRange.value[0].toISOString().split("T")[0];
    const endDate = dateRange.value[1].toISOString().split("T")[0];
    const res = await reportApi.getDailySales(startDate, endDate);
    if (res.code === 200) {
      salesData.value = res.data || [];
    }
  } catch (e) {
    console.error("加载销售报表失败", e);
  } finally {
    loading.value = false;
  }
};

const loadTopProducts = async () => {
  try {
    const res = await reportApi.getTopProducts(30, 10);
    if (res.code === 200) {
      topProducts.value = res.data || [];
    }
  } catch (e) {
    console.error("加载热销商品失败", e);
  }
};

onMounted(() => {
  loadSalesReport();
  loadTopProducts();
});
</script>

<template>
  <div class="report-page">
    <el-row :gutter="20">
      <el-col :span="14">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>销售统计</span>
              <div>
                <el-date-picker
                  v-model="dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  style="width: 260px; margin-right: 10px"
                />
                <el-button type="primary" @click="loadSalesReport"
                  >查询</el-button
                >
              </div>
            </div>
          </template>

          <el-table
            :data="salesData"
            v-loading="loading"
            stripe
            max-height="400"
          >
            <el-table-column prop="saleDate" label="日期" width="150" />
            <el-table-column prop="orderCount" label="订单数" width="100" />
            <el-table-column prop="totalSales" label="销售额">
              <template #default="{ row }">
                <span class="price">{{ formatPrice(row.totalSales) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card>
          <template #header>
            <span>热销商品 TOP 10</span>
          </template>

          <el-table :data="topProducts" stripe max-height="400">
            <el-table-column type="index" label="排名" width="60" />
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column prop="totalQuantity" label="销量" width="80" />
            <el-table-column prop="totalSales" label="销售额" width="100">
              <template #default="{ row }">
                <span class="price">{{ formatPrice(row.totalSales) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
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
</style>
