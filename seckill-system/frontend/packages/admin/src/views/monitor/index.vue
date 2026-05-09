<script setup lang="ts">
import { ref, onMounted } from "vue";
import { monitorApi } from "@mall/shared/api";
import { ElMessage } from "element-plus";

const loading = ref(false);
const jvmInfo = ref<any>(null);
const gcInfo = ref<any>(null);
const cacheStats = ref<any>(null);
const poolStatus = ref<any>(null);
const threadPool = ref<any>(null);

const loadAll = async () => {
  loading.value = true;
  try {
    const [jvm, gc, cache, pool, thread] = await Promise.all([
      monitorApi.getMemory(),
      monitorApi.getGc(),
      monitorApi.getCacheStats(),
      monitorApi.getPoolStatus(),
      monitorApi.getThreadPool(),
    ]);

    if (jvm.code === 200) jvmInfo.value = jvm.data;
    if (gc.code === 200) gcInfo.value = gc.data;
    if (cache.code === 200) cacheStats.value = cache.data;
    if (pool.code === 200) poolStatus.value = pool.data;
    if (thread.code === 200) threadPool.value = thread.data;
  } catch (e) {
    console.error("加载监控数据失败", e);
  } finally {
    loading.value = false;
  }
};

const triggerGc = async () => {
  try {
    await monitorApi.triggerGc();
    ElMessage.success("GC 已触发");
    loadAll();
  } catch (e) {
    ElMessage.error("触发 GC 失败");
  }
};

onMounted(loadAll);
</script>

<template>
  <div class="monitor-page" v-loading="loading">
    <el-row :gutter="20">
      <!-- JVM 内存 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span
                ><el-icon><Cpu /></el-icon> JVM 内存</span
              >
              <el-button size="small" @click="triggerGc">触发 GC</el-button>
            </div>
          </template>
          <div v-if="jvmInfo">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="堆内存初始"
                >{{ jvmInfo.heap?.init_mb }} MB</el-descriptions-item
              >
              <el-descriptions-item label="堆内存使用"
                >{{ jvmInfo.heap?.used_mb }} MB</el-descriptions-item
              >
              <el-descriptions-item label="堆内存最大"
                >{{ jvmInfo.heap?.max_mb }} MB</el-descriptions-item
              >
              <el-descriptions-item label="使用率">
                <el-progress
                  :percentage="parseFloat(jvmInfo.heap?.usage_percent || '0')"
                  :status="
                    parseFloat(jvmInfo.heap?.usage_percent || '0') > 80
                      ? 'exception'
                      : ''
                  "
                />
              </el-descriptions-item>
              <el-descriptions-item label="非堆内存"
                >{{ jvmInfo.non_heap?.used_mb }} MB</el-descriptions-item
              >
            </el-descriptions>
          </div>
        </el-card>
      </el-col>

      <!-- GC 统计 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span
              ><el-icon><Odometer /></el-icon> GC 统计</span
            >
          </template>
          <div v-if="gcInfo">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="GC 总次数">{{
                gcInfo.total_gc_count
              }}</el-descriptions-item>
              <el-descriptions-item label="GC 总耗时"
                >{{ gcInfo.total_gc_time_ms }} ms</el-descriptions-item
              >
            </el-descriptions>
            <el-table :data="gcInfo.collectors || []" style="margin-top: 10px">
              <el-table-column prop="name" label="收集器" />
              <el-table-column prop="type" label="类型" width="100" />
              <el-table-column
                prop="collection_count"
                label="次数"
                width="80"
              />
              <el-table-column
                prop="collection_time_ms"
                label="耗时(ms)"
                width="100"
              />
            </el-table>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 缓存统计 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span
              ><el-icon><Coin /></el-icon> Caffeine 缓存</span
            >
          </template>
          <div v-if="cacheStats">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="缓存大小">{{
                cacheStats.estimated_size
              }}</el-descriptions-item>
              <el-descriptions-item label="命中次数">{{
                cacheStats.hit_count
              }}</el-descriptions-item>
              <el-descriptions-item label="未命中">{{
                cacheStats.miss_count
              }}</el-descriptions-item>
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
              <el-descriptions-item label="驱逐次数">{{
                cacheStats.eviction_count
              }}</el-descriptions-item>
              <el-descriptions-item label="建议">{{
                cacheStats.suggestion
              }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-col>

      <!-- 连接池 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span
              ><el-icon><Connection /></el-icon> HikariCP 连接池</span
            >
          </template>
          <div v-if="poolStatus">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="池名称">{{
                poolStatus.config?.pool_name
              }}</el-descriptions-item>
              <el-descriptions-item label="最大连接">{{
                poolStatus.config?.maximum_pool_size
              }}</el-descriptions-item>
              <el-descriptions-item label="总连接">{{
                poolStatus.runtime?.total_connections
              }}</el-descriptions-item>
              <el-descriptions-item label="活跃连接">{{
                poolStatus.runtime?.active_connections
              }}</el-descriptions-item>
              <el-descriptions-item label="空闲连接">{{
                poolStatus.runtime?.idle_connections
              }}</el-descriptions-item>
              <el-descriptions-item label="使用率">
                <el-tag
                  :type="
                    parseFloat(poolStatus.usage_percent) > 80
                      ? 'danger'
                      : 'success'
                  "
                >
                  {{ poolStatus.usage_percent }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div style="margin-top: 20px; text-align: center">
      <el-button type="primary" @click="loadAll">
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
