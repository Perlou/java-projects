<script setup lang="ts">
import { ref, onMounted } from "vue";
import { userApi } from "@mall/shared/api";
import type { User } from "@mall/shared/types";
import { getUserStatusName } from "@mall/shared/utils";

const loading = ref(false);
const userList = ref<User[]>([]);

const loadUsers = async () => {
  loading.value = true;
  try {
    const res = await userApi.getAll();
    if (res.code === 200) {
      userList.value = res.data || [];
    }
  } catch (e) {
    console.error("加载用户失败", e);
  } finally {
    loading.value = false;
  }
};

onMounted(loadUsers);
</script>

<template>
  <div class="user-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户列表</span>
          <el-button type="primary" @click="loadUsers">
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
        </div>
      </template>

      <el-table :data="userList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="nickname" label="昵称" width="150" />
        <el-table-column prop="phone" label="手机" width="150" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ getUserStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link>编辑</el-button>
            <el-button
              size="small"
              :type="row.status === 1 ? 'danger' : 'success'"
              link
            >
              {{ row.status === 1 ? "禁用" : "启用" }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
