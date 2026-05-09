<script setup lang="ts">
import { ref, onMounted } from "vue";
import { monitorApi, reportApi } from "@mall/shared/api";

const loading = ref(false);
const stats = ref({
  orderCount: 0,
  productCount: 0,
  userCount: 0,
  todaySales: 0,
});

const jvmInfo = ref<any>(null);
const cacheStats = ref<any>(null);

const loadData = async () => {
  loading.value = true;
  try {
    // 加载监控数据
    const dashboardRes = await monitorApi.getDashboard();
    if (dashboardRes.code === 200) {
      jvmInfo.value = dashboardRes.data.jvm_memory;
      cacheStats.value = dashboardRes.data.cache_stats;
    }
  } catch (e) {
    console.error("加载数据失败", e);
  } finally {
    loading.value = false;
  }
};

onMounted(loadData);
</script>

<template>
  <div class="dashboard" v-loading="loading">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div
            class="stat-icon"
            style="
              background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            "
          >
            <el-icon size="32"><ShoppingCart /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.orderCount }}</div>
            <div class="stat-label">订单总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div
            class="stat-icon"
            style="
              background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
            "
          >
            <el-icon size="32"><Goods /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.productCount }}</div>
            <div class="stat-label">商品总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div
            class="stat-icon"
            style="
              background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
            "
          >
            <el-icon size="32"><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.userCount }}</div>
            <div class="stat-label">用户总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div
            class="stat-icon"
            style="
              background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
            "
          >
            <el-icon size="32"><Money /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ stats.todaySales.toFixed(2) }}</div>
            <div class="stat-label">今日销售额</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- JVM 和缓存信息 -->
    <el-row :gutter="20" class="info-cards">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span
              ><el-icon><Monitor /></el-icon> JVM 内存状态</span
            >
          </template>
          <div v-if="jvmInfo" class="jvm-info">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="堆内存使用">
                {{ jvmInfo.heap?.used_mb }} MB / {{ jvmInfo.heap?.max_mb }} MB
              </el-descriptions-item>
              <el-descriptions-item label="使用率">
                <el-progress
                  :percentage="parseFloat(jvmInfo.heap?.usage_percent || '0')"
                  :status="
                    parseFloat(jvmInfo.heap?.usage_percent || '0') > 80
                      ? 'warning'
                      : 'success'
                  "
                />
              </el-descriptions-item>
            </el-descriptions>
          </div>
          <el-empty v-else description="暂无数据" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span
              ><el-icon><Coin /></el-icon> 缓存统计</span
            >
          </template>
          <div v-if="cacheStats" class="cache-info">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="缓存类型">
                {{ cacheStats.cache_type }}
              </el-descriptions-item>
              <el-descriptions-item label="缓存大小">
                {{ cacheStats.estimated_size }}
              </el-descriptions-item>
              <el-descriptions-item label="命中次数">
                {{ cacheStats.hit_count }}
              </el-descriptions-item>
              <el-descriptions-item label="命中率">
                <el-tag
                  :type="
                    parseFloat(cacheStats.hit_rate) >= 80
                      ? 'success'
                      : 'warning'
                  "
                >
                  {{ cacheStats.hit_rate }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </div>
          <el-empty v-else description="暂无数据" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.dashboard {
  min-height: 100%;
}

.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  width: 100%;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-right: 16px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.info-cards {
  margin-top: 20px;
}
</style>
