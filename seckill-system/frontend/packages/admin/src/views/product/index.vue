<script setup lang="ts">
import { ref, onMounted } from "vue";
import { productApi } from "@mall/shared/api";
import type { Product } from "@mall/shared/types";
import { formatPrice, getProductStatusName } from "@mall/shared/utils";
import { ElMessage, ElMessageBox } from "element-plus";

const loading = ref(false);
const productList = ref<Product[]>([]);
const dialogVisible = ref(false);
const form = ref<Partial<Product>>({});

const loadProducts = async () => {
  loading.value = true;
  try {
    const res = await productApi.getOnSale();
    if (res.code === 200) {
      productList.value = res.data || [];
    }
  } catch (e) {
    console.error("加载商品失败", e);
  } finally {
    loading.value = false;
  }
};

const handleAdd = () => {
  form.value = { status: 1, stock: 0 };
  dialogVisible.value = true;
};

const handleEdit = (row: Product) => {
  form.value = { ...row };
  dialogVisible.value = true;
};

const handleSubmit = async () => {
  try {
    if (form.value.id) {
      await productApi.update(form.value.id, form.value);
      ElMessage.success("更新成功");
    } else {
      await productApi.create(form.value);
      ElMessage.success("创建成功");
    }
    dialogVisible.value = false;
    loadProducts();
  } catch (e) {
    ElMessage.error("操作失败");
  }
};

onMounted(loadProducts);
</script>

<template>
  <div class="product-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商品列表</span>
          <div>
            <el-button @click="loadProducts">
              <el-icon><Refresh /></el-icon> 刷新
            </el-button>
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon> 新增商品
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="productList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="商品名称" min-width="200" />
        <el-table-column prop="price" label="价格" width="120">
          <template #default="{ row }">
            <span class="price">{{ formatPrice(row.price) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ getProductStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
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
              :type="row.status === 1 ? 'warning' : 'success'"
              link
            >
              {{ row.status === 1 ? "下架" : "上架" }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑商品' : '新增商品'"
      width="500"
    >
      <el-form :model="form" label-width="80px">
        <el-form-item label="商品名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="form.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="form.stock" :min="0" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
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

.price {
  color: #f56c6c;
  font-weight: bold;
}
</style>
