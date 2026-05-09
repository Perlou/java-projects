<script setup lang="ts">
import { ref, onMounted } from "vue";
import { observabilityApi } from "@mall/shared/api";
import { ElMessage } from "element-plus";

const loading = ref(false);
const overview = ref<any>(null);
const traceInfo = ref<any>(null);
const seckillMetrics = ref<any>(null);
const alertRules = ref<any[]>([]);
const logLevels = ref<any>(null);
const metricTypes = ref<any>(null);

const loadData = async () => {
  loading.value = true;
  try {
    const [overviewRes, traceRes, metricsRes, alertsRes, logsRes, typesRes] =
      await Promise.all([
        observabilityApi.getOverview(),
        observabilityApi.getCurrentTrace(),
        observabilityApi.getSeckillMetrics(),
        observabilityApi.getAlertRules(),
        observabilityApi.getLogLevels(),
        observabilityApi.getMetricTypes(),
      ]);

    if (overviewRes.code === 200) overview.value = overviewRes.data;
    if (traceRes.code === 200) traceInfo.value = traceRes.data;
    if (metricsRes.code === 200) seckillMetrics.value = metricsRes.data;
    if (alertsRes.code === 200) alertRules.value = alertsRes.data || [];
    if (logsRes.code === 200) logLevels.value = logsRes.data;
    if (typesRes.code === 200) metricTypes.value = typesRes.data;
  } catch (e) {
    console.error("加载可观测性数据失败", e);
  } finally {
    loading.value = false;
  }
};

const checkAlerts = async () => {
  try {
    const res = await observabilityApi.checkAlerts();
    if (res.code === 200) {
      const count = res.data?.length || 0;
      if (count > 0) {
        ElMessage.warning(`检测到 ${count} 条告警！`);
      } else {
        ElMessage.success("无告警触发");
      }
    }
  } catch (e) {
    ElMessage.error("告警检测失败");
  }
};

const testLogStructured = async () => {
  try {
    await observabilityApi.logStructured("test_event", 1, "测试日志");
    ElMessage.success("结构化日志已记录，请查看控制台");
  } catch (e) {
    ElMessage.error("记录日志失败");
  }
};

onMounted(loadData);
</script>

<template>
  <div class="observability-page" v-loading="loading">
    <el-row :gutter="20">
      <!-- 三大支柱概览 -->
      <el-col :span="24">
        <el-card>
          <template #header>
            <span
              ><el-icon><View /></el-icon> Phase 18: 可观测性三大支柱</span
            >
          </template>
          <div v-if="overview?.pillars">
            <el-descriptions :column="3" border>
              <el-descriptions-item
                v-for="(desc, key) in overview.pillars"
                :key="String(key)"
                :label="String(key)"
              >
                {{ desc }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 链路追踪 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span
              ><el-icon><Connection /></el-icon> 链路追踪</span
            >
          </template>
          <div v-if="traceInfo">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="TraceId">
                <el-tag type="primary">{{ traceInfo.traceId || "N/A" }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="SpanId">
                <el-tag>{{ traceInfo.spanId || "N/A" }}</el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-col>

      <!-- 业务指标 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span
              ><el-icon><DataLine /></el-icon> 秒杀业务指标</span
            >
          </template>
          <div v-if="seckillMetrics">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="总请求数">
                {{ seckillMetrics.requestsTotal }}
              </el-descriptions-item>
              <el-descriptions-item label="成功数">
                <el-tag type="success">{{
                  seckillMetrics.successTotal
                }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="失败数">
                <el-tag type="danger">{{ seckillMetrics.failTotal }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="售罄数">
                {{ seckillMetrics.soldOutTotal }}
              </el-descriptions-item>
              <el-descriptions-item label="队列大小">
                {{ seckillMetrics.currentQueueSize }}
              </el-descriptions-item>
              <el-descriptions-item label="活跃用户">
                {{ seckillMetrics.activeUsers }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 告警规则 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span
                ><el-icon><Bell /></el-icon> 告警规则</span
              >
              <el-button size="small" type="warning" @click="checkAlerts">
                检测告警
              </el-button>
            </div>
          </template>
          <el-table :data="alertRules" max-height="200">
            <el-table-column prop="name" label="规则名" width="140" />
            <el-table-column label="级别" width="100">
              <template #default="{ row }">
                <el-tag
                  :type="
                    row.severity?.includes('CRITICAL')
                      ? 'danger'
                      : row.severity?.includes('HIGH')
                      ? 'warning'
                      : 'info'
                  "
                  size="small"
                >
                  {{ row.severity?.split("_")[0] }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" />
          </el-table>
        </el-card>
      </el-col>

      <!-- 日志级别 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span
                ><el-icon><Document /></el-icon> 日志级别规范</span
              >
              <el-button size="small" @click="testLogStructured">
                记录测试日志
              </el-button>
            </div>
          </template>
          <div v-if="logLevels">
            <el-descriptions :column="1" border>
              <el-descriptions-item
                v-for="(info, level) in logLevels"
                :key="String(level)"
                :label="String(level)"
              >
                {{ info?.description }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div style="margin-top: 20px; text-align: center">
      <el-button type="primary" @click="loadData">
        <el-icon><Refresh /></el-icon> 刷新数据
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
