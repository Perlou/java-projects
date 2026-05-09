<script setup lang="ts">
import { ref, onMounted } from "vue";
import { securityApi } from "@mall/shared/api";
import { ElMessage } from "element-plus";

const loading = ref(false);
const overview = ref<any>(null);
const rbacRoles = ref<any[]>([]);
const owasp = ref<any[]>([]);
const zeroTrustPrinciples = ref<any>(null);
const tokenInfo = ref<any>(null);
const riskResult = ref<any>(null);

// 测试表单
const testForm = ref({
  userId: 1,
  username: "testuser",
  role: "USER",
  resource: "order",
  action: "read",
  ipAddress: "10.0.0.1",
  hasValidToken: true,
});

const loadData = async () => {
  loading.value = true;
  try {
    const [overviewRes, rolesRes, owaspRes, principlesRes, tokenRes] =
      await Promise.all([
        securityApi.getOverview(),
        securityApi.getAllRoles(),
        securityApi.getOWASPTop10(),
        securityApi.getZeroTrustPrinciples(),
        securityApi.getTokenInfo(),
      ]);

    if (overviewRes.code === 200) overview.value = overviewRes.data;
    if (rolesRes.code === 200) rbacRoles.value = rolesRes.data || [];
    if (owaspRes.code === 200) owasp.value = owaspRes.data || [];
    if (principlesRes.code === 200)
      zeroTrustPrinciples.value = principlesRes.data;
    if (tokenRes.code === 200) tokenInfo.value = tokenRes.data;
  } catch (e) {
    console.error("加载安全数据失败", e);
  } finally {
    loading.value = false;
  }
};

const generateToken = async () => {
  try {
    const res = await securityApi.generateToken(
      testForm.value.userId,
      testForm.value.username,
      testForm.value.role
    );
    if (res.code === 200) {
      ElMessage.success("Token 已生成");
      console.log("Generated Token:", res.data);
    }
  } catch (e) {
    ElMessage.error("生成 Token 失败");
  }
};

const checkPermission = async () => {
  try {
    // 先分配角色
    await securityApi.assignRole(testForm.value.userId, testForm.value.role);

    const res = await securityApi.checkPermission(
      testForm.value.userId,
      testForm.value.resource,
      testForm.value.action
    );
    if (res.code === 200) {
      const allowed = res.data.allowed;
      if (allowed) {
        ElMessage.success(
          `权限检查通过: ${testForm.value.resource}:${testForm.value.action}`
        );
      } else {
        ElMessage.warning(
          `权限被拒绝: ${testForm.value.resource}:${testForm.value.action}`
        );
      }
    }
  } catch (e) {
    ElMessage.error("权限检查失败");
  }
};

const assessRisk = async () => {
  try {
    const res = await securityApi.assessRisk({
      userId: testForm.value.userId,
      ipAddress: testForm.value.ipAddress,
      hasValidToken: testForm.value.hasValidToken,
    });
    if (res.code === 200) {
      riskResult.value = res.data;
      const decision = res.data.decision;
      if (decision === "ALLOW") {
        ElMessage.success(`风险评估: 允许访问 (${res.data.totalScore}分)`);
      } else if (decision === "STEP_UP_AUTH") {
        ElMessage.warning(`风险评估: 需要额外验证 (${res.data.totalScore}分)`);
      } else {
        ElMessage.error(`风险评估: 拒绝访问 (${res.data.totalScore}分)`);
      }
    }
  } catch (e) {
    ElMessage.error("风险评估失败");
  }
};

onMounted(loadData);
</script>

<template>
  <div class="security-page" v-loading="loading">
    <el-row :gutter="20">
      <!-- 安全架构概览 -->
      <el-col :span="24">
        <el-card>
          <template #header>
            <span
              ><el-icon><Lock /></el-icon> Phase 19: 安全架构模块</span
            >
          </template>
          <div v-if="overview?.modules">
            <el-descriptions :column="4" border>
              <el-descriptions-item
                v-for="(desc, key) in overview.modules"
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
      <!-- JWT Token -->
      <el-col :span="8">
        <el-card>
          <template #header>
            <span
              ><el-icon><Key /></el-icon> JWT Token</span
            >
          </template>
          <el-form label-width="80px" size="small">
            <el-form-item label="用户ID">
              <el-input-number v-model="testForm.userId" :min="1" />
            </el-form-item>
            <el-form-item label="用户名">
              <el-input v-model="testForm.username" />
            </el-form-item>
            <el-form-item label="角色">
              <el-select v-model="testForm.role">
                <el-option value="VIEWER" label="VIEWER - 查看者" />
                <el-option value="USER" label="USER - 用户" />
                <el-option value="OPERATOR" label="OPERATOR - 运营" />
                <el-option value="ADMIN" label="ADMIN - 管理员" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="generateToken">
                生成 Token
              </el-button>
            </el-form-item>
          </el-form>
          <div v-if="tokenInfo" style="margin-top: 10px">
            <el-tag
              >Access Token:
              {{ tokenInfo.accessTokenExpireMinutes }}分钟</el-tag
            >
            <el-tag style="margin-left: 8px"
              >Refresh: {{ tokenInfo.refreshTokenExpireDays }}天</el-tag
            >
          </div>
        </el-card>
      </el-col>

      <!-- RBAC 权限 -->
      <el-col :span="8">
        <el-card>
          <template #header>
            <span
              ><el-icon><User /></el-icon> RBAC 权限</span
            >
          </template>
          <el-form label-width="60px" size="small">
            <el-form-item label="资源">
              <el-input v-model="testForm.resource" placeholder="order" />
            </el-form-item>
            <el-form-item label="操作">
              <el-input v-model="testForm.action" placeholder="read" />
            </el-form-item>
            <el-form-item>
              <el-button type="success" @click="checkPermission">
                检查权限
              </el-button>
            </el-form-item>
          </el-form>
          <el-table :data="rbacRoles" max-height="150" size="small">
            <el-table-column prop="name" label="角色" width="90" />
            <el-table-column prop="displayName" label="名称" width="80" />
            <el-table-column label="权限">
              <template #default="{ row }">
                <el-tag
                  v-for="p in row.permissions?.slice(0, 2)"
                  :key="p"
                  size="small"
                  style="margin-right: 4px"
                >
                  {{ p }}
                </el-tag>
                <span v-if="row.permissions?.length > 2">...</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 零信任 -->
      <el-col :span="8">
        <el-card>
          <template #header>
            <span
              ><el-icon><Shield /></el-icon> 零信任评估</span
            >
          </template>
          <el-form label-width="80px" size="small">
            <el-form-item label="IP 地址">
              <el-input v-model="testForm.ipAddress" placeholder="10.0.0.1" />
            </el-form-item>
            <el-form-item label="有效Token">
              <el-switch v-model="testForm.hasValidToken" />
            </el-form-item>
            <el-form-item>
              <el-button type="warning" @click="assessRisk">
                风险评估
              </el-button>
            </el-form-item>
          </el-form>
          <div v-if="riskResult" style="margin-top: 10px">
            <el-tag
              :type="
                riskResult.decision === 'ALLOW'
                  ? 'success'
                  : riskResult.decision === 'DENY'
                  ? 'danger'
                  : 'warning'
              "
            >
              {{ riskResult.decision }} ({{ riskResult.totalScore }}分)
            </el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <!-- OWASP Top 10 -->
      <el-col :span="24">
        <el-card>
          <template #header>
            <span
              ><el-icon><Warning /></el-icon> OWASP Top 10 (2021)</span
            >
          </template>
          <el-table :data="owasp" max-height="300">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="name" label="名称" width="250" />
            <el-table-column prop="nameCn" label="中文" width="150" />
            <el-table-column prop="description" label="描述" />
            <el-table-column label="防御措施" width="300">
              <template #default="{ row }">
                <el-tag
                  v-for="m in row.mitigations?.slice(0, 2)"
                  :key="m"
                  size="small"
                  style="margin-right: 4px"
                >
                  {{ m }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
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
