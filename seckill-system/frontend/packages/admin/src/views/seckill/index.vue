<script setup lang="ts">
import { ref, onMounted } from "vue";
import { seckillApi } from "@mall/shared/api";
import type { SeckillGoods } from "@mall/shared/types";
import { formatPrice, formatDateTime } from "@mall/shared/utils";
import { ElMessage, ElMessageBox } from "element-plus";

const loading = ref(false);
const goodsList = ref<SeckillGoods[]>([]);
const dialogVisible = ref(false);
const form = ref<Partial<SeckillGoods>>({});

const loadGoods = async () => {
  loading.value = true;
  try {
    const res = await seckillApi.getGoodsList();
    if (res.code === 200) {
      goodsList.value = res.data || [];
    }
  } catch (e) {
    console.error("加载秒杀商品失败", e);
  } finally {
    loading.value = false;
  }
};

const handleAdd = () => {
  form.value = {
    status: 1,
    stockCount: 100,
    originalPrice: 999,
    seckillPrice: 1,
  };
  dialogVisible.value = true;
};

const handleEdit = (row: SeckillGoods) => {
  form.value = { ...row };
  dialogVisible.value = true;
};

const handleSubmit = async () => {
  try {
    if (form.value.id) {
      await seckillApi.update(form.value.id, form.value);
      ElMessage.success("更新成功");
    } else {
      await seckillApi.create(form.value);
      ElMessage.success("创建成功");
    }
    dialogVisible.value = false;
    loadGoods();
  } catch (e) {
    ElMessage.error("操作失败");
  }
};

const handleDelete = async (row: SeckillGoods) => {
  try {
    await ElMessageBox.confirm(`确定要删除 "${row.goodsName}" 吗？`, "提示", {
      type: "warning",
    });
    await seckillApi.delete(row.id);
    ElMessage.success("删除成功");
    loadGoods();
  } catch (e) {
    if (e !== "cancel") {
      ElMessage.error("删除失败");
    }
  }
};

const handleReset = async (row: SeckillGoods) => {
  try {
    await seckillApi.reset(row.id);
    ElMessage.success("重置成功");
    loadGoods();
  } catch (e) {
    ElMessage.error("重置失败");
  }
};

const getStatusType = (status: number) => {
  if (status === 0) return "info";
  if (status === 1) return "success";
  return "danger";
};

const getStatusName = (status: number) => {
  if (status === 0) return "未开始";
  if (status === 1) return "进行中";
  return "已结束";
};

onMounted(loadGoods);
</script>

<template>
  <div class="seckill-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>🔥 秒杀商品管理</span>
          <div>
            <el-button @click="loadGoods">
              <el-icon><Refresh /></el-icon> 刷新
            </el-button>
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon> 新增秒杀商品
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="goodsList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="goodsName" label="商品名称" min-width="180" />
        <el-table-column prop="goodsImg" label="图片" width="80">
          <template #default="{ row }">
            <el-image
              v-if="row.goodsImg"
              :src="row.goodsImg"
              style="width: 40px; height: 40px"
              fit="cover"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="originalPrice" label="原价" width="100">
          <template #default="{ row }">
            <span style="text-decoration: line-through; color: #999">
              {{ formatPrice(row.originalPrice) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="seckillPrice" label="秒杀价" width="100">
          <template #default="{ row }">
            <span class="seckill-price">{{
              formatPrice(row.seckillPrice)
            }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stockCount" label="库存" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              link
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              size="small"
              type="warning"
              link
              @click="handleReset(row)"
            >
              重置
            </el-button>
            <el-button
              size="small"
              type="danger"
              link
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑秒杀商品' : '新增秒杀商品'"
      width="500"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="商品名称" required>
          <el-input v-model="form.goodsName" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="图片地址">
          <el-input v-model="form.goodsImg" placeholder="请输入图片URL" />
        </el-form-item>
        <el-form-item label="原价" required>
          <el-input-number
            v-model="form.originalPrice"
            :min="0"
            :precision="2"
          />
        </el-form-item>
        <el-form-item label="秒杀价" required>
          <el-input-number
            v-model="form.seckillPrice"
            :min="0"
            :precision="2"
          />
        </el-form-item>
        <el-form-item label="库存数量" required>
          <el-input-number v-model="form.stockCount" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="0">未开始</el-radio>
            <el-radio :value="1">进行中</el-radio>
            <el-radio :value="2">已结束</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.seckill-price {
  color: #f56c6c;
  font-weight: bold;
  font-size: 16px;
}
</style>
